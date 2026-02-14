package model;

import database.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PaymentService {

    // ✅ New: Add FineManager to calculate fines dynamically
    private FineManager fineManager;

    public PaymentService() {
        this.fineManager = new FineManager();
        // Ideally, we should sync the scheme with the one in Facade, 
        // but for now, it defaults to Option A (or you can load from DB).
        // If Facade changes it, it might need to notify here, but let's assume default for now.
    }

    // Generate Bill (Unpaid) or Receipt (Paid)
    public String generateBillOrReceipt(String plateNumber, ParkingLot parkingLot, boolean isPaid) {
        Connection conn = DbConnection.getInstance().getConnection();
        try {
            // Logic: If asking for Receipt (isPaid=true), look for PAID ticket.
            // If asking for Bill (isPaid=false), look for ACTIVE ticket.
            String status = isPaid ? "PAID" : "ACTIVE";
            
            // ✅ Updated SQL: Fetch 'is_violation' column
            String sql = "SELECT * FROM tickets WHERE plate_number = ? AND status = ? ORDER BY entry_time DESC LIMIT 1";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, plateNumber);
            pstmt.setString(2, status);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String ticketId = rs.getString("ticket_id");
                LocalDateTime entryTime = LocalDateTime.parse(rs.getString("entry_time"));
                
                LocalDateTime exitTime = LocalDateTime.now();
                // If it's a receipt, use the actual exit time stored in DB
                if (isPaid && rs.getString("exit_time") != null) {
                    try {
                        exitTime = LocalDateTime.parse(rs.getString("exit_time"));
                    } catch (Exception e) {} 
                }

                String type = rs.getString("vehicle_type");
                String spotId = rs.getString("spot_id");
                boolean isHandicapped = rs.getInt("is_handicapped") == 1;
                
                // ✅ Retrieve Violation Status from DB
                // Default to false if column is missing (safety check), though we added it.
                boolean isViolation = false;
                try {
                    isViolation = rs.getInt("is_violation") == 1;
                } catch (SQLException e) {
                    // Ignore if column doesn't exist yet (backward compatibility)
                }

                Vehicle vehicle = Vehicle.create(plateNumber, type);
                ParkingSpot spot = parkingLot.getSpotById(spotId);
                
                // 1. Calculate Parking Fee
                double parkingFee = FeeCalculator.calculate(vehicle, spot, entryTime, isHandicapped);
                
                long hours = FeeCalculator.getDurationInHours(entryTime);

                // 2. Calculate Fines (NEW LOGIC)
                // Use FineManager to calculate fine based on duration and violation status
                double fines = fineManager.calculateFine(hours, isViolation);

                // 3. Total Due
                double totalDue = parkingFee + fines;

                // --- Formatting for Display ---
                double displayRate = (spot != null) ? spot.getHourlyRate() : 2.0;
                if (isHandicapped) {
                     if (spot instanceof model.HandicappedSpot) {
                         displayRate = 0.0; 
                     } else {
                         displayRate = 2.0;
                     }
                }

                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                String title = isPaid ? "OFFICIAL RECEIPT" : "Parking Bill (UNPAID)";
                String totalLabel = isPaid ? "AMOUNT PAID" : "TOTAL DUE";
                String color = isPaid ? "green" : "blue";
                String alertColor = "red";

                String paymentMethodInfo = "";
                if (isPaid) {
                    String method = getPaymentMethod(ticketId);
                    title += " <span style='color:green; border:1px solid green; padding:2px; border-radius:3px; font-size:10px;'>✅ PAID</span>";
                    paymentMethodInfo = "<br><b>Payment Method:</b> " + method;
                }
                
                String okuLabel = "";
                if (isHandicapped) {
                    okuLabel = "<br><span style='color:orange; font-size:10px;'>(Handicapped Rate Applied)</span>";
                }
                
                // Violation Warning Label
                String violationLabel = "";
                if (isViolation) {
                    violationLabel = "<br><span style='color:red; font-weight:bold;'>⚠️ VIOLATION: Reserved Spot Misuse</span>";
                }

                // Construct HTML Output
                return String.format(
                        "<html>"
                        + "<div style='text-align: center;'><b>--- %s ---</b></div><br>"
                        + "<b>License Plate:</b> %s<br>"
                        + "<b>Entry Time:</b> %s<br>"
                        + "<b>Exit Time:</b> %s<br>"
                        + "------------------------------<br>"
                        + "<b>Duration:</b> %d hours<br>"
                        + "<b>Rate:</b> RM %.2f/hr %s<br>" 
                        + "<b>Parking Fee:</b> RM %.2f<br>"
                        + "<b>Fines:</b> <span style='color:%s'>RM %.2f</span> %s<br>" // Added Fine Color
                        + "------------------------------<br>"
                        + "<b>%s:</b> <span style='color:%s; font-size:14px'>RM %.2f</span>"
                        + paymentMethodInfo
                        + "</html>",
                        title, plateNumber, entryTime.format(fmt), exitTime.format(fmt),
                        hours, displayRate, okuLabel, 
                        parkingFee, 
                        (fines > 0 ? "red" : "black"), fines, violationLabel, // Conditional styling for fines
                        totalLabel, color, totalDue
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Vehicle not found.";
    }

    // Process Payment and Save to DB
    public boolean processPayment(String plateNumber, String paymentMethod, double amount) {
        Connection conn = DbConnection.getInstance().getConnection();
        try {
            // Get Ticket ID
            String findSql = "SELECT ticket_id FROM tickets WHERE plate_number = ? AND status = 'ACTIVE'";
            PreparedStatement findPstmt = conn.prepareStatement(findSql);
            findPstmt.setString(1, plateNumber);
            ResultSet rs = findPstmt.executeQuery();

            if (rs.next()) {
                String ticketId = rs.getString("ticket_id");

                // Insert Payment record
                String insertPay = "INSERT INTO payments (ticket_id, total_amount, payment_method) VALUES (?, ?, ?)";
                PreparedStatement pStmt = conn.prepareStatement(insertPay);
                pStmt.setString(1, ticketId);
                pStmt.setDouble(2, amount);
                pStmt.setString(3, paymentMethod);
                pStmt.executeUpdate();

                // Update Ticket status
                String updateTicket = "UPDATE tickets SET status = 'PAID', exit_time = ?, fee_amount = ? WHERE ticket_id = ?";
                PreparedStatement tStmt = conn.prepareStatement(updateTicket);
                tStmt.setString(1, LocalDateTime.now().toString());
                tStmt.setDouble(2, amount);
                tStmt.setString(3, ticketId);
                tStmt.executeUpdate();

                // (Optional) Mark legacy fines table as paid if you use it
                // ...

                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Calculate current fee (Used by Facade before processing payment)
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
                boolean isHandicapped = rs.getInt("is_handicapped") == 1;
                
                // ✅ Get Violation Status
                boolean isViolation = false;
                try {
                    isViolation = rs.getInt("is_violation") == 1;
                } catch (SQLException e) {}

                Vehicle vehicle = Vehicle.create(plate, type);
                ParkingSpot spot = parkingLot.getSpotById(spotId);
                
                // 1. Base Fee
                double fee = FeeCalculator.calculate(vehicle, spot, entryTime, isHandicapped);
                
                // 2. Fine
                long hours = FeeCalculator.getDurationInHours(entryTime);
                double fine = fineManager.calculateFine(hours, isViolation);
                
                return fee + fine;
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