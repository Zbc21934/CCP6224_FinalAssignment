/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;

/**
 *
 * @author PatrickToh
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

//  Singleton Pattern
public class DbConnection {
    
    private static DbConnection instance;
    private Connection connection;
    private static final String URL = "jdbc:sqlite:parking_lot.db"; 

    private DbConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(URL);
            System.out.println("Database connected successfully!");

            createTables();
            
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }

    public static DbConnection getInstance() {
        try {
            if (instance == null || instance.getConnection().isClosed()) {
                instance = new DbConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void createTables() {
        try (Statement stmt = this.connection.createStatement()) {
            
            // ---------------------------------------------------------
            // PARKING SPOTS
            // ---------------------------------------------------------
            String sqlSpots = "CREATE TABLE IF NOT EXISTS parking_spots ("
                    + "spot_id TEXT PRIMARY KEY, "       // 'F1-R1-S1'
                    + "floor_level INTEGER NOT NULL, "  
                    + "spot_type TEXT NOT NULL, "        // 'Compact', 'Regular', 'Handicapped', 'Reserved'
                    + "status TEXT DEFAULT 'AVAILABLE', "// 'AVAILABLE', 'OCCUPIED', 'MAINTENANCE'
                    + "hourly_rate REAL NOT NULL"        
                    + ");";

            // ---------------------------------------------------------
            // TICKETS
            // ---------------------------------------------------------
            String sqlTickets = "CREATE TABLE IF NOT EXISTS tickets ("
                    + "ticket_id TEXT PRIMARY KEY, "    
                    + "plate_number TEXT NOT NULL, "     
                    + "vehicle_type TEXT NOT NULL, "    
                    + "spot_id TEXT NOT NULL, "          
                    + "entry_time TEXT NOT NULL, "   
                    + "exit_time TEXT, "                 
                    + "fee_amount REAL DEFAULT 0.0, "    
                    + "status TEXT DEFAULT 'ACTIVE', " 
                    + "is_handicapped INTEGER DEFAULT 0, "
                    + "is_violation INTEGER DEFAULT 0, "
                    + "FOREIGN KEY(spot_id) REFERENCES parking_spots(spot_id)"
                    + ");";

            // ---------------------------------------------------------
            // PAYMENTS
            // ---------------------------------------------------------
            String sqlPayments = "CREATE TABLE IF NOT EXISTS payments ("
                    + "payment_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "ticket_id TEXT NOT NULL, "
                    + "total_amount REAL NOT NULL, "     
                    + "payment_method TEXT NOT NULL, "   
                    + "payment_time TEXT DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY(ticket_id) REFERENCES tickets(ticket_id)"
                    + ");";

            // ---------------------------------------------------------
            // FINES
            // ---------------------------------------------------------
            String sqlFines = "CREATE TABLE IF NOT EXISTS fines ("
                    + "fine_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "plate_number TEXT NOT NULL, "     
                    + "amount REAL NOT NULL, "
                    + "reason TEXT, "                    
                    + "status TEXT DEFAULT 'UNPAID', "   
                    + "created_at TEXT DEFAULT CURRENT_TIMESTAMP"
                    + ");";

            // ---------------------------------------------------------
            // ADMIN SETTINGS
            // ---------------------------------------------------------
            String sqlSettings = "CREATE TABLE IF NOT EXISTS admin_settings ("
                    + "setting_key TEXT PRIMARY KEY, "   
                    + "setting_value TEXT NOT NULL"      
                    + ");";

            stmt.execute(sqlSpots);
            stmt.execute(sqlTickets);
            stmt.execute(sqlPayments);
            stmt.execute(sqlFines);
            stmt.execute(sqlSettings);
            
            // ---------------------------------------------------------
            // customize the dedault value(admin)
            // ---------------------------------------------------------
            String initSettings = "INSERT OR IGNORE INTO admin_settings (setting_key, setting_value) "
                                + "VALUES ('current_fine_scheme', 'SCHEME_A');";
            stmt.execute(initSettings);
            String initPassword = "INSERT OR IGNORE INTO admin_settings (setting_key, setting_value) "
                                + "VALUES ('admin_password', 'admin');";
            stmt.execute(initPassword);
            // ---------------------------------------------------------
            // RESERVATIONS (New! For verification)
            // ---------------------------------------------------------
            String sqlReservations = "CREATE TABLE IF NOT EXISTS reservations ("
                    + "res_id TEXT PRIMARY KEY, "    // 例如 'VIP-001'
                    + "status TEXT DEFAULT 'VALID'"  // VALID / USED
                    + ");";
            stmt.execute(sqlReservations);

            // ---------------------------------------------------------
            // INSERT DUMMY DATA (For testing purposes)
            // ---------------------------------------------------------
            // Insert some valid IDs for testing the GUI later
            String initRes = "INSERT OR IGNORE INTO reservations (res_id) VALUES "
                           + "('VIP-001'), "
                           + "('RES-123'), "
                           + "('STAFF-999');";
            stmt.execute(initRes);

            System.out.println("Complete Database Schema initialized successfully.");
            
        } catch (SQLException e) {
            System.out.println("Error creating tables: " + e.getMessage());
        }
    }
}
