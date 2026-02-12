/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author PatrickToh
 */

import database.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class PaymentService {
    
    public boolean processPayment(String plateNumber) {
        Connection conn = DbConnection.getInstance().getConnection();
        
        try {
            //check active spot
            String findSql = "SELECT spot_id FROM tickets WHERE plate_number = ? AND status = 'ACTIVE'";
            PreparedStatement findPstmt = conn.prepareStatement(findSql);
            findPstmt.setString(1, plateNumber);
            ResultSet rs = findPstmt.executeQuery();

            if (rs.next()) {
                String spotId = rs.getString("spot_id");

                //update ticket set as paid and record exit time
                String updateTicket = "UPDATE tickets SET status = 'PAID', exit_time = ? WHERE plate_number = ? AND status = 'ACTIVE'";
                PreparedStatement tStmt = conn.prepareStatement(updateTicket);
                tStmt.setString(1, LocalDateTime.now().toString());
                tStmt.setString(2, plateNumber);
                tStmt.executeUpdate();
                
                // Fine
                String updateFines = "UPDATE fines SET status = 'PAID' WHERE plate_number = ? AND status = 'UNPAID'";
                PreparedStatement fStmt = conn.prepareStatement(updateFines);
                fStmt.setString(1, plateNumber);
                fStmt.executeUpdate();
                return true; 
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public String getSpotIdByPlate(String plateNumber) {
        Connection conn = DbConnection.getInstance().getConnection();
        try {
            String sql = "SELECT spot_id FROM tickets WHERE plate_number = ? AND status = 'ACTIVE'";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, plateNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("spot_id");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}