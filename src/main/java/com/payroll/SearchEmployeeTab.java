package com.payroll;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SearchEmployeeTab extends JPanel {
    private JTextField txtSearch;
    private JButton btnSearch;
    private JTable table;
    private DefaultTableModel model;

    public SearchEmployeeTab() {
        setLayout(new BorderLayout());

        // Top Search Panel
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("Search by Employee ID or First Name:"));
        txtSearch = new JTextField(15);
        topPanel.add(txtSearch);
        btnSearch = new JButton("Search");
        topPanel.add(btnSearch);

        add(topPanel, BorderLayout.NORTH);

        // Table Setup
        model = new DefaultTableModel();
        table = new JTable(model);
        
        // Columns must match the order in the SELECT statement
        model.setColumnIdentifiers(new String[]{
            "ID", "First Name", "Last Name", "Designation", 
            "Contact No", "Email ID", "Department", "Hourly Pay Rate" 
        });
        
        // Make table read-only
        table.setEnabled(false);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Action Listener
        btnSearch.addActionListener(e -> searchEmployee());
    }

    private void searchEmployee() {
        String keyword = txtSearch.getText().trim();
        if(keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an Employee ID or First Name to search.");
            return;
        }
        
        model.setRowCount(0); 

        // Columns to select: Must match the order in model.addRow
        String selectCols = "employee_id, first_name, last_name, designation, contact_no, email_id, department, pay_rate_hourly";
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql;
            PreparedStatement ps;

            // Check if the keyword is purely numeric (for ID search)
            if (keyword.matches("\\d+")) { 
                sql = "SELECT " + selectCols + " FROM employees WHERE employee_id = ? AND active = 1";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, Integer.parseInt(keyword));
                
            } else { 
                // Search by first name (case-insensitive partial match)
                // Using UPPER() on both the column and the keyword ensures case-insensitivity
                sql = "SELECT " + selectCols + " FROM employees WHERE UPPER(first_name) LIKE ? AND active = 1";
                ps = conn.prepareStatement(sql);
                ps.setString(1, "%" + keyword.toUpperCase() + "%"); 
            }

            ResultSet rs = ps.executeQuery();

            if (!rs.isBeforeFirst()) {    
                JOptionPane.showMessageDialog(this, "No matching employees found.");
                return;
            }

            // Populate table with results
            while(rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("employee_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("designation"),
                    rs.getString("contact_no"),
                    rs.getString("email_id"),
                    rs.getString("department"),
                    rs.getDouble("pay_rate_hourly")
                });
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error: Invalid number entered for Employee ID search.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}