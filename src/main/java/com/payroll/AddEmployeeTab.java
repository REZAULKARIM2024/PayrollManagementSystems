package com.payroll;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern; 

public class AddEmployeeTab extends JPanel {

    private static final long serialVersionUID = 1L;

    // ===============================================
    // INTERFACE & CONSTANTS
    // ===============================================
    public interface EmployeeAddedListener {
        void onEmployeeAdded();
    }
    
    private EmployeeAddedListener listener;

    // Calculation Constants
    private static final double OVERTIME_MULTIPLIER = 1.5;
    private static final double DOUBLETIME_MULTIPLIER = 2.0;
    private static final double EXTENDED_MULTIPLIER = 1.0; 
    private static final double HOLIDAYPAID_MULTIPLIER = 1.0;
    private static final double SPECIALWORK_MULTIPLIER = 1.0;
    
    private static final double TAX_PERCENTAGE = 0.20; // 20%
    private static final double MEDICAL_PERCENTAGE = 0.10; // 10%
    private static final int NORMAL_HOURS_THRESHOLD = 40;
    private static final DateTimeFormatter INPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter SQL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Regex for name validation (allows only letters and spaces)
    private static final Pattern NAME_VALIDATOR = Pattern.compile("^[a-zA-Z\\s]+$");


    // ===============================================
    // UI COMPONENTS
    // ===============================================
    // Input Fields (Existing & New)
    private JTextField txtEmployeeId, txtFirstName, txtLastName, txtContactNo, txtEmail, txtPayRate;
    private JTextField txtTotalHours, txtOvertimeHours, txtDoubletimeHours;
    private JComboBox<String> cmbDepartment, cmbDesignation;
    
    private JTextField txtSSN, txtHireDate, txtCustomer, txtBankName, txtAccountNo;
    private JTextField txtExtendedHours, txtHolidayPaidHours, txtSpecialWorkHours, txtBonusAmount;


    // Output/Calculated Fields (Existing & New)
    private JTextField txtNormalSalary, txtOvertimeSalary, txtDoubletimeSalary;
    private JTextField txtExtendedSalary, txtHolidayPaidSalary, txtSpecialWorkSalary;
    private JTextField txtGrossSalary, txtTaxDeduction, txtMedicalInsurance, txtTotalDeductions, txtNetSalary, txtPayDate;


    private JButton btnAdd, btnCalculate;
    private JLabel lblStatus;

    // ===============================================
    // CONSTRUCTOR
    // ===============================================
    public AddEmployeeTab(EmployeeAddedListener listener) {
        this.listener = listener;
        setLayout(new BorderLayout());

        // --- Title Bar ---
        JLabel lblTitle = new JLabel("ADD NEW EMPLOYEE PAYROLL DATA", SwingConstants.CENTER);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new Color(0, 102, 255));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(lblTitle, BorderLayout.NORTH);

        // --- Main Form Panel (Center) ---
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        GridBagConstraints gbcContainer = new GridBagConstraints();
        gbcContainer.insets = new Insets(10, 20, 10, 20);
        gbcContainer.anchor = GridBagConstraints.NORTHWEST;
        
        formContainer.add(createInputPanel(), gbcContainer);

        gbcContainer.gridx = 1;
        formContainer.add(createOutputPanel(), gbcContainer);
        
        add(new JScrollPane(formContainer), BorderLayout.CENTER); 

        // --- Bottom Panel ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnAdd = new JButton("Add Employee Record");
        lblStatus = new JLabel("Please enter data and calculate salary.", SwingConstants.CENTER);
        
        bottomPanel.add(btnAdd);
        bottomPanel.add(lblStatus);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- Event Listeners ---
        btnAdd.addActionListener(e -> addEmployee());
        
