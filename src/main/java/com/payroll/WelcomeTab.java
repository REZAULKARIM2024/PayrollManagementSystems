package com.payroll;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class WelcomeTab extends JPanel {

    private final JTabbedPane tabbedPane;

    public WelcomeTab(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(14, 90, 255)); // Blue header color
        headerPanel.setPreferredSize(new Dimension(0, 50));

        JLabel titleLabel = new JLabel("PAYROLL MANAGEMENT SYSTEM");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JLabel companyLabel = new JLabel("Transfotech Academy");
        companyLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        companyLabel.setForeground(Color.BLACK);

        JLabel subtitleLabel = new JLabel("Payroll Management System");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.DARK_GRAY);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        titlePanel.add(companyLabel);
        titlePanel.add(subtitleLabel);

        JPanel logoAndTitle = new JPanel(new BorderLayout());
        logoAndTitle.setBackground(Color.WHITE);
        // You can add an icon here if you want. Placeholder empty panel for now:
        JPanel iconPanel = new JPanel();
        iconPanel.setPreferredSize(new Dimension(60, 60));
        iconPanel.setBackground(Color.WHITE);
        logoAndTitle.add(iconPanel, BorderLayout.WEST);
        logoAndTitle.add(titlePanel, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(logoAndTitle, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // Grid of panels with clickable buttons
        JPanel gridPanel = new JPanel(new GridLayout(2, 5, 10, 10)); // 2 rows, 5 columns, gap 10px
        gridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        gridPanel.setBackground(Color.WHITE);

        // Define panels with label text and tab names to switch to
        String[][] panelInfo = {
                {"Login", "Login"},
                {"Add Employee", "Add Employee"},
                {"View Employees", "View Employees"},
                {"Update Employee", "Update Employee"},
                {"Payslip", "Payslip"},
                {"Delete Employee", "Delete Employee"},
                {"Search Employee", "Search Employee"},
                {"Refresh", "Refresh"},
                {"Exit", null} // Exit button closes app
        };

        for (String[] info : panelInfo) {
            String label = info[0];
            String tabName = info[1];
            JPanel panel = createClickablePanel(label, tabName);
            gridPanel.add(panel);
        }

        add(gridPanel, BorderLayout.CENTER);

        // Footer label
        JLabel footerLabel = new JLabel("Welcome! Use the buttons above to manage employees and generate payslips.");
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        footerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        footerLabel.setForeground(Color.DARK_GRAY);
        add(footerLabel, BorderLayout.SOUTH);
    }

    private JPanel createClickablePanel(String text, String tabName) {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(220, 220, 220));
        panel.setBorder(BorderFactory.createLineBorder(new Color(14, 90, 255)));
        panel.setLayout(new GridBagLayout());

        JLabel label = new JLabel(text);
        label.setForeground(new Color(14, 90, 255));
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));

        panel.add(label);

        // Change cursor on hover
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Mouse click listener
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if ("Exit".equals(text)) {
                    System.exit(0);
                } else if (tabName != null) {
                    int index = tabbedPane.indexOfTab(tabName);
                    if (index != -1) {
                        tabbedPane.setSelectedIndex(index);
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(new Color(180, 180, 255));
                label.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(new Color(220, 220, 220));
                label.setForeground(new Color(14, 90, 255));
            }
        });

        return panel;
    }
}
