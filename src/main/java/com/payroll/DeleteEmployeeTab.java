package com.payroll;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class DeleteEmployeeTab extends JPanel {
    private JTextField txtEmployeeId;
    private JButton btnDelete;
    private JLabel lblStatus;

    public DeleteEmployeeTab() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);

        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Employee ID:"), gbc);

        gbc.gridx = 1;
        txtEmployeeId = new JTextField(10);
        add(txtEmployeeId, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        btnDelete = new JButton("Delete Employee");
        add(btnDelete, gbc);

        gbc.gridy = 2;
        lblStatus = new JLabel(" ");
        add(lblStatus, gbc);

        btnDelete.addActionListener(e -> deleteEmployee());
    }

    private void deleteEmployee() {
        String idText = txtEmployeeId.getText();
        if(idText.isEmpty()) {
            lblStatus.setText("Please enter Employee ID");
            lblStatus.setForeground(Color.RED);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM employees WHERE employee_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(idText));
            int rows = ps.executeUpdate();

            if(rows > 0) {
                lblStatus.setText("Employee deleted successfully!");
                lblStatus.setForeground(new Color(0,128,0));
            } else {
                lblStatus.setText("Employee ID not found!");
                lblStatus.setForeground(Color.RED);
            }
        } catch(Exception ex) {
            lblStatus.setText("Error: "+ex.getMessage());
            lblStatus.setForeground(Color.RED);
        }
    }
}
