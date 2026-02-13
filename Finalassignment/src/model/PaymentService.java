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
            // if paid，check status；unpaid check active status
            String status = isPaid ? "PAID" : "ACTIVE";
String sql = "SELECT * FROM tickets WHERE plate_number = ? AND status = ? ORDER BY entry_time DESC LIMIT 1";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, plateNumber);
            pstmt.setString(2, status);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String ticketId = rs.getString("ticket_id");
                LocalDateTime entryTime = LocalDateTime.parse(rs.getString("entry_time"));
                LocalDateTime exitTime = LocalDateTime.now();

                if (isPaid && rs.getString("exit_time") != null) {
                    try {
                        exitTime = LocalDateTime.parse(rs.getString("exit_time"));
                    } catch (Exception e) {
                    } // use current time
                }

                String type = rs.getString("vehicle_type");
                String spotId = rs.getString("spot_id");

                // restore to calculate
                Vehicle vehicle = Vehicle.create(plateNumber, type);
                ParkingSpot spot = parkingLot.getSpotById(spotId);

                // FeeCalculator
                double parkingFee = FeeCalculator.calculate(vehicle, spot, entryTime);
                long hours = FeeCalculator.getDurationInHours(entryTime);
                double rate = (spot != null) ? spot.getHourlyRate() : 5.0;
                double fines = 0.00;//fine
                double totalDue = parkingFee + fines;

                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                String title = isPaid ? "OFFICIAL RECEIPT" : "Parking Bill  (UNPAID)";
                String totalLabel = isPaid ? "AMOUNT PAID" : "TOTAL DUE";
                String color = isPaid ? "green" : "blue";

                String paymentMethodInfo = "";
                if (isPaid) {
                    String method = getPaymentMethod(ticketId);
                    title = "OFFICIAL RECEIPT <span style='color:green; border:2px solid green; padding:3px; border-radius:5px;'>✅ PAID</span>";
                    totalLabel = "AMOUNT PAID";
                    color = "green";
                    paymentMethodInfo = "<br><b>Payment Method:</b> " + method;
                }

                return String.format(
                        "<html>"
                        + "<div style='text-align: center;'><b>--- %s ---</b></div><br>"
                        + "<b>License Plate:</b> %s<br>"
                        + "<b>Entry Time:</b> %s<br>"
                        + "<b>Exit Time:</b> %s<br>"
                        + "------------------------------<br>"
                        + "<b>Duration:</b> %d hours<br>"
                        + "<b>Rate Breakdown:</b> %d hrs x RM %.2f/hr<br>"
                        + "<b>Parking Fee:</b> RM %.2f<br>"
                        + "<b>Unpaid Fines:</b> RM %.2f<br>"
                        + "------------------------------<br>"
                        + "<b>%s:</b> <span style='color:%s; font-size:14px'>RM %.2f</span>"
                        + paymentMethodInfo
                        + "</html>",
                        title, plateNumber, entryTime.format(fmt), exitTime.format(fmt),
                        hours, hours, rate, parkingFee, fines, totalLabel, color, totalDue
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
            String sql = "SELECT * FROM tickets WHERE plate_number = ? AND status = 'ACTIVE'";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, plate);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                LocalDateTime entryTime = LocalDateTime.parse(rs.getString("entry_time"));
                String type = rs.getString("vehicle_type");
                String spotId = rs.getString("spot_id");

                Vehicle vehicle = Vehicle.create(plate, type);
                ParkingSpot spot = parkingLot.getSpotById(spotId);
                return FeeCalculator.calculate(vehicle, spot, entryTime);
            }
        } catch (Exception e) {
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
