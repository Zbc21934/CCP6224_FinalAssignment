/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import database.DbConnection;

public class FineManager {
    
    // hold the interface
    private FineScheme currentScheme;

    public FineManager() {
        //default optionA
        this.currentScheme = new FixedFine(); 
    }

    // 2. The Facade only calls this, it doesn't need to know if it's Option A, B, or C
    public double calculateFine(double totalHours, boolean isReservedMisuse) {
        return currentScheme.FineCalculation(totalHours, isReservedMisuse);
    }

    //choose option
    public void updateFineScheme(String schemeType) {
        switch (schemeType) {
            case "Option A":
                this.currentScheme = new FixedFine();
                break;
            case "Option B":
                this.currentScheme = new ProgressiveFine();
                break;
            case "Option C":
                this.currentScheme = new HourlyFine();
                break;
            default:
                System.out.println("Unknown scheme type: " + schemeType);
                return;
        }
        
        System.out.println("✅ Fine schemes updated to: " + schemeType);
        
        // save the selected scheme into db
        saveSchemeToDatabase(schemeType);
    }
    
    public FineScheme getSchemeByString(String schemeName) {
        if (schemeName == null) return new FixedFine(); // Default safety

        if (schemeName.contains("Option B") || schemeName.contains("Progressive")) {
            return new ProgressiveFine();
        } else if (schemeName.contains("Option C") || schemeName.contains("Hourly")) {
            return new HourlyFine();
        } else {
            return new FixedFine();// Default or Option A
        }
    }
    
    // 4. get scheme name(for admin panel)
    public String getCurrentSchemeName() {
        return currentScheme.getClass().getSimpleName(); // will return "FixedFine" etc.
    }

    // helper method: save the selected scheme into db
    private void saveSchemeToDatabase(String schemeType) {
        String sql = "INSERT OR REPLACE INTO admin_settings (setting_key, setting_value) VALUES ('current_fine_scheme', ?)";
        
        // ✅ Correct way: Get connection OUTSIDE the try-with-resources block
        Connection conn = DbConnection.getInstance().getConnection();
        
        // Only put PreparedStatement inside try(...) so only it gets closed, NOT the connection
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, schemeType);
            pstmt.executeUpdate();
            System.out.println("DEBUG: Scheme saved to DB without closing connection.");
        } catch (SQLException e) {
            System.out.println("❌ Failed to save setting: " + e.getMessage());
        }
    }
}