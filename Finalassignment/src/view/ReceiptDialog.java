/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author PatrickToh
 */
import javax.swing.*;
import java.awt.*;

public class ReceiptDialog extends JDialog {

    public ReceiptDialog(JFrame parent, String receiptHtml) {
        super(parent, "Official Receipt", true); // true mean much do action to close dialog
        
        setSize(400, 500);
        setLocationRelativeTo(parent); 
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("UNIVERSITY PARKING SYSTEM");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(Color.WHITE);
        
        //content
        JEditorPane receiptPane = new JEditorPane();
        receiptPane.setContentType("text/html"); 
        receiptPane.setText(receiptHtml);
        receiptPane.setEditable(false);
        receiptPane.setBackground(Color.WHITE);
        
        // scrooll
        JScrollPane scrollPane = new JScrollPane(receiptPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // botton button
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 14));
        closeBtn.setBackground(new Color(220, 220, 220));
        closeBtn.addActionListener(e -> dispose()); 

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        btnPanel.add(closeBtn);

        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }
}