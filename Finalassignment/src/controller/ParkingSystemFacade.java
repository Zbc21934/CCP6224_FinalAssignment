package controller;

import model.*;
import database.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

public class ParkingSystemFacade {

    private ParkingLot parkingLot;
    private Connection conn;
    private PaymentService paymentService;
    private FineManager fineManager;
    private ReportService reportService;

    public ParkingSystemFacade() {
        // 1. Initialize the parking lot structure
        this.parkingLot = new ParkingLot("University Parking");
        this.parkingLot.initialize(5, 20);

        // 2. Establish database connection
        this.conn = DbConnection.getInstance().getConnection();

        // 3. Initialize Services and Managers
        this.paymentService = new PaymentService();
        this.fineManager = new FineManager();
        this.reportService = new ReportService(this.conn, this.parkingLot);

        // 4. Restore state from DB
        loadActiveTickets();  
    }

    // =============================================================
    // ✅ RESERVATION VERIFICATION
    // =============================================================
    public boolean validateReservation(String resId) {
        if (resId == null || resId.trim().isEmpty()) {
            return false;
        }
        
        String sql = "SELECT * FROM reservations WHERE res_id = ? AND status = 'VALID'";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, resId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return true; 
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Reservation check failed: " + e.getMessage());
        }
        return false; 
    }

    // =============================================================
    // ✅ CORE PARKING LOGIC (UPDATED)
    // =============================================================

    // Update: Now accepts 'hasReservation' to determine if a violation occurred
    public Ticket parkVehicle(String plateNumber, String vehicleType, String spotId, boolean isHandicapped, boolean hasReservation) {
        // Step 1: find spot
        ParkingSpot spot = parkingLot.getSpotById(spotId);
        if (spot == null) {
            System.out.println("Error: Spot not found.");
            return null;
        }

        // Step 2: Create vehicle instance
        Vehicle vehicle;
        try {
            vehicle = Vehicle.create(plateNumber, vehicleType);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: Invalid vehicle type.");
            return null;
        }

        // Step 3: Check basic compatibility
        if (!vehicle.canParkIn(spot)) {
            System.out.println("Validation Failed: " + vehicleType + " cannot park in " + spot.getClass().getSimpleName());
            return null;
        }
        
        // 🔥 Step 4: Determine Violation Status 🔥
        // Logic: If it is a ReservedSpot AND the user does NOT have a valid reservation -> Violation
        boolean isViolation = (spot instanceof ReservedSpot && !hasReservation);
        
        if (isViolation) {
            System.out.println("⚠️ Violation Recorded: " + plateNumber + " in Reserved Spot without ID.");
        }

        // Step 5: Create Ticket (passing violation status)
        Ticket ticket = Ticket.createEntryTicket(plateNumber, spotId, vehicleType, isHandicapped, isViolation);
        
        // Step 6: Insert into Database (Saving is_violation)
        String sql = "INSERT INTO tickets (ticket_id, plate_number, vehicle_type, spot_id, entry_time, status, is_handicapped, is_violation) VALUES (?, ?, ?, ?, ?, 'ACTIVE', ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ticket.getTicketId());
            pstmt.setString(2, ticket.getPlateNumber());
            pstmt.setString(3, ticket.getVehicleType());
            pstmt.setString(4, ticket.getSpotId());
            pstmt.setString(5, ticket.getEntryTime().toString());
            pstmt.setInt(6, isHandicapped ? 1 : 0);
            pstmt.setInt(7, isViolation ? 1 : 0); // ✅ Save violation status to DB
            
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                spot.assignVehicle(vehicle); // Update memory state
                return ticket;
            }
        } catch (SQLException e) {
            System.out.println("DB Check-in Error: " + e.getMessage());
        }
        return null;
    }

    // Vehicle Checkout Wrapper
    public String checkOutVehicle(String plateNumber) {
        return paymentService.generateBillOrReceipt(plateNumber, parkingLot, false);
    }

    public boolean processPayment(String plateNumber, String paymentMethod) {
        String spotId = paymentService.getSpotIdByPlate(plateNumber);
        double amount = paymentService.calculateCurrentFee(plateNumber, parkingLot); // This should eventually call calculateTotalFee

        boolean isPaid = paymentService.processPayment(plateNumber, paymentMethod, amount);

        if (isPaid && spotId != null) {
            ParkingSpot spot = parkingLot.getSpotById(spotId);
            if (spot != null) {
                spot.removeVehicle();
            }
            return true;
        }
        return false;
    }

    public String generateOfficialReceipt(String plateNumber) {
        return paymentService.generateBillOrReceipt(plateNumber, parkingLot, true);
    }
    
    // =============================================================
    // ✅ BILLING & FINE LOGIC (UPDATED)
    // =============================================================

    public void updateFineScheme(String schemeType) {
        fineManager.updateFineScheme(schemeType);
    }
    
    // Core method to calculate total fee
    // Note: 'hasReservation' parameter removed because we now check the DB for the truth
    public double calculateTotalFee(String plate, double durationHours, String spotId) {
        // 1. Basic Parking Fee
        ParkingSpot spot = parkingLot.getSpotById(spotId);
        double rate = (spot != null) ? spot.getHourlyRate() : 0;
        double parkingFee = durationHours * rate;
        
        // 2. Check for violation (Query Database for the truth!)
        boolean isReservedMisuse = false;
        
        String sql = "SELECT is_violation FROM tickets WHERE plate_number = ? AND status = 'ACTIVE'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, plate);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Check if the ticket was marked as a violation at entry
                    isReservedMisuse = (rs.getInt("is_violation") == 1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking violation status: " + e.getMessage());
        }

        // 3. Calculate Fine (Delegate to Strategy)
        // If isReservedMisuse is true, Option B/C will add the fine amount
        double fine = fineManager.calculateFine(durationHours, isReservedMisuse);
        
        return parkingFee + fine;
    }

    // =============================================================
    // REPORTING & UTILITIES
    // =============================================================

    public String getDashboardReport(String reportType) {
        switch(reportType) {
            case "OCCUPANCY": return reportService.getOccupancySummary();
            case "REVENUE"  : return reportService.getRevenueSummary();
            case "VEHICLES" : return reportService.getActiveVehicleTable();
            case "FINES"    : return reportService.getFineReport();
            default         : return reportService.getOccupancySummary() + "\n\n" + reportService.getRevenueSummary();
        }
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    private void loadActiveTickets() {
        System.out.println("Loading active tickets from database...");

        String sql = "SELECT spot_id, plate_number, vehicle_type FROM tickets WHERE status = 'ACTIVE'";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String spotId = rs.getString("spot_id");
                String plate = rs.getString("plate_number");
                String type = rs.getString("vehicle_type");

                ParkingSpot spot = parkingLot.getSpotById(spotId);
                if (spot != null) {
                    Vehicle vehicle = Vehicle.create(plate, type);
                    spot.assignVehicle(vehicle);
                    System.out.println("Restored: " + plate + " at " + spotId);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading active tickets: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Found unknown vehicle type in DB, skipping.");
        }
        
        // Initialize dummy fines for testing
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("INSERT OR IGNORE INTO fines (plate_number, amount, reason, status) VALUES ('JJU8888', 50.0, 'Illegal Parking', 'UNPAID')");
            stmt.executeUpdate("INSERT OR IGNORE INTO fines (plate_number, amount, reason, status) VALUES ('WWA1234', 100.0, 'Overtime > 24h', 'UNPAID')");
        } catch (Exception e) { }
    }
}