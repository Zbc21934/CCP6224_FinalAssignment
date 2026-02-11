package controller;

import model.ParkingLot;
import database.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class ParkingSystemFacade {
    
    private ParkingLot parkingLot;
    
    private Connection conn;

    public ParkingSystemFacade() {
        this.parkingLot = new ParkingLot("University Parking");
        this.parkingLot.initialize(5, 20); 
       
        this.conn = DbConnection.getInstance().getConnection();
    }

    public boolean parkVehicle(String plateNumber, String vehicleType, String spotId) {
  
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
}