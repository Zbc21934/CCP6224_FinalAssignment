package main;

import view.MainFrame; // Import your GUI window
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        System.out.println("Launching Parking System GUI...");

        // This block starts the Java Swing Window
        SwingUtilities.invokeLater(() -> {
            try {
                // Create the Window
                MainFrame gui = new MainFrame();
                
                // Make it visible
                gui.setVisible(true);
                
                System.out.println("GUI Launched Successfully!");
            } catch (Exception e) {
                System.out.println("GUI Failed to Open:");
                e.printStackTrace();
            }
        });
    }
}