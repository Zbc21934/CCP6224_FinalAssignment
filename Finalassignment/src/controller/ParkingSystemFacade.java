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
    private ReportService reportService;

    public ParkingSystemFacade() {
        this.parkingLot = new ParkingLot("University Parking");
        this.parkingLot.initialize(5, 20);

        this.conn = DbConnection.getInstance().getConnection();
        this.paymentService = new PaymentService();
        
        this.paymentService = new PaymentService();
        this.reportService = new ReportService(this.conn, this.parkingLot);
        loadActiveTickets();  // Restore the parking state from the database
    }

    
    public String getDashboardReport(String reportType) {
        switch(reportType) {
            case "OCCUPANCY": return reportService.getOccupancySummary();
            case "REVENUE"  : return reportService.getRevenueSummary();
            case "VEHICLES" : return reportService.getActiveVehicleTable();
            case "FINES"    : return reportService.getFineReport();
            default         : return reportService.getOccupancySummary() + "\n\n" + reportService.getRevenueSummary();
        }
    }
    
    public Ticket parkVehicle(String plateNumber, String vehicleType, String spotId, boolean isHandicapped) {

        // Step 1: find spot
        ParkingSpot spot = parkingLot.getSpotById(spotId);
        if (spot == null) {
            System.out.println("Error: Spot not found.");
            return null;
        }

        // Step 2: use static method of vehicle
        Vehicle vehicle;
        try {
            vehicle = Vehicle.create(plateNumber, vehicleType);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: Invalid vehicle type.");
            return null;
        }

        // Step 3: check logic: vehicle can park or not
        if (!vehicle.canParkIn(spot)) {
            System.out.println("Validation Failed: " + vehicleType + " cannot park in " + spot.getClass().getSimpleName());
            return null;
        }
        
        Ticket ticket = Ticket.createEntryTicket(plateNumber, spotId, vehicleType, isHandicapped);
        String sql = "INSERT INTO tickets (ticket_id, plate_number, vehicle_type, spot_id, entry_time, status, is_handicapped) VALUES (?, ?, ?, ?, ?, 'ACTIVE', ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ticket.getTicketId());
            pstmt.setString(2, ticket.getPlateNumber());
            pstmt.setString(3, ticket.getVehicleType());
            pstmt.setString(4, ticket.getSpotId());
            pstmt.setString(5, ticket.getEntryTime().toString());
            pstmt.setInt(6, isHandicapped ? 1 : 0);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                spot.assignVehicle(vehicle); //refresh db
                return ticket;
            }
        } catch (SQLException e) {
            System.out.println("DB Check-in Error: " + e.getMessage());
        }
        return null;
    }

    //vehicle checkout
    public String checkOutVehicle(String plateNumber) {
        return paymentService.generateBillOrReceipt(plateNumber, parkingLot, false);
    }

    public boolean processPayment(String plateNumber, String paymentMethod) {
        String spotId = paymentService.getSpotIdByPlate(plateNumber);

        // calculate amout
        double amount = paymentService.calculateCurrentFee(plateNumber, parkingLot);

        // call service
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

    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    private void loadActiveTickets() {
        System.out.println("Loading active tickets from database...");

        // Query only for vehicles that are still parked (status = 'ACTIVE')
        String sql = "SELECT spot_id, plate_number, vehicle_type FROM tickets WHERE status = 'ACTIVE'";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String spotId = rs.getString("spot_id");
                String plate = rs.getString("plate_number");
                String type = rs.getString("vehicle_type");

                // 1. Find the matching ParkingSpot object in memory
                ParkingSpot spot = parkingLot.getSpotById(spotId);

                if (spot != null) {
                    // 2. Re-create the Vehicle object
                    Vehicle vehicle = Vehicle.create(plate, type);

                    // 3. Re-assign the vehicle to the spot in memory
                    spot.assignVehicle(vehicle);

                    System.out.println("Restored: " + plate + " at " + spotId);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading active tickets: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Found unknown vehicle type in DB, skipping.");
        }
    }
    
    //REPORT GENERATION
    public String generateDashboardReport() {
        StringBuilder report = new StringBuilder();
        report.append("=========================================\n");
        report.append("       UNIVERSITY PARKING DASHBOARD      \n");
        report.append("=========================================\n\n");

        try {
            //oCCUPANCY REPORT
            // -------------------
            String countSql = "SELECT COUNT(*) AS total FROM tickets WHERE status = 'ACTIVE'";
            Statement stmt = conn.createStatement();
            ResultSet rsCount = stmt.executeQuery(countSql);
            int activeVehicles = 0;
            if (rsCount.next()) {
                activeVehicles = rsCount.getInt("total");
            }
            int totalSpots = 100; // 5 floors * 20 spots
            double occupancyRate = (double) activeVehicles / totalSpots * 100;
            
            report.append("[1] OCCUPANCY REPORT\n");
            report.append("--------------------\n");
            report.append(String.format("Total Spots:    %d\n", totalSpots));
            report.append(String.format("Occupied Spots: %d\n", activeVehicles));
            report.append(String.format("Available:      %d\n", (totalSpots - activeVehicles)));
            report.append(String.format("Occupancy Rate: %.2f%%\n\n", occupancyRate));


            // REVENUE REPORT
            String revSql = "SELECT SUM(total_amount) AS total_rev FROM payments";
            ResultSet rsRev = stmt.executeQuery(revSql);
            double totalRevenue = 0.0;
            if (rsRev.next()) {
                totalRevenue = rsRev.getDouble("total_rev");
            }
            report.append("[2] REVENUE REPORT\n");
            report.append("------------------\n");
            report.append(String.format("Total Revenue Collected: RM %.2f\n\n", totalRevenue));


            //  LIST OF VEHICLES (CURRENTLY IN LOT)
            report.append("[3] CURRENT VEHICLES IN LOT\n");
            report.append("---------------------------\n");
            report.append(String.format("%-15s %-15s %-10s %-20s\n", "Plate No.", "Type", "Spot", "Entry Time"));
            report.append("------------------------------------------------------------\n");
            
            String vehicleSql = "SELECT plate_number, vehicle_type, spot_id, entry_time FROM tickets WHERE status = 'ACTIVE' ORDER BY entry_time DESC";
            ResultSet rsVeh = stmt.executeQuery(vehicleSql);
            boolean hasVehicles = false;
            while (rsVeh.next()) {
                hasVehicles = true;
                String time = rsVeh.getString("entry_time").substring(11, 19);
                report.append(String.format("%-15s %-15s %-10s %-20s\n", 
                        rsVeh.getString("plate_number"),
                        rsVeh.getString("vehicle_type"),
                        rsVeh.getString("spot_id"),
                        time));
            }
            if (!hasVehicles) report.append("No vehicles currently parked.\n");
            report.append("\n");


            // OUTSTANDING FINES REPORT
            report.append("[4] OUTSTANDING FINES (UNPAID)\n");
            report.append("------------------------------\n");
            report.append(String.format("%-15s %-10s %-20s\n", "Plate No.", "Amount", "Reason"));
            report.append("--------------------------------------------------\n");
            
            String fineSql = "SELECT plate_number, amount, reason FROM fines WHERE status = 'UNPAID'";
            ResultSet rsFines = stmt.executeQuery(fineSql);
            boolean hasFines = false;
            while (rsFines.next()) {
                hasFines = true;
                report.append(String.format("%-15s RM %-7.2f %-20s\n", 
                        rsFines.getString("plate_number"),
                        rsFines.getDouble("amount"),
                        rsFines.getString("reason")));
            }
            if (!hasFines) report.append("No outstanding fines.\n");

        } catch (SQLException e) {
            report.append("Error generating report: " + e.getMessage());
        }

        return report.toString();
    }
}
