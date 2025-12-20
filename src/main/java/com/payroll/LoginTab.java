package com.payroll;

import javax.swing.*;
import java.awt.*;

public class LoginTab extends JPanel {

    private final JTabbedPane tabbedPane;

    public LoginTab(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // ===== Header bar =====
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 118, 210)); // Bright blue
        headerPanel.setPreferredSize(new Dimension(0, 60));

        JLabel lblTitle = new JLabel("PAYROLL MANAGEMENT SYSTEM", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.CENTER);

        JLabel lblCompany = new JLabel("Transfotech, LLC  ", SwingConstants.RIGHT);
        lblCompany.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCompany.setForeground(Color.WHITE);
        headerPanel.add(lblCompany, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // ===== Center login form =====
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(245, 245, 245)); // light gray

        JPanel loginForm = new JPanel(new GridBagLayout());
        loginForm.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JTextField txtUsername = new JTextField(15);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JPasswordField txtPassword = new JPasswordField(15);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(25, 118, 210));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        // === Add components ===
        gbc.gridx = 0; gbc.gridy = 0;
        loginForm.add(lblUsername, gbc);

        gbc.gridx = 1;
        loginForm.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        loginForm.add(lblPassword, gbc);

        gbc.gridx = 1;
        loginForm.add(txtPassword, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.CENTER;
        loginForm.add(btnLogin, gbc);

        centerPanel.add(loginForm);
        add(centerPanel, BorderLayout.CENTER);

        // ===== Footer text =====
        JLabel lblFooter = new JLabel("© Transfotech Academy | Payroll Management System", SwingConstants.CENTER);
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFooter.setForeground(new Color(100, 100, 100));
        lblFooter.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(lblFooter, BorderLayout.SOUTH);

        // ===== Login Button Action =====
        btnLogin.addActionListener(e -> {
            String username = txtUsername.getText();
            String password = new String(txtPassword.getPassword());

            if ("admin".equals(username) && "admin123".equals(password)) {
                JOptionPane.showMessageDialog(this, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);

                // Switch to Welcome tab (assumes Welcome tab is at index 0)
                tabbedPane.setSelectedIndex(0);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
