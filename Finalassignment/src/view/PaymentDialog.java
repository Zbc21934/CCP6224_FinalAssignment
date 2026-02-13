package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class PaymentDialog extends JDialog {
    
    private String selectedMethod = null; 

    public PaymentDialog(JFrame parent, String billHtml) {
        super(parent, "Payment Check-out", true); 
        
        setSize(480, 650); 
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- 1. 顶部标题 ---
        JLabel titleLabel = new JLabel("PARKING CHECK-OUT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(52, 152, 219)); 
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(20, 0, 10, 0));
        
        // --- 2. 中间账单内容 ---
        JEditorPane billPane = new JEditorPane();
        billPane.setContentType("text/html");
        billPane.setText(billHtml);
        billPane.setEditable(false);
        billPane.setBackground(new Color(245, 245, 245)); 
        billPane.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JScrollPane scrollPane = new JScrollPane(billPane);
        scrollPane.setBorder(new EmptyBorder(10, 25, 10, 25)); 
        scrollPane.getViewport().setBackground(Color.WHITE);

        // --- 3. 底部支付按钮区域 ---
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 15, 0)); 
        btnPanel.setBorder(new EmptyBorder(20, 25, 25, 25));
        btnPanel.setBackground(Color.WHITE);

        // [按钮 A] 现金支付
        JButton cashBtn = createStyledButton("💵 Pay Cash", new Color(46, 204, 113)); 
        cashBtn.addActionListener(e -> {
            selectedMethod = "CASH"; 
            dispose(); 
        });

        // [按钮 B] 刷卡支付 (专业版 + 验证)
        JButton cardBtn = createStyledButton("💳 Pay Card", new Color(52, 152, 219)); 
        cardBtn.addActionListener(e -> {
            performCardSimulation();
        });

        btnPanel.add(cashBtn);
        btnPanel.add(cardBtn);

        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    // 🟢 专业版：模拟信用卡支付网关 (带验证)
    private void performCardSimulation() {
        JPanel cardFormPanel = new JPanel();
        cardFormPanel.setLayout(new BoxLayout(cardFormPanel, BoxLayout.Y_AXIS));
        cardFormPanel.setBackground(Color.WHITE);
        cardFormPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel header = new JLabel("Credit or Debit Card Details");
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardFormPanel.add(header);
        cardFormPanel.add(Box.createVerticalStrut(15));

        // --- 卡号字段 ---
        cardFormPanel.add(createLabel("Card Number"));
        JTextField cardNumField = createTextField("0000 0000 0000 0000"); // 占位符提示
        cardFormPanel.add(cardNumField);
        cardFormPanel.add(Box.createVerticalStrut(10));

        // --- 过期日期和 CVC ---
        JPanel rowPanel = new JPanel(new GridLayout(1, 2, 15, 0)); 
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel expiryPanel = new JPanel(new BorderLayout());
        expiryPanel.setBackground(Color.WHITE);
        expiryPanel.add(createLabel("Expiry Date (MM/YY)"), BorderLayout.NORTH);
        JTextField expiryField = createTextField("MM/YY");
        expiryPanel.add(expiryField, BorderLayout.CENTER);
        
        JPanel cvcPanel = new JPanel(new BorderLayout());
        cvcPanel.setBackground(Color.WHITE);
        cvcPanel.add(createLabel("Security Code (CVC)"), BorderLayout.NORTH);
        JTextField cvcField = createTextField("123");
        cvcPanel.add(cvcField, BorderLayout.CENTER);

        rowPanel.add(expiryPanel);
        rowPanel.add(cvcPanel);
        
        JPanel rowContainer = new JPanel(new BorderLayout());
        rowContainer.setBackground(Color.WHITE);
        rowContainer.add(rowPanel, BorderLayout.NORTH);
        rowContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        cardFormPanel.add(rowContainer);
        cardFormPanel.add(Box.createVerticalStrut(20));
        
        JLabel icons = new JLabel("🔒 Secured by ParkingPayment Gateway");
        icons.setFont(new Font("Arial", Font.ITALIC, 10));
        icons.setForeground(Color.GRAY);
        icons.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardFormPanel.add(icons);

        // 循环显示弹窗，直到用户输入正确或点击取消
        while (true) {
            int result = JOptionPane.showConfirmDialog(this, 
                    cardFormPanel, 
                    "Secure Payment Gateway", 
                    JOptionPane.OK_CANCEL_OPTION, 
                    JOptionPane.PLAIN_MESSAGE); 

            if (result != JOptionPane.OK_OPTION) {
                return; // 用户点击取消，直接退出
            }

            // --- 🟢 开始验证 (Validation) ---
            String rawCardNum = cardNumField.getText();
            String cardNum = rawCardNum.replaceAll("\\s+", ""); // 去掉空格
            String expiry = expiryField.getText().trim();
            String cvc = cvcField.getText().trim();

            // 1. 验证卡号 (必须是16位数字)
            if (!cardNum.matches("\\d{16}")) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid Card Number!\nPlease enter 16 digits.", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                continue; // 重新弹窗
            }

            // 2. 验证日期 (格式必须是 MM/YY)
            if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid Expiry Date!\nFormat must be MM/YY (e.g., 08/26).", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                continue; 
            }

            // 3. 验证 CVC (必须是3位数字)
            if (!cvc.matches("\\d{3}")) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid CVC!\nMust be 3 digits.", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                continue; 
            }

            // --- ✅ 验证通过 ---
            
            // 模拟处理动画
            JOptionPane.showMessageDialog(this, 
                "Connecting to Bank...\n" +
                "Verifying Card: **** **** **** " + cardNum.substring(12) + "\n\n" +
                "Transaction Authorized! ✅", 
                "Processing Payment", 
                JOptionPane.INFORMATION_MESSAGE);

            selectedMethod = "CARD";
            dispose(); // 关闭主支付窗口
            break; // 跳出循环
        }
    }

    public String getSelectedMethod() {
        return selectedMethod;
    }

    // 🎨 辅助：创建漂亮按钮
    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(0, 50));
        btn.setBorder(BorderFactory.createEmptyBorder());
        return btn;
    }

    // 🎨 辅助：创建表单 Label (之前你漏掉的)
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(80, 80, 80));
        lbl.setBorder(new EmptyBorder(0, 0, 5, 0));
        return lbl;
    }
    
    // 🎨 辅助：创建输入框 
    private JTextField createTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Monospaced", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1, true),
            new EmptyBorder(8, 8, 8, 8)
        ));
        return tf;
    }
}