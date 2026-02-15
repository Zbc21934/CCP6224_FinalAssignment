package model;

public class VehicleRules {

    public static String getRulesHtml() {
        return "<html><body style='width: 300px; padding: 5px;'>"
                + "<h2 style='color: #2c3e50;'>Parking Vehicle Rules</h2>"
                + "<hr>"
                + "<table border='0' cellpadding='5'>"
                
                // Motorcycle Rule
                + "<tr>"
                + "<td><b>🏍️ Motorcycle:</b></td>"
                + "<td>Compact, Reserved</td>"
                + "</tr>"
                
                // Car Rule
                + "<tr>"
                + "<td><b>🚗 Car:</b></td>"
                + "<td>Compact, Regular, Reserved</td>"
                + "</tr>"
                
                // SUV Rule
                + "<tr>"
                + "<td><b>🚙 SUV/Truck:</b></td>"
                + "<td>Regular, Reserved</td>"
                + "</tr>"
                
                // Handicapped Rule
                + "<tr>"
                + "<td><b>♿ Handicapped:</b></td>"
                + "<td><span style='color:green;'>ALL SPOTS</span> (Free/Discounted)</td>"
                + "</tr>"
                
                + "</table>"
                + "<hr>"
                + "<i>* Reserved spots are for staff/VIPs unless specified.</i>"
                + "</body></html>";
    }
}