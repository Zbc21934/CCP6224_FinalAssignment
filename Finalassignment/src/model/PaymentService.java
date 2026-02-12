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
    
    // 处理支付的核心逻辑
    public boolean processPayment(String plateNumber) {
        Connection conn = DbConnection.getInstance().getConnection();
        
        try {
            // 1. 查找该车当前停在哪个车位 (Active Ticket)
            String findSql = "SELECT spot_id FROM tickets WHERE plate_number = ? AND status = 'ACTIVE'";
            PreparedStatement findPstmt = conn.prepareStatement(findSql);
            findPstmt.setString(1, plateNumber);
            ResultSet rs = findPstmt.executeQuery();

            if (rs.next()) {
                String spotId = rs.getString("spot_id");

                // 2. 更新 Ticket 表 -> 设为 'PAID' 并记录出场时间
                String updateTicket = "UPDATE tickets SET status = 'PAID', exit_time = ? WHERE plate_number = ? AND status = 'ACTIVE'";
                PreparedStatement tStmt = conn.prepareStatement(updateTicket);
                tStmt.setString(1, LocalDateTime.now().toString());
                tStmt.setString(2, plateNumber);
                tStmt.executeUpdate();
                
                // 3. 更新罚款表 (Fines) -> 设为 'PAID' (如果有未付罚款)
                String updateFines = "UPDATE fines SET status = 'PAID' WHERE plate_number = ? AND status = 'UNPAID'";
                PreparedStatement fStmt = conn.prepareStatement(updateFines);
                fStmt.setString(1, plateNumber);
                fStmt.executeUpdate();

                // 4. 释放内存中的车位 (需要通过 Facade 或直接操作 ParkingLot，但这里我们只返回 spotId 让 Controller 去释放)
                // 为了解耦，我们这里只负责数据库。内存的释放交给 Controller。
                return true; 
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 辅助：获取车位ID，方便 Controller 释放内存
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