/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author PatrickToh
 */
import controller.ParkingSystemFacade;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class AdminPanel extends JPanel {

    private ParkingSystemFacade parkingSystem;
    private boolean isAuthenticated = false;

    public AdminPanel(ParkingSystemFacade parkingSystem) {
        this.parkingSystem = parkingSystem;
        setLayout(new BorderLayout());
        showLockScreen(); // Show lock screen as the initial state
    }

    // Authentication
    public boolean triggerLogin() {
        if (isAuthenticated) return true;
        
        String pwd = JOptionPane.showInputDialog(this, "Enter Admin Password:");
        if (parkingSystem.validateAdminLogin(pwd)) {
            isAuthenticated = true;
            initAdminInterface();
            return true;
        } else {
            if (pwd != null) {
                JOptionPane.showMessageDialog(this, "Access Denied!", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }
    }

    private void showLockScreen() {
        removeAll();
        JLabel lockLabel = new JLabel("🔒 Admin Area Locked", SwingConstants.CENTER);
        lockLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lockLabel.setForeground(Color.GRAY);
        add(lockLabel, BorderLayout.CENTER);
        revalidate(); 
        repaint();
    }

    private void initAdminInterface() {
        removeAll();
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Tab report
        tabs.addTab("Reports", new OriginalReportPanel());

        // Tab admin
        tabs.addTab("Function", new SettingsPanel());

        add(tabs, BorderLayout.CENTER);
        revalidate(); 
        repaint();
    }

    private class OriginalReportPanel extends JPanel {
        private JTextArea reportArea;

        public OriginalReportPanel() {
            setLayout(new BorderLayout());
            JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
            navBar.setBackground(new Color(240, 240, 240));

            JButton btnOcc = new JButton("Occupancy Report ");
            JButton btnRev = new JButton("Revenue Report ");
            JButton btnVeh = new JButton("Currently Vehicles in the Lot ");
            JButton btnFine = new JButton("Fine Report");

            navBar.add(btnOcc);
            navBar.add(btnRev);
            navBar.add(btnVeh);
            navBar.add(btnFine);

            reportArea = new JTextArea();
            reportArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
            reportArea.setEditable(false);
            reportArea.setMargin(new Insets(20, 20, 20, 20));
            JScrollPane scrollPane = new JScrollPane(reportArea);

            btnOcc.addActionListener(e -> reportArea.setText(parkingSystem.getDashboardReport("OCCUPANCY")));
            btnRev.addActionListener(e -> reportArea.setText(parkingSystem.getDashboardReport("REVENUE")));
            btnVeh.addActionListener(e -> reportArea.setText(parkingSystem.getDashboardReport("VEHICLES")));
            btnFine.addActionListener(e -> reportArea.setText(parkingSystem.getDashboardReport("FINES")));

            add(navBar, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);
        }
    }

    private class SettingsPanel extends JPanel {
        private JTextArea outputArea;

        public SettingsPanel() {
            setLayout(new BorderLayout());
            
            // Left menu container for buttons
            JPanel menu = new JPanel(new GridLayout(8, 1, 5, 5));
            menu.setBorder(new EmptyBorder(10, 10, 10, 10));
            menu.setPreferredSize(new Dimension(250, 0));
            menu.setBackground(new Color(230, 230, 230));

            // functional requirements
            String[] functions = {
                "View all floors and spots", 
                "View occupancy rate", 
                "View revenue", 
                "View vehicles currently parked", 
                "View unpaid fines", 
                "Choose a fine scheme", 
                "🚪 Logout"
            };

            for (String label : functions) {
                JButton btn = new JButton(label);
                btn.setHorizontalAlignment(SwingConstants.LEFT);
                btn.setFocusPainted(false);
                btn.addActionListener(e -> handleMenuAction(label));
                menu.add(btn);
            }
            
            // Right display area
            outputArea = new JTextArea();
            outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
            outputArea.setEditable(false);
            outputArea.setMargin(new Insets(20, 20, 20, 20));
            outputArea.setText("Select a function from the left menu.");
            
            add(menu, BorderLayout.WEST);
            add(new JScrollPane(outputArea), BorderLayout.CENTER);
        }

        private void handleMenuAction(String cmd) {
            if (cmd.contains("floors")) {
                outputArea.setText("--- ALL FLOORS & SPOTS STATUS ---\n" + parkingSystem.getDashboardReport("OCCUPANCY"));
            } else if (cmd.contains("occupancy")) {
                outputArea.setText(parkingSystem.getDashboardReport("OCCUPANCY"));
            } else if (cmd.contains("revenue")) {
                outputArea.setText(parkingSystem.getDashboardReport("REVENUE"));
            } else if (cmd.contains("vehicles")) {
                outputArea.setText(parkingSystem.getDashboardReport("VEHICLES"));
            } else if (cmd.contains("unpaid")) {
                outputArea.setText(parkingSystem.getDashboardReport("FINES"));
            } else if (cmd.contains("scheme")) {
                String[] options = {
                    "Option A: Fixed Fine (Flat RM 50)", 
                    "Option B: Progressive Fine (Increasing Rate)", 
                    "Option C: Hourly Fine (RM 20 per hour)"
                };
                
                String selected = (String) JOptionPane.showInputDialog(this, 
                        "Select active Fine Calculation Strategy:", 
                        "System Configuration", 
                        JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                
                if (selected != null) {
                    String optionId = selected.split(":")[0].trim(); 
                    parkingSystem.updateFineScheme(optionId);
                    outputArea.setText("System Action: Successfully updated to " + selected);
                }
            } else if (cmd.contains("Logout")) {
                isAuthenticated = false;
                showLockScreen();
            }
        }
    }

    public boolean isAuthenticated() { return isAuthenticated; }
}