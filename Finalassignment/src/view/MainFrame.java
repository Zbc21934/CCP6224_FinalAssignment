package view;

import controller.ParkingSystemFacade;
import model.Floor;
import model.ParkingSpot;
import model.Vehicle;
import model.Ticket;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import javax.swing.border.*;

public class MainFrame extends JFrame {

    private ParkingSystemFacade parkingSystem;
    private JPanel spotsPanel;
    private JLabel currentFloorLabel;

    public MainFrame() {
        // Initialize the Backend
        parkingSystem = new ParkingSystemFacade();

        // Setup Window
        setTitle("University Parking Management System");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Tab 1: Visual View
        JPanel visualPanel = createVisualPanel();
        tabbedPane.addTab("Parking Lot View", visualPanel);

        // Tab 2: Reports
        JPanel reportPanel = createReportPanel();
        tabbedPane.addTab("Reports", reportPanel);

        add(tabbedPane);

        // Load first floor
        if (!parkingSystem.getParkingLot().getFloors().isEmpty()) {
            loadFloor(parkingSystem.getParkingLot().getFloors().get(0));
        }
    }

   private JPanel createVisualPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE); // Set main container background to white

        // --- Top Bar: Title and Controls ---
        // Consolidating floor selection and title at the top for a modern look
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(245, 245, 245)); // Light gray background for contrast
        topBar.setBorder(new EmptyBorder(15, 20, 15, 20)); // Add padding around the bar

        // 1. Left Side: Header Title
        currentFloorLabel = new JLabel("Please Select a Floor");
        currentFloorLabel.setFont(new Font("Segoe UI", Font.BOLD, 24)); // Larger, modern font
        currentFloorLabel.setForeground(new Color(52, 152, 219)); // Primary blue color
        topBar.add(currentFloorLabel, BorderLayout.WEST);

        // 2. Right Side: Button Area (Floors + Exit)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false); // Make panel transparent to show topBar color

        // --- Add Floor Selection Buttons ---
        List<Floor> floors = parkingSystem.getParkingLot().getFloors();
        for (Floor floor : floors) {
            // Call the helper method to create styled buttons
            JButton floorBtn = createStyledButton(floor.getFloorID(), Color.WHITE, Color.BLACK);
            floorBtn.addActionListener(e -> loadFloor(floor));
            actionPanel.add(floorBtn);
        }

        // Add visual spacing
        actionPanel.add(Box.createHorizontalStrut(20));

        // --- Add Exit Button (Orange Theme) ---
        JButton exitBtn = createStyledButton("Vehicle Exit (Enter Plate)", new Color(243, 156, 18), Color.WHITE);
        exitBtn.addActionListener(e -> {
            String plate = JOptionPane.showInputDialog(this, "Enter License Plate Number:");
            if (plate != null && !plate.trim().isEmpty()) {
                performCheckOut(plate.trim());
            }
        });
        actionPanel.add(exitBtn);

        // Position the action panel on the right side of the Top Bar
        topBar.add(actionPanel, BorderLayout.EAST);

        // --- Center Area: Parking Spots Grid ---
        spotsPanel = new JPanel();
        spotsPanel.setBackground(Color.WHITE);
        spotsPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // Margin for the grid
        JScrollPane scrollPane = new JScrollPane(spotsPanel);
        scrollPane.setBorder(null); // Remove default border for a cleaner look

        // Assemble the final container
        container.add(topBar, BorderLayout.NORTH); // Controls at the top
        container.add(scrollPane, BorderLayout.CENTER); // Grid in the middle
        
        // Note: BorderLayout.SOUTH is empty as controls are moved to the top

        return container;
    }