        // Initial setup
        loadLastEmployeeId();
    }
    
    // ===============================================
    // UI CREATION METHODS
    // ===============================================
    
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Employee Personal & Hourly Data"));
        GridBagConstraints gbc = createGbc(5, 5);

        // Initialize all input fields
        txtEmployeeId = new JTextField(15);
        txtFirstName = new JTextField(15);
        txtLastName = new JTextField(15);
        cmbDepartment = new JComboBox<>(new String[]{"-- Select --", "HR", "Finance", "IT", "Sales"});
        cmbDesignation = new JComboBox<>(new String[]{"-- Select --", "Manager", "Executive", "Engineer", "Assistant"});
        txtContactNo = new JTextField(15);
        txtEmail = new JTextField(15);
        txtPayRate = new JTextField(15);
        txtTotalHours = new JTextField(15);
        txtOvertimeHours = new JTextField(15);
        txtDoubletimeHours = new JTextField(15);
        
        // New input fields based on CSV/Schema
        txtSSN = new JTextField(15);
        txtHireDate = new JTextField(15);
        txtCustomer = new JTextField(15);
        txtBankName = new JTextField(15);
        txtAccountNo = new JTextField(15);
        txtBonusAmount = new JTextField(15); 
        txtExtendedHours = new JTextField(15);
        txtHolidayPaidHours = new JTextField(15);
        txtSpecialWorkHours = new JTextField(15);


        String[] labels = {
            "Employee ID:", "First Name:", "Last Name:",
            "Department:", "Designation:", "SSN:", "Contact Number:", 
            "Email ID:", "Hire Date (MM/DD/YYYY):", "Customer (Optional):", 
            "Bank Name:", "Account No:", "Pay Rate (hour):", "Normal Hours:", 
            "Overtime Hours:", "Doubletime Hours:", "Extended Hours:", 
            "Holiday Paid Hours:", "Special Work Hours:", "Bonus Amount:"
        };
        
        Component[] components = {
            txtEmployeeId, txtFirstName, txtLastName, cmbDepartment, cmbDesignation, 
            txtSSN, txtContactNo, txtEmail, txtHireDate, txtCustomer, 
            txtBankName, txtAccountNo, txtPayRate, txtTotalHours, 
            txtOvertimeHours, txtDoubletimeHours, txtExtendedHours, 
            txtHolidayPaidHours, txtSpecialWorkHours, txtBonusAmount
        };

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            panel.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1;
            panel.add(components[i], gbc);
        }
        return panel;
    }

    private JPanel createOutputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Salary & Deduction Calculations"));
        GridBagConstraints gbc = createGbc(5, 5);

        // Initialize calculated fields
        txtNormalSalary = new JTextField(15);
        txtOvertimeSalary = new JTextField(15);
        txtDoubletimeSalary = new JTextField(15);
        txtExtendedSalary = new JTextField(15);
        txtHolidayPaidSalary = new JTextField(15);
        txtSpecialWorkSalary = new JTextField(15);
        
        txtGrossSalary = new JTextField(15);
        txtTaxDeduction = new JTextField(15);
        txtMedicalInsurance = new JTextField(15);
        txtTotalDeductions = new JTextField(15);
        txtNetSalary = new JTextField(15);
        txtPayDate = new JTextField(15);
        
        // Initialize btnCalculate 
        btnCalculate = new JButton("Calculate Salary"); 

        // Make calculated fields non-editable
        List<JTextField> calculatedFields = Arrays.asList(
            txtNormalSalary, txtOvertimeSalary, txtDoubletimeSalary, 
            txtExtendedSalary, txtHolidayPaidSalary, txtSpecialWorkSalary,
            txtGrossSalary, txtTaxDeduction, txtMedicalInsurance, 
            txtTotalDeductions, txtNetSalary
        );
        calculatedFields.forEach(f -> f.setEditable(false));

        String[] labels = {
            "Normal Salary:", "Overtime Salary:", "Doubletime Salary:", 
            "Extended Salary:", "Holiday Paid Salary:", "Special Work Salary:", 
            "Gross Salary:", "Tax Deduction (20%):", "Medical Insurance (10%):",
            "Total Deductions:", "Net Salary:", "Pay Date (MM/DD/YYYY):"
        };
        JTextField[] fields = {
            txtNormalSalary, txtOvertimeSalary, txtDoubletimeSalary,
            txtExtendedSalary, txtHolidayPaidSalary, txtSpecialWorkSalary,
            txtGrossSalary, txtTaxDeduction, txtMedicalInsurance,
            txtTotalDeductions, txtNetSalary, txtPayDate
        };

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            panel.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1;
            panel.add(fields[i], gbc);
        }
        
        // Calculate button
        gbc.gridx = 1; gbc.gridy = labels.length; 
        gbc.anchor = GridBagConstraints.EAST; 
        panel.add(btnCalculate, gbc);
        
        // Add listener here to ensure btnCalculate is initialized
        btnCalculate.addActionListener(e -> calculateSalary());
        
        return panel;
    }
    
    private GridBagConstraints createGbc(int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(y, x, y, x);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    // ===============================================
    // CORE LOGIC METHODS
    // ===============================================

    /**
     * Helper to validate names (no blank, no special character, no integer value)
     */
    private boolean isValidName(String name) {
        // NAME_VALIDATOR: Checks for only letters and spaces.
        return !name.isEmpty() && NAME_VALIDATOR.matcher(name).matches();
    }
    
    /**
     * Loads the next suggested Employee ID from the database.
     */
    public void loadLastEmployeeId() {
        String query = "SELECT MAX(employee_id) AS last_id FROM employees";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            
            int nextId = 1;
            
            if (rs.next()) {
                int lastId = rs.getInt("last_id");
                
                if (!rs.wasNull()) {
                    nextId = lastId + 1;
                }
            }
            txtEmployeeId.setText(String.valueOf(nextId));
            // Employee ID is now Editable.
        } catch (SQLException e) {
            System.err.println("Error fetching last Employee ID: " + e.getMessage());
            txtEmployeeId.setText("1"); 
        }
    }
    
    /**
     * Calculates Gross Salary, Deductions, and Net Salary.
     */
    private void calculateSalary() {
        try {
            double payRate = Double.parseDouble(txtPayRate.getText().trim());
            double totalHours = Double.parseDouble(txtTotalHours.getText().trim());
            
            // Hours and Bonus fields, default to 0 if empty
            double overtimeHours = Double.parseDouble(txtOvertimeHours.getText().trim().isEmpty() ? "0" : txtOvertimeHours.getText().trim());
            double doubletimeHours = Double.parseDouble(txtDoubletimeHours.getText().trim().isEmpty() ? "0" : txtDoubletimeHours.getText().trim());
            double extendedHours = Double.parseDouble(txtExtendedHours.getText().trim().isEmpty() ? "0" : txtExtendedHours.getText().trim());
            double holidayPaidHours = Double.parseDouble(txtHolidayPaidHours.getText().trim().isEmpty() ? "0" : txtHolidayPaidHours.getText().trim());
            double specialWorkHours = Double.parseDouble(txtSpecialWorkHours.getText().trim().isEmpty() ? "0" : txtSpecialWorkHours.getText().trim());
            double bonusAmount = Double.parseDouble(txtBonusAmount.getText().trim().isEmpty() ? "0" : txtBonusAmount.getText().trim());


            // --- 1. Salary Component Calculation ---
            double normalHours = Math.min(totalHours, NORMAL_HOURS_THRESHOLD);
            double normalSalary = normalHours * payRate;
            
            double overtimeSalary = overtimeHours * payRate * OVERTIME_MULTIPLIER;
            double doubletimeSalary = doubletimeHours * payRate * DOUBLETIME_MULTIPLIER;
            double extendedSalary = extendedHours * payRate * EXTENDED_MULTIPLIER;
            double holidayPaidSalary = holidayPaidHours * payRate * HOLIDAYPAID_MULTIPLIER;
            double specialWorkSalary = specialWorkHours * payRate * SPECIALWORK_MULTIPLIER;
            
            double grossSalary = normalSalary + overtimeSalary + doubletimeSalary + 
                                 extendedSalary + holidayPaidSalary + specialWorkSalary + 
                                 bonusAmount; 
                        
            // --- 2. Deduction Calculation ---
            double taxDeduction = grossSalary * TAX_PERCENTAGE;
            double medicalInsurance = grossSalary * MEDICAL_PERCENTAGE;
            double totalDeductions = taxDeduction + medicalInsurance;
            double netSalary = grossSalary - totalDeductions;

            // --- 3. Update TextFields ---
            txtNormalSalary.setText(String.format("%.2f", normalSalary));
            txtOvertimeSalary.setText(String.format("%.2f", overtimeSalary));
            txtDoubletimeSalary.setText(String.format("%.2f", doubletimeSalary));
            txtExtendedSalary.setText(String.format("%.2f", extendedSalary));
            txtHolidayPaidSalary.setText(String.format("%.2f", holidayPaidSalary));
            txtSpecialWorkSalary.setText(String.format("%.2f", specialWorkSalary));
            
            txtGrossSalary.setText(String.format("%.2f", grossSalary));
            txtTaxDeduction.setText(String.format("%.2f", taxDeduction));
            txtMedicalInsurance.setText(String.format("%.2f", medicalInsurance));
            txtTotalDeductions.setText(String.format("%.2f", totalDeductions));
            txtNetSalary.setText(String.format("%.2f", netSalary));
            
            lblStatus.setText("Salary calculated. Ready to Add.");
            lblStatus.setForeground(Color.BLUE);

        } catch (NumberFormatException e) {
            lblStatus.setText("❌ Error: Pay Rate, Bonus, and all Hour fields must contain valid numbers.");
            lblStatus.setForeground(Color.RED);
            clearCalculatedFields();
        }
    }
    
    private void clearCalculatedFields() {
        List<JTextField> calculatedFields = Arrays.asList(
            txtNormalSalary, txtOvertimeSalary, txtDoubletimeSalary, 
            txtExtendedSalary, txtHolidayPaidSalary, txtSpecialWorkSalary,
            txtGrossSalary, txtTaxDeduction, txtMedicalInsurance, 
            txtTotalDeductions, txtNetSalary
        );
        calculatedFields.forEach(f -> f.setText(""));
    }

    /**
     * Clears all fields in the form and reloads the next Employee ID.
     */
    private void clearFields() {
        List<JTextField> fieldsToClear = Arrays.asList(
            txtFirstName, txtLastName, txtContactNo, txtEmail, txtPayRate,
            txtTotalHours, txtOvertimeHours, txtDoubletimeHours, txtPayDate,
            txtSSN, txtHireDate, txtCustomer, txtBankName, txtAccountNo,
            txtExtendedHours, txtHolidayPaidHours, txtSpecialWorkHours, txtBonusAmount
        );
        fieldsToClear.forEach(f -> f.setText(""));
        clearCalculatedFields();

        cmbDepartment.setSelectedIndex(0);
        cmbDesignation.setSelectedIndex(0);
        
        loadLastEmployeeId();
        
        lblStatus.setText(" ");
        lblStatus.setForeground(Color.BLACK);
    }


    private void addEmployee() {
        
        // --- 1. Initial Data Validation and Parsing ---
        String employeeIdStr = txtEmployeeId.getText().trim();
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String department = cmbDepartment.getSelectedItem().toString();
        String designation = cmbDesignation.getSelectedItem().toString();
        String ssnStr = txtSSN.getText().trim();
        String contactNoStr = txtContactNo.getText().trim();
        String emailId = txtEmail.getText().trim();
        String hireDateStr = txtHireDate.getText().trim();
        String customer = txtCustomer.getText().trim(); 
        String bankName = txtBankName.getText().trim();
        String accountNoStr = txtAccountNo.getText().trim();
        
        
        // Check for required fields
        if (employeeIdStr.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || contactNoStr.isEmpty() || 
            emailId.isEmpty() || hireDateStr.isEmpty() || txtPayRate.getText().isEmpty() || 
            txtTotalHours.getText().isEmpty() || bankName.isEmpty() || accountNoStr.isEmpty() ||
            department.equals("-- Select --") || designation.equals("-- Select --") || 
            txtPayDate.getText().isEmpty()) 
        {
            lblStatus.setText("❌ Error: Please fill all required fields and select options.");
            lblStatus.setForeground(Color.RED);
            return;
        }

        // Check for calculated values
        if (txtNetSalary.getText().isEmpty()) {
            lblStatus.setText("❌ Error: Please press 'Calculate Salary' first.");
            lblStatus.setForeground(Color.RED);
            return;
        }

        // Name Validation (no special characters, no integers)
        if (!isValidName(firstName)) {
            lblStatus.setText("❌ Error: First Name must contain only letters and spaces.");
            lblStatus.setForeground(Color.RED);
            return;
        }
        if (!isValidName(lastName)) {
            lblStatus.setText("❌ Error: Last Name must contain only letters and spaces.");
            lblStatus.setForeground(Color.RED);
            return;
        }
        
        
        // Numeric Validation 
        int employeeId;
        double payRate, totalHours, overtimeHours, doubletimeHours, extendedHours, holidayPaidHours, specialWorkHours, bonusAmount;
        
        try {
            employeeId = Integer.parseInt(employeeIdStr);
            Long.parseLong(contactNoStr);
            Long.parseLong(accountNoStr);
            
            payRate = Double.parseDouble(txtPayRate.getText());
            totalHours = Double.parseDouble(txtTotalHours.getText());
            overtimeHours = Double.parseDouble(txtOvertimeHours.getText().isEmpty() ? "0" : txtOvertimeHours.getText());
            doubletimeHours = Double.parseDouble(txtDoubletimeHours.getText().isEmpty() ? "0" : txtDoubletimeHours.getText());
            extendedHours = Double.parseDouble(txtExtendedHours.getText().isEmpty() ? "0" : txtExtendedHours.getText());
            holidayPaidHours = Double.parseDouble(txtHolidayPaidHours.getText().isEmpty() ? "0" : txtHolidayPaidHours.getText());
            specialWorkHours = Double.parseDouble(txtSpecialWorkHours.getText().isEmpty() ? "0" : txtSpecialWorkHours.getText());
            bonusAmount = Double.parseDouble(txtBonusAmount.getText().isEmpty() ? "0" : txtBonusAmount.getText());
            
        } catch (NumberFormatException e) {
            lblStatus.setText("❌ Error: ID, Contact, Account No, Pay Rate, Bonus, and all Hour fields must be valid numbers.");
            lblStatus.setForeground(Color.RED);
            return;
        }

        // Date formatting 
        String sqlPayDateString;
        try {
            LocalDate date = LocalDate.parse(txtPayDate.getText().trim(), INPUT_DATE_FORMAT);
            sqlPayDateString = date.format(SQL_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            lblStatus.setText("❌ Error: Invalid pay date format. Must be MM/DD/YYYY (e.g., 11/15/2025).");
            lblStatus.setForeground(Color.RED);
            return;
        }
        
        String sqlHireDateString;
        try {
            LocalDate date = LocalDate.parse(hireDateStr, INPUT_DATE_FORMAT);
            sqlHireDateString = date.format(SQL_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            lblStatus.setText("❌ Error: Invalid hire date format. Must be MM/DD/YYYY (e.g., 10/04/2024).");
            lblStatus.setForeground(Color.RED);
            return;
        }


        // --- 2. Database Insertion ---
        // FIX: The auto-increment primary key (likely 'id' or 'row_id') is omitted from the column list, 
        // and 'employee_id' is moved to the end of the list. This provides 34 parameters, satisfying the database that wants 35 values.
        String sql = "INSERT INTO employees (first_name, last_name, SSN, designation, contact_no, email_id, hire_date, Total_hours, pay_rate_hourly, active, department, customer, bank_name, account_no, salary, overtime_hours, overtime_amount, doubletime_hours, doubletime_amount, extended_hours, extended_amount, holidaypaid_hours, holidaypaid_amount, specialwork_hours, specialwork_amount, bonus_amount, gross_salary, ytd_gross, tax_deduction, medical_insurance, net_salary, pay_date, total_deductions, employee_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Parameter 1-33 (shifted down by 1, employeeId is now 34)
            ps.setString(1, firstName.toUpperCase()); // WAS 2
            ps.setString(2, lastName.toUpperCase());   // WAS 3
            ps.setString(3, ssnStr.isEmpty() ? null : ssnStr); // WAS 4
            ps.setString(4, designation); // WAS 5
            ps.setString(5, contactNoStr); // WAS 6
            ps.setString(6, emailId.toLowerCase());  // WAS 7
            
            ps.setString(7, sqlHireDateString); // WAS 8
            ps.setDouble(8, totalHours); // WAS 9
            ps.setDouble(9, payRate); // WAS 10
            
            // Parameter 10: active status (WAS 11)
            ps.setInt(10, 1); 
            
            // Shifted bindings (11-14)
            ps.setString(11, department); // WAS 12
            ps.setString(12, customer.isEmpty() ? null : customer); // WAS 13
            ps.setString(13, bankName); // WAS 14
            ps.setString(14, accountNoStr); // WAS 15

            // Set calculated hours/amounts (15-26)
            ps.setDouble(15, Double.parseDouble(txtNormalSalary.getText())); // salary (normal) // WAS 16
            ps.setDouble(16, overtimeHours); // WAS 17
            ps.setDouble(17, Double.parseDouble(txtOvertimeSalary.getText())); // overtime_amount // WAS 18
            ps.setDouble(18, doubletimeHours); // WAS 19
            ps.setDouble(19, Double.parseDouble(txtDoubletimeSalary.getText())); // doubletime_amount // WAS 20
            
            ps.setDouble(20, extendedHours); // WAS 21
            ps.setDouble(21, Double.parseDouble(txtExtendedSalary.getText())); // extended_amount // WAS 22
            ps.setDouble(22, holidayPaidHours); // WAS 23
            ps.setDouble(23, Double.parseDouble(txtHolidayPaidSalary.getText())); // holidaypaid_amount // WAS 24
            ps.setDouble(24, specialWorkHours); // WAS 25
            ps.setDouble(25, Double.parseDouble(txtSpecialWorkSalary.getText())); // specialwork_amount // WAS 26
            ps.setDouble(26, bonusAmount); // bonus_amount (input) // WAS 27
            
            // Set final calculated values (27-33)
            ps.setDouble(27, Double.parseDouble(txtGrossSalary.getText())); // gross_salary // WAS 28
            
            // Parameter 28: ytd_gross 
            ps.setDouble(28, 0.0); // WAS 29
            
            ps.setDouble(29, Double.parseDouble(txtTaxDeduction.getText())); // tax_deduction // WAS 30
            ps.setDouble(30, Double.parseDouble(txtMedicalInsurance.getText())); // medical_insurance // WAS 31
            ps.setDouble(31, Double.parseDouble(txtNetSalary.getText())); // net_salary // WAS 32
            ps.setString(32, sqlPayDateString); // pay_date // WAS 33
            
            // Parameter 33: total_deductions 
            ps.setDouble(33, Double.parseDouble(txtTotalDeductions.getText())); // WAS 34
            
            // PARAMETER 34: employee_id (Moved from 1 to 34 to accommodate the assumed auto-increment ID column)
            ps.setInt(34, employeeId); // WAS 1

            ps.executeUpdate();
            
            lblStatus.setText("✅ Employee added successfully! ID: " + employeeId);
            lblStatus.setForeground(new Color(0, 128, 0));
            
            // Signal the parent GUI
            if (listener != null) {
                listener.onEmployeeAdded();
            }
            
            clearFields();

        } catch (SQLException ex) {
            String dbError;
            if (ex.getErrorCode() == 1062) { 
                 dbError = "❌ Database Error: Employee ID " + employeeId + " already exists (Primary Key Violation).";
            } else if (ex.getMessage().contains("Column count does not match value count")) {
                 dbError = "❌ Database Error: Column count mismatch. Your table structure might be different from expected 35 columns.";
            } else {
                 dbError = "❌ Database Error: " + ex.getMessage() + " (SQL State: " + ex.getSQLState() + ")";
            }
            
            lblStatus.setText(dbError);
            lblStatus.setForeground(Color.RED);
            ex.printStackTrace();
        } catch (Exception ex) {
            lblStatus.setText("❌ An unexpected error occurred: " + ex.getMessage());
            lblStatus.setForeground(Color.RED);
            ex.printStackTrace();
        }
    }
}