package view; 

import controller.ParkingSystemFacade;
import model.Floor;
import model.ParkingSpot;
import javax.swing.*;
import java.awt.*;
import java.util.List;


public class MainFrame extends JFrame {
    private ParkingSystemFacade parkingSystem;
    private JPanel spotsPanel;
    private JLabel currentFloorLabel;

    public MainFrame() {
        // Initialize the Backend
        parkingSystem = new ParkingSystemFacade();

        // Setup Window
        setTitle("University Parking Management System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Tabs
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: Visual View
        JPanel visualPanel = createVisualPanel();
        tabbedPane.addTab("Parking Lot View", visualPanel);

        // Tab 2: Reports
        JPanel reportPanel = new JPanel();
        reportPanel.add(new JLabel("Reports & Revenue Panel"));
        tabbedPane.addTab("Reports", reportPanel);

        add(tabbedPane);
        
        // Load first floor
        if (!parkingSystem.getParkingLot().getFloors().isEmpty()) {
            loadFloor(parkingSystem.getParkingLot().getFloors().get(0));
        }
    }

    private JPanel createVisualPanel() {
        JPanel container = new JPanel(new BorderLayout());

        // Top Bar: Floor Selection
        JPanel floorSelectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        floorSelectionPanel.setBorder(BorderFactory.createTitledBorder("Select Floor"));
        
        List<Floor> floors = parkingSystem.getParkingLot().getFloors();
        for (Floor floor : floors) {
            JButton floorBtn = new JButton(floor.getFloorID());
            floorBtn.setFont(new Font("Arial", Font.BOLD, 14));
            floorBtn.addActionListener(e -> loadFloor(floor));
            floorSelectionPanel.add(floorBtn);
        }

        // Center: Grid
        spotsPanel = new JPanel();
        spotsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JScrollPane scrollPane = new JScrollPane(spotsPanel);

        // Header
        currentFloorLabel = new JLabel("Please Select a Floor");
        currentFloorLabel.setFont(new Font("Arial", Font.BOLD, 18));
        currentFloorLabel.setHorizontalAlignment(SwingConstants.CENTER);

        container.add(currentFloorLabel, BorderLayout.NORTH);
        container.add(floorSelectionPanel, BorderLayout.SOUTH);
        container.add(scrollPane, BorderLayout.CENTER);

        return container;
    }

    private void loadFloor(Floor floor) {
        currentFloorLabel.setText("Viewing: " + floor.getFloorID());
        spotsPanel.removeAll();

        // 5 columns grid
        spotsPanel.setLayout(new GridLayout(0, 5, 15, 15));

        for (ParkingSpot spot : floor.getSpots()) {
            JButton spotBtn = new JButton();
            
            // HTML for multi-line button text
            String btnText = "<html><center><b>" + spot.getSpotID() + "</b><br/>" 
                           + spot.getClass().getSimpleName().replace("Spot", "") + "</center></html>";
            spotBtn.setText(btnText);
            
            spotBtn.setPreferredSize(new Dimension(150, 80));
            spotBtn.setFont(new Font("Arial", Font.PLAIN, 12));
            
            if (spot.isOccupied()) {
                spotBtn.setBackground(new Color(255, 100, 100)); // Red
            } else {
                spotBtn.setBackground(new Color(144, 238, 144)); // Green
            }

            spotBtn.addActionListener(e -> showSpotDetails(spot));
            spotsPanel.add(spotBtn);
        }

        spotsPanel.revalidate();
        spotsPanel.repaint();
    }

    private void showSpotDetails(ParkingSpot spot) {
        // 1. if occupied, show dialog
        if (spot.isOccupied()) {
            String message = "Spot ID: " + spot.getSpotID() + "\n"
                    + "Status: Occupied\n"
                    + "Type: " + spot.getClass().getSimpleName() + "\n"
                    + "Hourly Rate: RM " + spot.getHourlyRate();
            JOptionPane.showMessageDialog(this, message, "Spot Occupied", JOptionPane.INFORMATION_MESSAGE);
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

        // 3. confirmDialog (OK / Cancel)
        int result = JOptionPane.showConfirmDialog(null, panel, "Park Vehicle",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // 4. if OK
        if (result == JOptionPane.OK_OPTION) {
            String plate = plateField.getText().trim();
            String type = (String) typeBox.getSelectedItem();

            if (plate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "License plate cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // --- Facade ---
            // parkVehicle use Vehicle.create() to do vertification
            boolean success = parkingSystem.parkVehicle(plate, type, spot.getSpotID());

            if (success) {
                JOptionPane.showMessageDialog(this, "Vehicle Parked Successfully!");
                loadFloor(parkingSystem.getParkingLot().getFloors().get(0)); 
            } else {
                JOptionPane.showMessageDialog(this, "Parking Failed! (Check vehicle type rules)", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}