package controller;

import model.ParkingLot;
import model.Vehicle;
import model.ParkingSpot;
import database.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.sql.ResultSet;
import java.sql.Statement;

public class ParkingSystemFacade {
    
    private ParkingLot parkingLot;
    
    private Connection conn;

    public ParkingSystemFacade() {
        this.parkingLot = new ParkingLot("University Parking");
        this.parkingLot.initialize(5, 20); 
       
        this.conn = DbConnection.getInstance().getConnection();
       
        loadActiveTickets();  // Restore the parking state from the database
    }

    public boolean parkVehicle(String plateNumber, String vehicleType, String spotId) {
  
        // Step 1: find spot
        ParkingSpot spot = parkingLot.getSpotById(spotId);
        if (spot == null) {
            System.out.println("Error: Spot not found.");
            return false;
        }

        // Step 2: use static method of vehicle
        Vehicle vehicle;
        try {
            vehicle = Vehicle.create(plateNumber, vehicleType);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: Invalid vehicle type.");
            return false;
        }

        // Step 3: check logic: vehicle can park or not
        if (!vehicle.canParkIn(spot)) {
            System.out.println("Validation Failed: " + vehicleType + " cannot park in " + spot.getClass().getSimpleName());
            return false;
        }
        
        String ticketId = "T-" + System.currentTimeMillis();
        LocalDateTime entryTime = LocalDateTime.now();

        String sql = "INSERT INTO tickets (ticket_id, plate_number, vehicle_type, spot_id, entry_time, status) " +
                     "VALUES (?, ?, ?, ?, ?, 'ACTIVE')";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ticketId);
            pstmt.setString(2, plateNumber);
            pstmt.setString(3, vehicleType);
            pstmt.setString(4, spotId);
            pstmt.setString(5, entryTime.toString()); 

            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("Database Insert Success: " + plateNumber);
                //  parkingLot.getSpot(spotId).occupy(); 
                spot.assignVehicle(vehicle); // refresh status in db
                return true;
            }
        } catch (SQLException e) {
            System.out.println("DB error; Check-in Error: " + e.getMessage());
        }
        return false;
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }
    
    private void loadActiveTickets() {
        System.out.println("🔄 Loading active tickets from database...");

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

                    System.out.println("✅ Restored: " + plate + " at " + spotId);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error loading active tickets: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("⚠️ Found unknown vehicle type in DB, skipping.");
        }
    }
}