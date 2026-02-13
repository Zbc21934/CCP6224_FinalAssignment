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
import java.time.format.DateTimeFormatter;

public class PaymentService {

    // isPaid = false = Exit Bill
    // isPaid = true  = Official Receipt
   public String generateBillOrReceipt(String plateNumber, ParkingLot parkingLot, boolean isPaid) {
    Connection conn = DbConnection.getInstance().getConnection();
    try {
        String status = isPaid ? "PAID" : "ACTIVE";
        
        // 1. 确保 SQL 能查到正确的数据
        String sql = "SELECT * FROM tickets WHERE plate_number = ? AND status = ? ORDER BY entry_time DESC LIMIT 1";

        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, plateNumber);
        pstmt.setString(2, status);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            String ticketId = rs.getString("ticket_id");
            // 处理时间解析
            LocalDateTime entryTime = LocalDateTime.parse(rs.getString("entry_time"));
            LocalDateTime exitTime = LocalDateTime.now();
            if (isPaid && rs.getString("exit_time") != null) {
                try {
                    exitTime = LocalDateTime.parse(rs.getString("exit_time"));
                } catch (Exception e) {} 
            }

            String type = rs.getString("vehicle_type");
            String spotId = rs.getString("spot_id");

            // 🟢 2. 关键修改：从数据库读取 is_handicapped
            boolean isHandicapped = rs.getInt("is_handicapped") == 1;

            Vehicle vehicle = Vehicle.create(plateNumber, type);
            ParkingSpot spot = parkingLot.getSpotById(spotId);

            // 🟢 3. 关键修改：传入 isHandicapped 参数进行计算
            double parkingFee = FeeCalculator.calculate(vehicle, spot, entryTime, isHandicapped);
            
            // 计算时长
            long hours = FeeCalculator.getDurationInHours(entryTime);

            // 🟢 4. 优化收据上的“费率”显示 (Rate Display)
            // 如果是 OKU，显示的单价应该是 RM 2.00 或 RM 0.00，而不是车位原本的 RM 10.00
            double displayRate = (spot != null) ? spot.getHourlyRate() : 2.0;
            if (isHandicapped) {
                 if (spot instanceof model.HandicappedSpot) {
                     displayRate = 0.0; // 免费
                 } else {
                     displayRate = 2.0; // 优惠价
                 }
            }

            double fines = 0.00; 
            double totalDue = parkingFee + fines;

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String title = isPaid ? "OFFICIAL RECEIPT" : "Parking Bill (UNPAID)";
            String totalLabel = isPaid ? "AMOUNT PAID" : "TOTAL DUE";
            String color = isPaid ? "green" : "blue";

            String paymentMethodInfo = "";
            if (isPaid) {
                String method = getPaymentMethod(ticketId);
                // 给标题加个漂亮的绿框
                title += " <span style='color:green; border:1px solid green; padding:2px; border-radius:3px; font-size:10px;'>✅ PAID</span>";
                paymentMethodInfo = "<br><b>Payment Method:</b> " + method;
            }
            
            // 🟢 5. 如果是 OKU，加一行提示文字
            String okuLabel = "";
            if (isHandicapped) {
                okuLabel = "<br><span style='color:orange; font-size:10px;'>(Handicapped Rate Applied)</span>";
            }

            return String.format(
                    "<html>"
                    + "<div style='text-align: center;'><b>--- %s ---</b></div><br>"
                    + "<b>License Plate:</b> %s<br>"
                    + "<b>Entry Time:</b> %s<br>"
                    + "<b>Exit Time:</b> %s<br>"
                    + "------------------------------<br>"
                    + "<b>Duration:</b> %d hours<br>"
                    + "<b>Rate:</b> RM %.2f/hr %s<br>"  // 修改了这里，使用 displayRate
                    + "<b>Parking Fee:</b> RM %.2f<br>"
                    + "<b>Unpaid Fines:</b> RM %.2f<br>"
                    + "------------------------------<br>"
                    + "<b>%s:</b> <span style='color:%s; font-size:14px'>RM %.2f</span>"
                    + paymentMethodInfo
                    + "</html>",
                    title, plateNumber, entryTime.format(fmt), exitTime.format(fmt),
                    hours, displayRate, okuLabel, parkingFee, fines, totalLabel, color, totalDue
            );
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return "Vehicle not found.";
}

    // process payment
    // payment method and amount
    public boolean processPayment(String plateNumber, String paymentMethod, double amount) {
        Connection conn = DbConnection.getInstance().getConnection();
        try {
            // get Ticket ID
            String findSql = "SELECT ticket_id FROM tickets WHERE plate_number = ? AND status = 'ACTIVE'";
            PreparedStatement findPstmt = conn.prepareStatement(findSql);
            findPstmt.setString(1, plateNumber);
            ResultSet rs = findPstmt.executeQuery();

            if (rs.next()) {
                String ticketId = rs.getString("ticket_id");

                // inset Payment record
                String insertPay = "INSERT INTO payments (ticket_id, total_amount, payment_method) VALUES (?, ?, ?)";
                PreparedStatement pStmt = conn.prepareStatement(insertPay);
                pStmt.setString(1, ticketId);
                pStmt.setDouble(2, amount);
                pStmt.setString(3, paymentMethod);
                pStmt.executeUpdate();

                // update Ticket status
                String updateTicket = "UPDATE tickets SET status = 'PAID', exit_time = ?, fee_amount = ? WHERE ticket_id = ?";
                PreparedStatement tStmt = conn.prepareStatement(updateTicket);
                tStmt.setString(1, LocalDateTime.now().toString());
                tStmt.setDouble(2, amount);
                tStmt.setString(3, ticketId);
                tStmt.executeUpdate();

                // fine
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

    // calculate bedore facade
   public double calculateCurrentFee(String plate, ParkingLot parkingLot) {
    Connection conn = DbConnection.getInstance().getConnection();
    try {
       String sql = "SELECT * FROM tickets WHERE plate_number = ? AND status = 'ACTIVE' ORDER BY entry_time DESC LIMIT 1";
        
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, plate);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            LocalDateTime entryTime = LocalDateTime.parse(rs.getString("entry_time"));
            String type = rs.getString("vehicle_type");
            String spotId = rs.getString("spot_id");
            //Retrieve the handicapped status from the database
            boolean isHandicapped = rs.getInt("is_handicapped") == 1;

            Vehicle vehicle = Vehicle.create(plate, type);
            ParkingSpot spot = parkingLot.getSpotById(spotId);
            
            // Pass the 'isHandicapped' boolean to the calculator
            return FeeCalculator.calculate(vehicle, spot, entryTime, isHandicapped);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return 0.0;
}

    public String getSpotIdByPlate(String plateNumber) {
        Connection conn = DbConnection.getInstance().getConnection();
        try {
            String sql = "SELECT spot_id FROM tickets WHERE plate_number = ? AND status = 'ACTIVE'";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, plateNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("spot_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getPaymentMethod(String ticketId) {
        Connection conn = DbConnection.getInstance().getConnection();
        try {
            String sql = "SELECT payment_method FROM payments WHERE ticket_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, ticketId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("payment_method");
            }
        } catch (Exception e) {
        }
        return "CASH";
    }
}
