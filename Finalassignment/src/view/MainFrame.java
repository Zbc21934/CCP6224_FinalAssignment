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
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Tab 1: Visual View
        JPanel visualPanel = createVisualPanel();
        tabbedPane.addTab("Parking Lot View", visualPanel);

        // Tab 2: Admin Panel
        AdminPanel adminPanel = new AdminPanel(parkingSystem);
        tabbedPane.addTab("Admin Panel", adminPanel);
        add(tabbedPane);
        tabbedPane.addChangeListener(e -> {
            //  Admin Panel (Index 1)
            if (tabbedPane.getSelectedIndex() == 1) {
                // NOT login
                if (!adminPanel.isAuthenticated()) {
                    // SwingUtilities ，poppup smooth
                    SwingUtilities.invokeLater(() -> {
                        boolean success = adminPanel.triggerLogin();
                        if (!success) {
                            tabbedPane.setSelectedIndex(0);
                        }
                    });
                }
            }
        });

        // Load first floor
        if (!parkingSystem.getParkingLot().getFloors().isEmpty()) {
            loadFloor(parkingSystem.getParkingLot().getFloors().get(0));
        }
    }

    private JPanel createVisualPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);

        // --- Top Bar: Title and Controls ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(245, 245, 245));
        topBar.setBorder(new EmptyBorder(15, 20, 15, 20));

        // 1. Left Side: Header Title
        currentFloorLabel = new JLabel("Please Select a Floor");
        currentFloorLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        currentFloorLabel.setForeground(new Color(52, 152, 219));
        topBar.add(currentFloorLabel, BorderLayout.WEST);

        // 2. Right Side: Button Area
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        // --- Add Floor Selection Buttons ---
        List<Floor> floors = parkingSystem.getParkingLot().getFloors();
        for (Floor floor : floors) {
            JButton floorBtn = createStyledButton(floor.getFloorID(), Color.WHITE, Color.BLACK);
            floorBtn.addActionListener(e -> loadFloor(floor));
            actionPanel.add(floorBtn);
        }

        // Add visual spacing
        actionPanel.add(Box.createHorizontalStrut(20));

        // --- Rules Button ---
        JButton rulesBtn = createStyledButton("<html><b>i<b>", new Color(52, 152, 219), Color.WHITE);
        rulesBtn.addActionListener(e -> showVehicleRules());
        actionPanel.add(rulesBtn);

        actionPanel.add(Box.createHorizontalStrut(10));

        // --- Add Exit Button (Orange Theme) ---
        JButton exitBtn = createStyledButton("Vehicle Exit (Enter Plate)", new Color(243, 156, 18), Color.WHITE);
        exitBtn.addActionListener(e -> {
            String plate = JOptionPane.showInputDialog(this, "Enter License Plate Number:");
            if (plate != null && !plate.trim().isEmpty()) {
                performCheckOut(plate.trim());
            }
        });
        actionPanel.add(exitBtn);

        topBar.add(actionPanel, BorderLayout.EAST);

        // --- Center Area: Parking Spots Grid ---
        spotsPanel = new JPanel();
        spotsPanel.setBackground(Color.WHITE);
        spotsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        JScrollPane scrollPane = new JScrollPane(spotsPanel);
        scrollPane.setBorder(null);

        container.add(topBar, BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);

        return container;
    }

    private void loadFloor(Floor floor) {
        currentFloorLabel.setText("Viewing: " + floor.getFloorID());
        spotsPanel.removeAll();

        // 1. Set Grid to 5 Columns
        spotsPanel.setLayout(new GridLayout(0, 5, 15, 15));

        for (ParkingSpot spot : floor.getSpots()) {
            JButton spotBtn = new JButton();

            String spotType = spot.getClass().getSimpleName().replace("Spot", "");
            String btnText = "<html><center><b>" + spot.getSpotID() + "</b><br/>"
                    + spotType + "</center></html>";
            spotBtn.setText(btnText);
            spotBtn.setPreferredSize(new Dimension(140, 80));
            spotBtn.setFont(new Font("Arial", Font.PLAIN, 12));

            if (spot.isOccupied()) {
                spotBtn.setBackground(new Color(231, 76, 60)); // Red
                spotBtn.setForeground(Color.WHITE);
            } else {
                spotBtn.setBackground(new Color(46, 204, 113)); // Green
                spotBtn.setForeground(Color.BLACK);
            }

            spotBtn.addActionListener(e -> showSpotDetails(spot));
            spotsPanel.add(spotBtn);
        }

        spotsPanel.revalidate();
        spotsPanel.repaint();
    }

    private void showSpotDetails(ParkingSpot spot) {
        // 1. If occupied, show dialog
        if (spot.isOccupied()) {
            Vehicle v = spot.getVehicle();
            String plate = (v != null) ? v.getLicensePlate() : "Unknown";

            String message = "<html><b>Spot ID:</b> " + spot.getSpotID() + "<br>"
                    + "<b>Status:</b> Occupied<br>"
                    + "<b>Plate:</b> " + plate + "<br>"
                    + "<b>Type:</b> " + spot.getClass().getSimpleName().replace("Spot", "") + "<br>"
                    + "<b>Rate:</b> RM " + spot.getHourlyRate() + "/hrs</html>";

            Object[] options = {"Cancel"};
            JOptionPane.showOptionDialog(this, message, "Spot Occupied",
                    JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
            return;
        }

        // 2. If empty, show menu
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Spot ID: " + spot.getSpotID() + " (" + spot.getClass().getSimpleName() + ")"));

        JTextField plateField = new JTextField();
        panel.add(new JLabel("Enter License Plate:"));
        panel.add(plateField);

        String[] vehicleTypes = {"Car", "Motorcycle", "SUV", "Handicapped"};
        JComboBox<String> typeBox = new JComboBox<>(vehicleTypes);
        panel.add(new JLabel("Select Vehicle Type:"));
        panel.add(typeBox);

        // Handicapped check box
        JCheckBox handicappedCheckBox = new JCheckBox("Handicapped Card Holder?");
        panel.add(handicappedCheckBox);

        // 3. Confirm Dialog
        int result = JOptionPane.showConfirmDialog(null, panel, "Park Vehicle",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String plate = plateField.getText().trim();
            String type = (String) typeBox.getSelectedItem();
            boolean isHandicapped = handicappedCheckBox.isSelected();

            if (plate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "License plate cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // ==========================================================
            // ✅ RESERVATION VERIFICATION LOGIC
            // ==========================================================
            boolean hasReservation = false;

            // Check if the spot is a ReservedSpot
            if (spot instanceof model.ReservedSpot) {

                //Check if the is Vehicle is HandicappedVehicle
                boolean isPrivileged = type.equalsIgnoreCase("Handicapped") || isHandicapped;
                
                //if not Handicappedvehicle
                if (!isPrivileged) {
                    // Prompt user for Reservation ID
                    String resId = JOptionPane.showInputDialog(this,
                            "This is a Reserved Spot.\nPlease enter your Reservation ID:",
                            "Reservation Check",
                            JOptionPane.QUESTION_MESSAGE);

                    // Validate ID via Facade
                    boolean isValid = parkingSystem.validateReservation(resId);
                    hasReservation = isValid;

                    // Warn user if invalid ID but allow entry (Violation Logic)
                    if (!isValid) {
                        int choice = JOptionPane.showConfirmDialog(this,
                                "Invalid or missing Reservation ID!\nParking here will result in a FINE.\nDo you still want to park?",
                                "Warning: Violation",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);

                        if (choice != JOptionPane.YES_OPTION) {
                            return; // User cancelled
                        }
                        // User proceeded -> 'hasReservation' remains false, which Facade will flag as Violation
                    }
                } //else : the Handicapped Vehicle can skip all the step, no asking for reservation ID, no warning
            }

            // ==========================================================
            // CALL BACKEND (Updated: Passing hasReservation)
            // ==========================================================
            Ticket ticket = parkingSystem.parkVehicle(plate, type, spot.getSpotID(), isHandicapped, hasReservation);

            if (ticket != null) {
                String ticketMsg = "<html>"
                        + "<h3>✅ Entry Success!</h3>"
                        + "<b>Ticket ID:</b> " + ticket.getTicketId() + "<br>"
                        + "<b>Spot:</b> " + ticket.getSpotId() + "<br>"
                        + "<b>Entry Time:</b> " + ticket.getFormattedEntryTime() + "<br>"
                        + "</html>";
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

    private void showVehicleRules() {
        String rulesHtml = model.VehicleRules.getRulesHtml();
        JOptionPane.showMessageDialog(this, rulesHtml, "Parking Lot Rules", JOptionPane.INFORMATION_MESSAGE);
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        return btn;
    }
}
