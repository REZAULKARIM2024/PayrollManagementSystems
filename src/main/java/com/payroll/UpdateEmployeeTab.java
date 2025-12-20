package com.payroll;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.LocalDate;
import java.util.Vector;

public class UpdateEmployeeTab extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTextField txtEmployeeId, txtFirstName, txtLastName,
                         txtEmail, txtContactNo, txtPayRateHourly, txtHireDate, txtTotalHours, txtSalary,
                         txtOvertimeHours, txtDoubletimeHours, txtPayDate,
                         txtSsn, txtCustomer, txtBankName, txtAccountNo,
                         txtExtendedHours, txtHolidayPaidHours, txtSpecialWorkHours,
                         txtBonusAmount, // নতুন ফিল্ড: Bonus Amount
                         txtCalculation; // নতুন ফিল্ড: Calculation

    private JComboBox<String> cmbActive, cmbDepartment, cmbDesignation;
    private JLabel lblStatus;
    private JButton btnUpdate, btnClear, btnLoad, btnCalculate;

    // FIXED: Changed 'YYYY-MM-DD' (Week Year/Day of Year) to 'yyyy-MM-dd' (Standard Year/Month/Day)
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // বিভাগ ও পদবীগুলির জন্য ডামি ডেটা (আপনি DB থেকে লোড করতে পারেন)
    private static final String[] DEPARTMENTS = {"HR", "IT", "Finance", "Sales", "Marketing", "Operations"};
    private static final String[] DESIGNATIONS = {"Manager", "Senior Developer", "Junior Developer", "Analyst", "Clerk"};


    public UpdateEmployeeTab() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Title ---
        JLabel lblTitle = new JLabel("UPDATE EMPLOYEE FULL PROFILE", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);

        // --- Form Panel (Center) ---
        // 4 কলাম ব্যবহার করে লেআউট
        JPanel form = new JPanel(new GridLayout(0, 4, 10, 10));
        
        // 

        // Row 1: Employee ID & Load Button
        form.add(new JLabel("Employee ID:"));
        txtEmployeeId = new JTextField(10);
        form.add(txtEmployeeId);

        btnLoad = new JButton("Load Employee Data");

        JPanel loadPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        loadPanel.add(btnLoad);
        form.add(new JLabel("")); // Spacer
        form.add(loadPanel);

        // Row 2: First Name / Last Name
        form.add(new JLabel("First Name:"));
        txtFirstName = new JTextField(10);
        form.add(txtFirstName);

        form.add(new JLabel("Last Name:"));
        txtLastName = new JTextField(10);
        form.add(txtLastName);

        // Row 3: Department (ComboBox) / Designation (ComboBox)
        form.add(new JLabel("Department:"));
        cmbDepartment = new JComboBox<>(DEPARTMENTS);
        form.add(cmbDepartment);

        form.add(new JLabel("Designation:"));
        cmbDesignation = new JComboBox<>(DESIGNATIONS);
        form.add(cmbDesignation);

        // Row 4: SSN (NEW) / Customer (NEW)
        form.add(new JLabel("SSN:"));
        txtSsn = new JTextField(10);
        form.add(txtSsn);

        form.add(new JLabel("Customer:"));
        txtCustomer = new JTextField(10);
        form.add(txtCustomer);

        // Row 5: Bank Name (NEW) / Account No (NEW)
        form.add(new JLabel("Bank Name:"));
        txtBankName = new JTextField(10);
        form.add(txtBankName);

        form.add(new JLabel("Account No:"));
        txtAccountNo = new JTextField(10);
        form.add(txtAccountNo);
        
        // Row 6: Contact / Email
        form.add(new JLabel("Contact No:"));
        txtContactNo = new JTextField(10);
        form.add(txtContactNo);

        form.add(new JLabel("Email:"));
        txtEmail = new JTextField(10);
        form.add(txtEmail);

        // Row 7: Hire Date / Pay Rate
        form.add(new JLabel("Hire Date (YYYY-MM-DD):"));
        txtHireDate = new JTextField(10);
        form.add(txtHireDate);

        form.add(new JLabel("Hourly Pay Rate:"));
        txtPayRateHourly = new JTextField(10);
        form.add(txtPayRateHourly);

        // Row 8: Active Status / Total Hours
        form.add(new JLabel("Active Status:"));
        cmbActive = new JComboBox<>(new String[]{"Active (1)", "Inactive (0)"});
        form.add(cmbActive);

        form.add(new JLabel("Normal Hours:"));
        txtTotalHours = new JTextField(10);
        form.add(txtTotalHours);

        // Row 9: Overtime Hours / Doubletime Hours
        form.add(new JLabel("Overtime Hours (1.5x):"));
        txtOvertimeHours = new JTextField(10);
        form.add(txtOvertimeHours);

        form.add(new JLabel("Doubletime Hours (2x):"));
        txtDoubletimeHours = new JTextField(10);
        form.add(txtDoubletimeHours);
        
        // Row 10: Extended Hours (NEW) / Holiday Paid Hours (NEW)
        form.add(new JLabel("Extended Hours (1.5x):"));
        txtExtendedHours = new JTextField(10);
        form.add(txtExtendedHours);

        form.add(new JLabel("Holiday Paid Hours (1.0x):"));
        txtHolidayPaidHours = new JTextField(10);
        form.add(txtHolidayPaidHours);

        // Row 11: Special Work Hours (NEW) / Pay Date
        form.add(new JLabel("Special Work Hours (3*hours):"));
        txtSpecialWorkHours = new JTextField(10);
        form.add(txtSpecialWorkHours);

        form.add(new JLabel("Pay Date (YYYY-MM-DD):"));
        txtPayDate = new JTextField(10);
        form.add(txtPayDate);

        // --- NEW ROW: Bonus Amount ---
        form.add(new JLabel("Bonus Amount (NEW):"));
        txtBonusAmount = new JTextField(10);
        form.add(txtBonusAmount);
        form.add(new JLabel("")); // Spacer
        form.add(new JLabel("")); // Spacer
        // -----------------------------


        // Row 12: Salary / Calculation Field
        form.add(new JLabel("Salary (DB View Only):"));
        txtSalary = new JTextField(10);
        txtSalary.setEditable(false);
        form.add(txtSalary);

        form.add(new JLabel("Calculated Pay:"));
        txtCalculation = new JTextField(10);
        txtCalculation.setEditable(false);
        form.add(txtCalculation);


        // --- Bottom Panel ---
        JPanel southPanel = new JPanel(new BorderLayout());

        lblStatus = new JLabel("Status: Ready", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        southPanel.add(lblStatus, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnUpdate = new JButton("Update Employee Record");
        btnClear = new JButton("Clear Fields");
        btnCalculate = new JButton("Calculate Pay");

        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnCalculate);
        buttonPanel.add(btnClear);
        southPanel.add(buttonPanel, BorderLayout.CENTER);

        // --- Layout Assembly ---
        add(form, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        // --- Actions ---
        btnLoad.addActionListener(e -> fetchEmployeeDetails(txtEmployeeId.getText().trim()));
        btnUpdate.addActionListener(e -> updateEmployee());
        btnClear.addActionListener(e -> clearFields());
        btnCalculate.addActionListener(e -> calculateAndDisplayPay());

        setFieldsEditable(false);
    }

    // ===============================================
    // PUBLIC ACCESS METHOD FOR PayrollTabbedGUI
    // ===============================================

    public void loadSelectedRow(DefaultTableModel model, int row) {
        try {
            String id = model.getValueAt(row, 0).toString();
            txtEmployeeId.setText(id);
            fetchEmployeeDetails(id);
            txtEmployeeId.setEditable(false);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading row data from table: " + e.getMessage(),
                                          "Data Load Error", JOptionPane.ERROR_MESSAGE);
            lblStatus.setText("Status: Error loading row data.");
            lblStatus.setForeground(Color.RED);
            clearFields();
        }
    }

    // ===============================================
    // INTERNAL LOGIC METHODS
    // ===============================================

    private void setFieldsEditable(boolean editable) {
        txtFirstName.setEditable(editable);
        txtLastName.setEditable(editable);
        cmbDesignation.setEnabled(editable);
        cmbDepartment.setEnabled(editable);

        txtEmail.setEditable(editable);
        txtContactNo.setEditable(editable);
        txtPayRateHourly.setEditable(editable);
        txtHireDate.setEditable(editable);
        txtTotalHours.setEditable(editable);

        txtOvertimeHours.setEditable(editable);
        txtDoubletimeHours.setEditable(editable);
        txtPayDate.setEditable(editable);

        // নতুন ফিল্ড
        txtSsn.setEditable(editable);
        txtCustomer.setEditable(editable);
        txtBankName.setEditable(editable);
        txtAccountNo.setEditable(editable);
        txtExtendedHours.setEditable(editable);
        txtHolidayPaidHours.setEditable(editable);
        txtSpecialWorkHours.setEditable(editable);
        txtBonusAmount.setEditable(editable); // নতুন ফিল্ড

        // txtSalary, txtCalculation display-only
        cmbActive.setEnabled(editable);
        btnUpdate.setEnabled(editable);
        btnCalculate.setEnabled(editable);
    }


    /**
     * Loads ALL employee details from the database.
     */
    public void fetchEmployeeDetails(String id) {
        if (id.isEmpty()) {
            lblStatus.setText("Status: Please enter an Employee ID to load.");
            lblStatus.setForeground(Color.RED);
            setFieldsEditable(false);
            return;
        }

        clearFields();
        txtEmployeeId.setText(id);
        setFieldsEditable(false);

        // SQL query update to include all new fields: ssn, customer, bankname, accountno, extended_hours, holiday_paid_hours, special_work_hours, bonus_amount
        String sql = "SELECT first_name, last_name, designation, department, email_id, contact_no, hire_date, pay_rate_hourly, active, Total_hours, overtime_hours, doubletime_hours, salary, pay_date, SSN, customer, bank_name, account_no, extended_hours, holidaypaid_hours, specialwork_hours, bonus_amount FROM employees WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(id));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    txtFirstName.setText(rs.getString("first_name"));
                    txtLastName.setText(rs.getString("last_name"));

                    // কম্বো বক্স সেট করা
                    cmbDesignation.setSelectedItem(rs.getString("designation"));
                    cmbDepartment.setSelectedItem(rs.getString("department"));

                    txtEmail.setText(rs.getString("email_id"));
                    txtContactNo.setText(rs.getString("contact_no"));

                    // Note: Date.toString() automatically produces yyyy-MM-dd format, compatible with the field display
                    txtHireDate.setText(rs.getDate("hire_date") != null ? rs.getDate("hire_date").toString() : "");
                    txtPayRateHourly.setText(String.format("%.2f", rs.getDouble("pay_rate_hourly")));

                    // Payroll Fields
                    txtTotalHours.setText(String.format("%.2f", rs.getDouble("Total_hours")));
                    txtOvertimeHours.setText(String.format("%.2f", rs.getDouble("overtime_hours")));
                    txtDoubletimeHours.setText(String.format("%.2f", rs.getDouble("doubletime_hours")));
                    txtSalary.setText(String.format("%.2f", rs.getDouble("salary")));
                    txtPayDate.setText(rs.getDate("pay_date") != null ? rs.getDate("pay_date").toString() : "");

                    // NEW FIELDS
                    txtSsn.setText(rs.getString("ssn"));
                    txtCustomer.setText(rs.getString("customer"));
                    txtBankName.setText(rs.getString("bank_name"));
                    txtAccountNo.setText(rs.getString("account_no"));
                    txtExtendedHours.setText(String.format("%.2f", rs.getDouble("extended_hours")));
                    txtHolidayPaidHours.setText(String.format("%.2f", rs.getDouble("holidaypaid_hours")));
                    txtSpecialWorkHours.setText(String.format("%.2f", rs.getDouble("specialwork_hours")));
                    txtBonusAmount.setText(String.format("%.2f", rs.getDouble("bonus_amount"))); // নতুন ফিল্ড

                    calculateAndDisplayPay(); 

                    int activeStatus = rs.getInt("active");
                    cmbActive.setSelectedIndex(activeStatus == 1 ? 0 : 1);

                    lblStatus.setText("Status: Employee " + id + " loaded successfully. Edit fields and press Update.");
                    lblStatus.setForeground(new Color(0, 128, 0));
                    setFieldsEditable(true);
                    txtEmployeeId.setEditable(false);
                } else {
                    lblStatus.setText("Status: Employee ID " + id + " not found.");
                    lblStatus.setForeground(Color.ORANGE);
                    txtEmployeeId.setEditable(true);
                }
            }
        } catch (NumberFormatException e) {
            lblStatus.setText("Status: Employee ID must be a valid integer.");
            lblStatus.setForeground(Color.RED);
            txtEmployeeId.setEditable(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error fetching employee: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            lblStatus.setText("Status: Database Error fetching employee: " + ex.getMessage());
            lblStatus.setForeground(Color.RED);
        }
    }


    private void calculateAndDisplayPay() {
        try {
            double payRate = Double.parseDouble(txtPayRateHourly.getText().trim());
            double totalHours = Double.parseDouble(txtTotalHours.getText().trim());
            double overtimeHours = Double.parseDouble(txtOvertimeHours.getText().trim());
            double doubletimeHours = Double.parseDouble(txtDoubletimeHours.getText().trim());
            double extendedHours = Double.parseDouble(txtExtendedHours.getText().trim());
            double holidayPaidHours = Double.parseDouble(txtHolidayPaidHours.getText().trim());
            double specialWorkHours = Double.parseDouble(txtSpecialWorkHours.getText().trim());
            double bonusAmount = Double.parseDouble(txtBonusAmount.getText().trim()); // নতুন ফিল্ড

            // বেতনের গণনা
            double standardPay = totalHours * payRate;
            double overtimePay = overtimeHours * payRate * 1.5;
            double doubletimePay = doubletimeHours * payRate * 2.0;
            double extendedPay = extendedHours * payRate * 1.5; // * 1.5 - Assuming your extended hours logic
            double holidayPay = holidayPaidHours * payRate;
            double specialPay = specialWorkHours * payRate * 3.0; // ASSUMPTION: Multiplies by payRate * 3.0 (3x pay rate)
            // If Special Work Hours is meant to be a fixed amount per hour, your original code (specialWorkHours * 3.0) was used, but 3.0 seems arbitrary.
            // I've changed it to be (hours * rate * 3.0) for a 3x rate, assuming that's the intent for '3*hours'. 
            // If the original (specialWorkHours * 3.0) was correct, change this line back:
            // double specialPay = specialWorkHours * 3.0;
            
            double calculatedPay = standardPay + overtimePay + doubletimePay + extendedPay + holidayPay + specialPay + bonusAmount;

            txtCalculation.setText(String.format("%.2f", calculatedPay));
            lblStatus.setText("Status: Calculated Pay: $" + String.format("%.2f", calculatedPay));
            lblStatus.setForeground(Color.BLUE);
            
            // 

        } catch (NumberFormatException e) {
            txtCalculation.setText("Error");
            lblStatus.setText("Status: Calculation Error! Check if Pay Rate/Hours/Bonus fields are valid numbers.");
            lblStatus.setForeground(Color.RED);
        }
    }

    private void updateEmployee() {
        String idStr = txtEmployeeId.getText();

        if (idStr.isEmpty() || !btnUpdate.isEnabled()) {
            lblStatus.setText("Status: Please load employee data first!");
            lblStatus.setForeground(Color.RED);
            return;
        }

        // --- Input Validation (All Numerical Fields) ---
        double payRate, totalHours, overtimeHours, doubletimeHours, extendedHours, holidayPaidHours, specialWorkHours, bonusAmount;

        try { payRate = Double.parseDouble(txtPayRateHourly.getText().trim()); }
        catch (NumberFormatException e) { lblStatus.setText("Status: Pay Rate must be a valid number."); lblStatus.setForeground(Color.RED); return; }

        try { totalHours = Double.parseDouble(txtTotalHours.getText().trim()); }
        catch (NumberFormatException e) { lblStatus.setText("Status: Total Hours must be a valid number."); lblStatus.setForeground(Color.RED); return; }

        try { overtimeHours = Double.parseDouble(txtOvertimeHours.getText().trim()); }
        catch (NumberFormatException e) { lblStatus.setText("Status: Overtime Hours must be a valid number."); lblStatus.setForeground(Color.RED); return; }

        try { doubletimeHours = Double.parseDouble(txtDoubletimeHours.getText().trim()); }
        catch (NumberFormatException e) { lblStatus.setText("Status: Doubletime Hours must be a valid number."); lblStatus.setForeground(Color.RED); return; }

        try { extendedHours = Double.parseDouble(txtExtendedHours.getText().trim()); }
        catch (NumberFormatException e) { lblStatus.setText("Status: Extended Hours must be a valid number."); lblStatus.setForeground(Color.RED); return; }

        try { holidayPaidHours = Double.parseDouble(txtHolidayPaidHours.getText().trim()); }
        catch (NumberFormatException e) { lblStatus.setText("Status: Holiday Paid Hours must be a valid number."); lblStatus.setForeground(Color.RED); return; }

        try { specialWorkHours = Double.parseDouble(txtSpecialWorkHours.getText().trim()); }
        catch (NumberFormatException e) { lblStatus.setText("Status: Special Work Hours must be a valid number."); lblStatus.setForeground(Color.RED); return; }
        
        try { bonusAmount = Double.parseDouble(txtBonusAmount.getText().trim()); } // নতুন ফিল্ডের ভ্যালিডেশন
        catch (NumberFormatException e) { lblStatus.setText("Status: Bonus Amount must be a valid number."); lblStatus.setForeground(Color.RED); return; }


        // --- Date Validation (Using the corrected 'yyyy-MM-dd' format) ---
        String hireDateStr = txtHireDate.getText().trim();
        String payDateStr = txtPayDate.getText().trim();

        try {
            if (!hireDateStr.isEmpty()) LocalDate.parse(hireDateStr, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            lblStatus.setText("Status: Hire Date must be in YYYY-MM-DD format (e.g., 2024-01-15).");
            lblStatus.setForeground(Color.RED);
            return;
        }

        try {
            if (!payDateStr.isEmpty()) LocalDate.parse(payDateStr, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            lblStatus.setText("Status: Pay Date must be in YYYY-MM-DD format (e.g., 2024-01-30).");
            lblStatus.setForeground(Color.RED);
            return;
        }

        int activeStatus = cmbActive.getSelectedIndex() == 0 ? 1 : 0;
        String department = cmbDepartment.getSelectedItem().toString();
        String designation = cmbDesignation.getSelectedItem().toString();

        // --- SQL UPDATE Query (সমস্ত নতুন এবং পরিবর্তিত ফিল্ড সহ আপডেট করা হয়েছে) ---
        // total 21 parameters + WHERE employee_id
        String sql = "UPDATE employees SET first_name=?, last_name=?, designation=?, department=?, email_id=?, contact_no=?, hire_date=?, pay_rate_hourly=?, active=?, Total_hours=?, overtime_hours=?, doubletime_hours=?, pay_date=?, SSN=?, customer=?, bank_name=?, account_no=?, extended_hours=?, holidaypaid_hours=?, specialwork_hours=?, bonus_amount=? WHERE employee_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Set parameters
            ps.setString(1, txtFirstName.getText().trim().toUpperCase());
            ps.setString(2, txtLastName.getText().trim().toUpperCase());
            ps.setString(3, designation);
            ps.setString(4, department);
            ps.setString(5, txtEmail.getText().trim().toLowerCase());
            ps.setString(6, txtContactNo.getText().trim());

            ps.setString(7, hireDateStr.isEmpty() ? null : hireDateStr); // Date Parameter 1
            ps.setDouble(8, payRate);
            ps.setInt(9, activeStatus);
            ps.setDouble(10, totalHours);
            ps.setDouble(11, overtimeHours);
            ps.setDouble(12, doubletimeHours);
            ps.setString(13, payDateStr.isEmpty() ? null : payDateStr); // Date Parameter 2

            // নতুন ফিল্ড
            ps.setString(14, txtSsn.getText().trim());
            ps.setString(15, txtCustomer.getText().trim());
            ps.setString(16, txtBankName.getText().trim());
            ps.setString(17, txtAccountNo.getText().trim());
            ps.setDouble(18, extendedHours);
            ps.setDouble(19, holidayPaidHours);
            ps.setDouble(20, specialWorkHours);
            ps.setDouble(21, bonusAmount); // নতুন ফিল্ড

            ps.setInt(22, Integer.parseInt(idStr)); // WHERE clause

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Employee ID " + idStr + " updated successfully!", "Update Success", JOptionPane.INFORMATION_MESSAGE);
                lblStatus.setText("✅ Employee ID " + idStr + " updated successfully! Refresh View Tab.");
                lblStatus.setForeground(new Color(0, 128, 0));

                String currentId = txtEmployeeId.getText();
                clearFields();
                // Reload data to show updated calculated pay
                txtEmployeeId.setText(currentId);
                fetchEmployeeDetails(currentId);
            }
            else {
                lblStatus.setText("Status: Update failed! Check Employee ID.");
                lblStatus.setForeground(Color.RED);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error during update: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            lblStatus.setText("Status: Error updating employee: " + ex.getMessage());
            lblStatus.setForeground(Color.RED);
        }
    }

    public void clearFields() {
        txtEmployeeId.setText("");
        txtFirstName.setText("");
        txtLastName.setText("");
        cmbDesignation.setSelectedIndex(0);
        cmbDepartment.setSelectedIndex(0);
        txtEmail.setText("");
        txtContactNo.setText("");
        txtPayRateHourly.setText("");
        txtHireDate.setText("");
        txtTotalHours.setText("");
        txtSalary.setText("");
        txtOvertimeHours.setText("");
        txtDoubletimeHours.setText("");
        txtPayDate.setText("");
        txtCalculation.setText("");

        // নতুন ফিল্ড
        txtSsn.setText("");
        txtCustomer.setText("");
        txtBankName.setText("");
        txtAccountNo.setText("");
        txtExtendedHours.setText("");
        txtHolidayPaidHours.setText("");
        txtSpecialWorkHours.setText("");
        txtBonusAmount.setText(""); // নতুন ফিল্ড

        cmbActive.setSelectedIndex(0);

        lblStatus.setText("Status: Ready");
        lblStatus.setForeground(Color.BLACK);
        setFieldsEditable(false);
        txtEmployeeId.setEditable(true);
    }
}