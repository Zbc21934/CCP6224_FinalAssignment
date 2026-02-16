/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author PatrickToh
 */
import java.sql.*;

public class ReportService {

    private Connection conn;
    private ParkingLot parkingLot;
    private FineManager fineManager;
    
    public String getReportByType(String type) {
    switch (type.toUpperCase()) {
        case "OCCUPANCY": return getOccupancySummary();
        case "REVENUE":   return getRevenueSummary();
        case "VEHICLES":  return getActiveVehicleTable();
        case "FINES":     return getFineReport();
        default:          return "Unknown Report Type";
    }
}
    public ReportService(Connection conn, ParkingLot parkingLot, FineManager fineManager) {
        this.conn = conn;
        this.parkingLot = parkingLot;
        this.fineManager = fineManager;
    }

    public String getOccupancySummary() {
        try {
            String sql = "SELECT COUNT(*) AS total FROM tickets WHERE status = 'ACTIVE'";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            int occupied = rs.next() ? rs.getInt("total") : 0;

            int totalCapacity = parkingLot.getTotalCapacity();
            int available = totalCapacity - occupied;
            double usage = (double) occupied / totalCapacity * 100;

            StringBuilder sb = new StringBuilder();
            sb.append("------------------------------------------\n");
            sb.append("             --- OCCUPANCY ---            \n");
            sb.append("------------------------------------------\n");
            sb.append(String.format("%-18s: %d\n", "Total Capacity", totalCapacity));
            sb.append(String.format("%-18s: %d\n", "Occupied", occupied));
            sb.append(String.format("%-18s: %d\n", "Available", available));
            sb.append(String.format("%-18s: %.2f%%\n", "Usage Rate", usage));
            sb.append("------------------------------------------\n");

            return sb.toString();
        } catch (Exception e) {
            return "Error generating occupancy report: " + e.getMessage();
        }
    }

    public String getRevenueSummary() {
    try {
        Statement stmt = conn.createStatement();
        
        String revSql = "SELECT SUM(total_amount) AS total, COUNT(*) AS count FROM payments";
        ResultSet rsRev = stmt.executeQuery(revSql);
        double totalRevenue = 0.0;
        int transactionCount = 0;
        if (rsRev.next()) {
            totalRevenue = rsRev.getDouble("total");
            transactionCount = rsRev.getInt("count");
        }

        double avgRevenue = (transactionCount > 0) ? totalRevenue / transactionCount : 0.0;
        StringBuilder sb = new StringBuilder();
        sb.append("------------------------------------------\n");
        sb.append("             --- REVENUE ---              \n");
        sb.append("------------------------------------------\n");
        sb.append(String.format("%-18s: RM %.2f\n", "Total Collected", totalRevenue));
        sb.append(String.format("%-18s: %d\n", "Total Transactions", transactionCount));
        sb.append(String.format("%-18s: RM %.2f\n", "Avg per Vehicle", avgRevenue));
        sb.append("------------------------------------------\n");
        sb.append("\nPayment Methods:\n");
        String methodSql = "SELECT payment_method, SUM(total_amount) as amt FROM payments GROUP BY payment_method";
        ResultSet rsMethod = stmt.executeQuery(methodSql);
        while (rsMethod.next()) {
            sb.append(String.format("- %-12s: RM %.2f\n", 
                    rsMethod.getString("payment_method"), rsMethod.getDouble("amt")));
        }
        
        sb.append("------------------------------------------\n");
        
        return sb.toString();
    } catch (SQLException e) {
        return "Error generating revenue report: " + e.getMessage();
    }
}

    public String getActiveVehicleTable() {        
        StringBuilder sb = new StringBuilder();
        sb.append("------------------------------------------\n");
        sb.append("         --- Current Vehicle ---          \n");
        sb.append("------------------------------------------\n");
        sb.append(String.format("%-15s %-12s %-10s\n", "Plate", "Type", "Spot"));
        sb.append("------------------------------------------\n");
        try {
            String sql = "SELECT plate_number, vehicle_type, spot_id FROM tickets WHERE status = 'ACTIVE'";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                sb.append(String.format("%-15s %-12s %-10s\n",
                        rs.getString("plate_number"), rs.getString("vehicle_type"), rs.getString("spot_id")));
            }
        } catch (SQLException e) {
            sb.append("Error: ").append(e.getMessage());
        }
        
