package com.payroll;

import javax.swing.*;
import java.awt.*;

public class RefreshTab extends JPanel {

    private static final long serialVersionUID = 1L;
    
    private ViewEmployeeTab viewTab; 

    public RefreshTab(ViewEmployeeTab viewTab) {
        super(new BorderLayout());
        this.viewTab = viewTab;
        
        JButton refreshBtn = new JButton("Force Refresh All Employee Data");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        refreshBtn.addActionListener(e -> {
            // ViewTab এর মাধ্যমে PayrollTabbedGUI-কে ডাটা রিলোড করার অনুরোধ পাঠানো হলো
            if (this.viewTab != null) {
                this.viewTab.reloadEmployees();
                JOptionPane.showMessageDialog(this, "Data refresh triggered successfully!", "Info", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error: View Tab reference is missing.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout());
        centerPanel.add(refreshBtn);
        
        add(centerPanel, BorderLayout.CENTER);
    }
}