private void loadFloor(Floor floor) {
        currentFloorLabel.setText("Viewing: " + floor.getFloorID());
        spotsPanel.removeAll();

        // 1. Set Grid to 5 Columns (matches your 5 spots per row)
        spotsPanel.setLayout(new GridLayout(0, 5, 15, 15));

        for (ParkingSpot spot : floor.getSpots()) {
            JButton spotBtn = new JButton();

            // Get simple name (e.g., "Compact" instead of "CompactSpot")
            String spotType = spot.getClass().getSimpleName().replace("Spot", "");
            
            // HTML for multi-line button text
            String btnText = "<html><center><b>" + spot.getSpotID() + "</b><br/>"
                    + spotType + "</center></html>";
            spotBtn.setText(btnText);

            // 2. Adjust button size slightly to look good in the new grid
            spotBtn.setPreferredSize(new Dimension(140, 80));
            spotBtn.setFont(new Font("Arial", Font.PLAIN, 12));

            // Color Logic
            if (spot.isOccupied()) {
                spotBtn.setBackground(new Color(231, 76, 60)); // Red (Occupied)
                spotBtn.setForeground(Color.WHITE);
            } else {
                spotBtn.setBackground(new Color(46, 204, 113)); // Green (Available)
                spotBtn.setForeground(Color.BLACK);
            }

            // Add click listener
            spotBtn.addActionListener(e -> showSpotDetails(spot));
            spotsPanel.add(spotBtn);
        }

        spotsPanel.revalidate();
        spotsPanel.repaint();
    }

    private void showSpotDetails(ParkingSpot spot) {
        // 1. if occupied, show dialog
        if (spot.isOccupied()) {
            Vehicle v = spot.getVehicle();
            String plate = (v != null) ? v.getLicensePlate() : "Unknown";

            String message = "<html><b>Spot ID:</b> " + spot.getSpotID() + "<br>"
                    + "<b>Status:</b> Occupied<br>"
                    + "<b>Plate:</b> " + plate + "<br>"
                    + "<b>Type:</b> " + spot.getClass().getSimpleName().replace("Spot", "") + "<br>"
                    + "<b>Rate:</b> RM " + spot.getHourlyRate() + "/hrs</html>";

            Object[] options = {"Cancel"};

            int choice = JOptionPane.showOptionDialog(this,
                    message,
                    "Spot Occupied",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);
            return;
        }
        // 2. if empty, show menu
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Spot ID: " + spot.getSpotID() + " (" + spot.getClass().getSimpleName() + ")"));

        JTextField plateField = new JTextField();
        panel.add(new JLabel("Enter License Plate:"));
        panel.add(plateField);

        String[] vehicleTypes = {"Car", "Motorcycle", "SUV", "Handicapped"};
        JComboBox<String> typeBox = new JComboBox<>(vehicleTypes);
        panel.add(new JLabel("Select Vehicle Type:"));
        panel.add(typeBox);
        
        //handicapped check box
        JCheckBox handicappedCheckBox = new JCheckBox("Handicapped Card Holder?");
        panel.add(handicappedCheckBox);
        
        // 3. confirmDialog (OK / Cancel)
        int result = JOptionPane.showConfirmDialog(null, panel, "Park Vehicle",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // 4. if OK
        if (result == JOptionPane.OK_OPTION) {
            String plate = plateField.getText().trim();
            String type = (String) typeBox.getSelectedItem();
            boolean isHandicapped = handicappedCheckBox.isSelected();
            
            if (plate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "License plate cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Ticket ticket = parkingSystem.parkVehicle(plate, type, spot.getSpotID(), isHandicapped);
            
            if (ticket != null) {
                String ticketMsg = "<html>"
                        + "<h3>✅ Entry Success!</h3>"
                        + "<b>Ticket ID:</b> " + ticket.getTicketId() + "<br>"
                        + "<b>Spot:</b> " + ticket.getSpotId() + "<br>"
                        + "<b>Entry Time:</b> " + ticket.getFormattedEntryTime() + "<br>"
                        + 
                        "</html>";
                JOptionPane.showMessageDialog(this, ticketMsg, "Parking Ticket", JOptionPane.INFORMATION_MESSAGE);

                loadFloor(parkingSystem.getParkingLot().getFloors().get(0));
            } else {
                JOptionPane.showMessageDialog(this, "Parking Failed! Please check vehicle rules.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void performCheckOut(String plateNumber) {
        String bill = parkingSystem.checkOutVehicle(plateNumber);

        if (bill == null || bill.equals("Vehicle not found.")) {
            JOptionPane.showMessageDialog(this, "Vehicle not found or already paid!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        PaymentDialog payDialog = new PaymentDialog(this, bill);
        payDialog.setVisible(true);
       String method = payDialog.getSelectedMethod();

        if (method != null) { 
            boolean paid = parkingSystem.processPayment(plateNumber, method);

            if (paid) {
                String finalReceipt = parkingSystem.generateOfficialReceipt(plateNumber);
                finalReceipt += "<br><center>Thank you for parking with us!</center>";

                new ReceiptDialog(this, finalReceipt).setVisible(true);

                if (!parkingSystem.getParkingLot().getFloors().isEmpty()) {
                    loadFloor(parkingSystem.getParkingLot().getFloors().get(0));
                }
            } else {
                JOptionPane.showMessageDialog(this, "Payment Failed!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    
   private JPanel createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Selection Row
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        navBar.setBackground(new Color(240, 240, 240));

        // Report Button
        JButton btnOcc = new JButton("Occupancy Report ");
        JButton btnRev = new JButton("Revenue Report ");
        JButton btnVeh = new JButton("Currently Vehicles in the Lot ");
        JButton btnFine = new JButton("Fine Report");
        navBar.add(btnOcc);
        navBar.add(btnRev);
        navBar.add(btnVeh);
        navBar.add(btnFine);
        
        // text areaa
        JTextArea reportArea = new JTextArea();
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
        reportArea.setEditable(false);
        reportArea.setMargin(new Insets(20, 20, 20, 20));
        JScrollPane scrollPane = new JScrollPane(reportArea);

        // button logic call controller
        btnOcc.addActionListener(e -> reportArea.setText(parkingSystem.getDashboardReport("OCCUPANCY")));
        btnRev.addActionListener(e -> reportArea.setText(parkingSystem.getDashboardReport("REVENUE")));
        btnVeh.addActionListener(e -> reportArea.setText(parkingSystem.getDashboardReport("VEHICLES")));
        panel.add(navBar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Create a styled button
    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false); // Remove focus border (dotted line) on click

        // Add a compound border (thin line + padding) for a refined look
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1), // Light gray border
                BorderFactory.createEmptyBorder(8, 15, 8, 15) // Inner text padding
        ));
        return btn;
    }
}
