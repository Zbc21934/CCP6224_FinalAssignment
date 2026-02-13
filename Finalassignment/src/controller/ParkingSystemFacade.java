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

    public ParkingSystemFacade() {
        this.parkingLot = new ParkingLot("University Parking");
        this.parkingLot.initialize(5, 20);

        this.conn = DbConnection.getInstance().getConnection();
        this.paymentService = new PaymentService();
        loadActiveTickets();  // Restore the parking state from the database
    }

    public Ticket parkVehicle(String plateNumber, String vehicleType, String spotId) {

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
        //builder
        Ticket ticket = new Ticket.Builder(plateNumber, spotId)
                .setVehicleType(vehicleType)
                .build();

        String sql = "INSERT INTO tickets (ticket_id, plate_number, vehicle_type, spot_id, entry_time, status) VALUES (?, ?, ?, ?, ?, 'ACTIVE')";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ticket.getTicketId());
            pstmt.setString(2, ticket.getPlateNumber());
            pstmt.setString(3, ticket.getVehicleType());
            pstmt.setString(4, ticket.getSpotId());
            pstmt.setString(5, ticket.getEntryTime().toString());

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
}
