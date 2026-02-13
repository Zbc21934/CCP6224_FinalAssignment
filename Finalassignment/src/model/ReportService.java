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

    public ReportService(Connection conn, ParkingLot parkingLot) {
        this.conn = conn;
        this.parkingLot = parkingLot;
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
            String sql = "SELECT SUM(total_amount) AS total_rev FROM payments";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            double total = rs.next() ? rs.getDouble("total_rev") : 0.0;
            return String.format("--- REVENUE ---\nTotal Collections: RM %.2f", total);
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    public String getActiveVehicleTable() {
        StringBuilder sb = new StringBuilder("--- CURRENT VEHICLES ---\n");
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
        return sb.toString();
    }

    public String getFineReport() {
        StringBuilder sb = new StringBuilder("--- ⚠️ OUTSTANDING FINES (UNPAID) ---\n");
        sb.append(String.format("%-15s %-10s %-20s\n", "Plate No.", "Amount", "Reason"));
        sb.append("--------------------------------------------------\n");

        try {
            String sql = "SELECT plate_number, amount, reason FROM fines WHERE status = 'UNPAID'";
            ResultSet rs = conn.createStatement().executeQuery(sql);

            boolean hasFines = false;
            while (rs.next()) {
                hasFines = true;
                sb.append(String.format("%-15s RM %-7.2f %-20s\n",
                        rs.getString("plate_number"),
                        rs.getDouble("amount"),
                        rs.getString("reason")));
            }

            if (!hasFines) {
                sb.append("No outstanding fines found. Everyone is being good! 😊\n");
            }
        } catch (SQLException e) {
            sb.append("Error fetching fine data: ").append(e.getMessage());
        }
        return sb.toString();
    }
}
