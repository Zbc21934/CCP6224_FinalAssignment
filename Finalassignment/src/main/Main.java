package main;

import view.MainFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // try ti open Nimbus
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // error then use default design
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ex) {}
        }

        System.out.println("Launching Parking System GUI...");

        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame gui = new MainFrame();
                gui.setVisible(true);
                System.out.println("GUI Launched Successfully!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}