        sb.append("------------------------------------------\n");
        return sb.toString();
    }
    public String getFineReport() {
        StringBuilder sb = new StringBuilder();
        // Define table alignment format
        String headerFormat = "%-15s | %-12s | %-25s | %-10s\n";
        String rowFormat    = "%-15s | RM %-9.2f | %-25s | %-10s\n";
        String line = "--------------------------------------------------------------------------------\n";

        sb.append("================================================================================\n");
        sb.append("                         📊 PARKING FINE SYSTEM REPORT                          \n");
        sb.append("================================================================================\n\n");

        // --- Part 1: UNPAID FINES (Static DB records + Real-time dynamic fines) ---
        sb.append("[ SECTION 1: UNPAID FINES ]\n");
        sb.append(line);
        sb.append(String.format(headerFormat, "Plate Number", "Amount", "Reason/Details", "Status"));
        sb.append(line);

        boolean hasUnpaid = false;
        // Set to track processed plates to avoid duplicate entries
        java.util.Set<String> processedPlates = new java.util.HashSet<>();

        // 1.1 Fetch fines already recorded in DB with 'UNPAID' status (e.g., Entry Violation)
        try {
            String sql = "SELECT plate_number, amount, reason FROM fines WHERE status = 'UNPAID'";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                hasUnpaid = true;
                String plate = rs.getString("plate_number");
                processedPlates.add(plate);
                sb.append(String.format(rowFormat,
                        plate,
                        rs.getDouble("amount"),
                        rs.getString("reason"),
                        "UNPAID"));
            }
        } catch (SQLException e) {
            sb.append("DB Error: " + e.getMessage() + "\n");
        }

        // 1.2 Real-time calculation for active vehicles in the lot
        try {
            String sql = "SELECT plate_number, entry_time, is_violation FROM tickets WHERE status = 'ACTIVE'";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                String plate = rs.getString("plate_number");
                
                // Skip if this plate was already listed in the static fines section
                if (processedPlates.contains(plate)) continue;

                java.time.LocalDateTime entryTime = java.time.LocalDateTime.parse(rs.getString("entry_time"));
                boolean isViolation = rs.getInt("is_violation") == 1;

                long hours = FeeCalculator.getDurationInHours(entryTime);
                // Core: Use fineManager to calculate the current fine amount dynamically
                double dynamicFine = fineManager.calculateFine(hours, isViolation);

                // Only display in the report if an actual fine exists (Amount > 0)
                if (dynamicFine > 0) {
                    hasUnpaid = true;
                    
                    String reason;
                    if (hours > 24) {
                        // Any fine triggered after 24 hours is labeled as Overtime
                        reason = "Overtime (" + hours + "h)";
                    } else {
                        // Fines triggered within 24 hours are labeled as Reserved Misuse
                        reason = "Reserved Misuse";
                    }

                    sb.append(String.format(rowFormat, plate, dynamicFine, reason, "UNPAID"));
                }
            }
        } catch (Exception e) {
            sb.append("❌ Calculation Error: " + e.getMessage() + "\n");
        }

        if (!hasUnpaid) sb.append("No active unpaid fines found.\n");
        sb.append(line + "\n\n");

        // --- Part 2: PAID FINES (Historical records) ---
        sb.append("[ SECTION 2: PAID FINES HISTORY ]\n");
        sb.append(line);
        sb.append(String.format(headerFormat, "Plate Number", "Amount", "Reason/Details", "Status"));
        sb.append(line);

        boolean hasPaid = false;
        try {
            String sql = "SELECT plate_number, amount, reason FROM fines WHERE status = 'PAID'";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                hasPaid = true;
                sb.append(String.format(rowFormat,
                        rs.getString("plate_number"),
                        rs.getDouble("amount"),
                        rs.getString("reason"),
                        "PAID"));
            }
        } catch (SQLException e) {
            sb.append("DB Error: " + e.getMessage() + "\n");
        }

        if (!hasPaid) sb.append("No payment history found.\n");
        
        sb.append(line);
        sb.append("Report Generated: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\n");
        sb.append("================================================================================\n");

        return sb.toString();
    }
}
