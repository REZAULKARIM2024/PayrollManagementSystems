package com.payroll;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PayslipTab extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter INPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter SQL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");

    // Input fields
    private JTextField txtEmployeeId;
    private JTextField txtPayDate;
    private JButton btnGenerate;

    // Labels for Payslip
    private JLabel lblCompany, lblName, lblSSN, lblCheckDate;
    private JLabel lblYTDGross, lblGrossAmt, lblNetAmt, lblTotalDeduction;
    private JLabel lblAmountWords, lblPayTo;

    // Tables (Initialized here, but assigned in buildPayslipPanel)
    private JTable tblWeekWorked, tblTax, tblBank;

    // Labels and Panel for Cheque
    private JLabel lblChequeAmount, lblChequePayee, lblChequeDate, lblChequeWords;
    private JPanel chequePanel; 

    public PayslipTab() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        add(buildTopPanel(), BorderLayout.NORTH);
        
        // Payslip panel is in the center
        add(buildPayslipPanel(), BorderLayout.CENTER);
        
        // Cheque panel is in the South (Footer)
        add(buildChequePanel(), BorderLayout.SOUTH);
    }

    private JPanel buildTopPanel() {
        JPanel top = new JPanel(new BorderLayout());

        lblCompany = new JLabel("PAYROLL MANAGEMENT SYSTEM", SwingConstants.CENTER);
        lblCompany.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblCompany.setForeground(new Color(10, 90, 200));
        top.add(lblCompany, BorderLayout.NORTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Employee ID:"));
        txtEmployeeId = new JTextField(8);
        controls.add(txtEmployeeId);

        controls.add(new JLabel("Pay Date (MM/DD/YYYY):"));
        txtPayDate = new JTextField(10);
        txtPayDate.setToolTipText("Enter exact pay date (e.g., 10/11/2025)");
        controls.add(txtPayDate);

        btnGenerate = new JButton("Generate Payslip & Cheque");
        btnGenerate.addActionListener(e -> generatePayslip());
        controls.add(btnGenerate);

        top.add(controls, BorderLayout.SOUTH);
        return top;
    }

    private JPanel buildPayslipPanel() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createTitledBorder("Employee Payslip"));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        JLabel companyName = new JLabel("Transfotech, LLC");
        companyName.setFont(new Font("SansSerif", Font.BOLD, 15));
        header.add(companyName, BorderLayout.WEST);

        lblCheckDate = new JLabel("Check Date: ");
        lblCheckDate.setHorizontalAlignment(SwingConstants.RIGHT);
        lblCheckDate.setFont(new Font("SansSerif", Font.PLAIN, 15));
        header.add(lblCheckDate, BorderLayout.EAST);
        root.add(header);

        // Employee info row
        JPanel nameRow = new JPanel(new GridLayout(1, 2));
        lblName = new JLabel("Name: ");
        lblName.setFont(new Font("SansSerif", Font.BOLD, 15));

        lblSSN = new JLabel("SSN: xxx-xx-0000", SwingConstants.RIGHT);
        lblSSN.setFont(new Font("SansSerif", Font.PLAIN, 15));

        nameRow.add(lblName);
        nameRow.add(lblSSN);
        root.add(nameRow);

        root.add(Box.createVerticalStrut(8));

        // Initialize Tables (Fixing the compilation error)
        tblWeekWorked = createTable(new String[]{
                "Company", "Department", "Reg. Hrs", "Pay Rate", "Reg. Pay",
                "OT Hrs", "OT Pay", "DT Hrs", "DT Pay", "Gross"
        });
        tblTax = createTable(new String[]{"Tax Deduction", "Medical Insurance", "Total Deductions"});
        tblBank = createTable(new String[]{"Bank Name", "Account No.", "Net Salary"});

        root.add(new JScrollPane(tblWeekWorked));
        root.add(Box.createVerticalStrut(6));
        root.add(new JScrollPane(tblTax));
        root.add(Box.createVerticalStrut(6));
        root.add(new JScrollPane(tblBank));
        root.add(Box.createVerticalStrut(8));

        // Summary panel
        JPanel summary = new JPanel(new GridLayout(1, 4));
        lblYTDGross = createSummaryLabel("YTD Gross: $");
        lblGrossAmt = createSummaryLabel("Gross Salary: $");
        lblTotalDeduction = createSummaryLabel("Total Deductions: $");
        lblNetAmt = createSummaryLabel("Net Salary: $");

        summary.add(lblYTDGross);
        summary.add(lblGrossAmt);
        summary.add(lblTotalDeduction);
        summary.add(lblNetAmt);
        root.add(summary);

        root.add(Box.createVerticalStrut(8));

        lblAmountWords = new JLabel("Amount in words: ");
        lblAmountWords.setFont(new Font("SansSerif", Font.PLAIN, 15));
        lblPayTo = new JLabel("PAY TO THE ORDER OF: ");
        lblPayTo.setFont(new Font("SansSerif", Font.BOLD, 15));

        root.add(lblAmountWords);
        root.add(Box.createVerticalStrut(6));
        root.add(lblPayTo);

        return root;
    }
    
    private JPanel buildChequePanel() {
        chequePanel = new JPanel();
        chequePanel.setLayout(new BorderLayout());
        chequePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 2), "Cheque Preview", 0, 0, new Font("SansSerif", Font.BOLD, 14), Color.RED));
        chequePanel.setBackground(new Color(240, 240, 255)); 

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        content.setBackground(new Color(240, 240, 255));

        // Top Row: Bank Name and Amount
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(content.getBackground());
        JLabel bankHeader = new JLabel("Transfotech Bank, NA", SwingConstants.LEFT);
        bankHeader.setFont(new Font("Serif", Font.ITALIC, 20));
        topRow.add(bankHeader, BorderLayout.WEST);

        lblChequeDate = new JLabel("Date: (MM/DD/YYYY)", SwingConstants.RIGHT);
        lblChequeDate.setFont(new Font("SansSerif", Font.BOLD, 14));
        topRow.add(lblChequeDate, BorderLayout.EAST);
        content.add(topRow);

        content.add(Box.createVerticalStrut(15));
        
        // Amount Box
        JPanel amountBox = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        amountBox.setBackground(content.getBackground());
        JLabel amountLabel = new JLabel("Amount: $");
        amountLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lblChequeAmount = new JLabel("0.00");
        lblChequeAmount.setFont(new Font("Monospaced", Font.BOLD, 22));
        lblChequeAmount.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        amountBox.add(amountLabel);
        amountBox.add(lblChequeAmount);
        
        JPanel payToRow = new JPanel(new BorderLayout(10, 0));
        payToRow.setBackground(content.getBackground());
        JLabel payToLabel = new JLabel("Pay to the Order of:");
        payToLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblChequePayee = new JLabel("___________________________________");
        lblChequePayee.setFont(new Font("SansSerif", Font.BOLD, 16));
        payToRow.add(payToLabel, BorderLayout.WEST);
        payToRow.add(lblChequePayee, BorderLayout.CENTER);
        payToRow.add(amountBox, BorderLayout.EAST);
        content.add(payToRow);
        
        content.add(Box.createVerticalStrut(15));

        // Amount in Words
        lblChequeWords = new JLabel("________________________________________________________________");
        lblChequeWords.setFont(new Font("SansSerif", Font.PLAIN, 16));
        JPanel wordsPanel = new JPanel(new BorderLayout());
        wordsPanel.setBackground(content.getBackground());
        wordsPanel.add(lblChequeWords, BorderLayout.CENTER);
        wordsPanel.add(new JLabel("DOLLARS"), BorderLayout.EAST);
        content.add(wordsPanel);

        content.add(Box.createVerticalStrut(40));

        // Signature Line
        JPanel signaturePanel = new JPanel(new BorderLayout());
        signaturePanel.setBackground(content.getBackground());
        signaturePanel.add(new JLabel("For: Payroll Payment"), BorderLayout.WEST);
        JLabel signatureLine = new JLabel("X _______________________________");
        signatureLine.setHorizontalAlignment(SwingConstants.RIGHT);
        signaturePanel.add(signatureLine, BorderLayout.EAST);
        content.add(signaturePanel);
        
        chequePanel.add(content, BorderLayout.CENTER);
        
        // MICR Line (Simulated)
        JLabel micrLine = new JLabel("  :999999999:  :000000000:  9999", SwingConstants.CENTER);
        micrLine.setFont(new Font("Monospaced", Font.BOLD, 16));
        chequePanel.add(micrLine, BorderLayout.SOUTH);

        return chequePanel;
    }


    private JTable createTable(String[] columns) {
        JTable table = new JTable(new DefaultTableModel(columns, 0));
        table.setFont(new Font("SansSerif", Font.PLAIN, 15));
        table.setRowHeight(22);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        return table;
    }

    private JLabel createSummaryLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        return lbl;
    }

    private void generatePayslip() {
        String empIdStr = txtEmployeeId.getText().trim();
        String payDateStr = txtPayDate.getText().trim();

        if (empIdStr.isEmpty() || payDateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Employee ID এবং Pay Date অবশ্যই দিতে হবে!", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int empId;
        try {
            empId = Integer.parseInt(empIdStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Employee ID অবশ্যই একটি সঠিক সংখ্যা হতে হবে।", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sqlPayDate;
        try {
            LocalDate parsed = LocalDate.parse(payDateStr, INPUT_DATE_FORMAT);
            sqlPayDate = parsed.format(SQL_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "তারিখের বিন্যাস ভুল। MM/DD/YYYY বিন্যাস ব্যবহার করুন।", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        fetchPayslipFromDB(empId, sqlPayDate, payDateStr);
    }

    private void fetchPayslipFromDB(int empId, String sqlPayDate, String displayPayDate) {
        // SQL query uses employee_id AND pay_date, as both fields are now confirmed to be in the 'employees' table.
        String sql = "SELECT * FROM employees WHERE employee_id = ? AND pay_date = ?"; 
        
        try (Connection conn = DBConnection.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, empId);
            ps.setString(2, sqlPayDate); 

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    populatePayslip(rs, displayPayDate);
                    populateCheque(rs, displayPayDate);
                    JOptionPane.showMessageDialog(this, "Payslip এবং Cheque তৈরি করা হয়েছে।");
                } else {
                    clearPayslipDisplay();
                    clearChequeDisplay();
                    JOptionPane.showMessageDialog(this, "এই Employee ID এবং Pay Date-এর জন্য কোনো Payroll Record খুঁজে পাওয়া যায়নি।");
                }
            }

        } catch (SQLException ex) {
            clearPayslipDisplay();
            clearChequeDisplay();
            Logger.getLogger(PayslipTab.class.getName()).log(Level.SEVERE, "Database Error", ex);
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            Logger.getLogger(PayslipTab.class.getName()).log(Level.SEVERE, "Unexpected Error", ex);
            JOptionPane.showMessageDialog(this, "একটি অপ্রত্যাশিত ত্রুটি ঘটেছে: " + ex.getMessage(), "General Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populatePayslip(ResultSet rs, String displayPayDate) throws SQLException {
        String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
        
        // --- Fetching Pre-calculated Data Directly from the employees table ---
        double payRate = rs.getDouble("pay_rate_hourly");
        double regularHours = Math.min(rs.getDouble("Total_hours"), 40); // Max 40 regular hours
        double regularPay = regularHours * payRate; // Calculate Reg. Pay manually

        // Fetching pre-calculated amounts for gross
        double overtimeHours = rs.getDouble("overtime_hours");
        double overtimePay = rs.getDouble("overtime_amount"); 
        double doubleTimeHours = rs.getDouble("doubletime_hours");
        double doubleTimePay = rs.getDouble("doubletime_amount"); 
        
        double grossSalary = rs.getDouble("gross_salary");
        double totalDeductions = rs.getDouble("total_deductions");
        double netSalary = rs.getDouble("net_salary"); 
        // --------------------------------------------------------------------

        // Update Labels
        lblName.setText("Name: " + fullName);
        lblSSN.setText("SSN: xxx-xx-0000"); 
        lblCheckDate.setText("Check Date: " + displayPayDate);

        // Clear previous data
        ((DefaultTableModel) tblWeekWorked.getModel()).setRowCount(0);
        ((DefaultTableModel) tblTax.getModel()).setRowCount(0);
        ((DefaultTableModel) tblBank.getModel()).setRowCount(0);

        // Week Worked table
        DefaultTableModel weekModel = (DefaultTableModel) tblWeekWorked.getModel();
        weekModel.addRow(new Object[]{
                rs.getString("customer"),
                rs.getString("department"),
                regularHours,
                MONEY.format(payRate),
                MONEY.format(regularPay),
                overtimeHours,
                MONEY.format(overtimePay),
                doubleTimeHours,
                MONEY.format(doubleTimePay),
                MONEY.format(grossSalary)
        });

        // Tax table
        DefaultTableModel taxModel = (DefaultTableModel) tblTax.getModel();
        taxModel.addRow(new Object[]{
                MONEY.format(rs.getDouble("tax_deduction")),
                MONEY.format(rs.getDouble("medical_insurance")),
                MONEY.format(totalDeductions)
        });

        // Bank table
        DefaultTableModel bankModel = (DefaultTableModel) tblBank.getModel();
        bankModel.addRow(new Object[]{
                rs.getString("bank_name"),
                rs.getString("account_no"),
                MONEY.format(netSalary)
        });

        // Summary
        lblYTDGross.setText("YTD Gross: $" + MONEY.format(rs.getDouble("ytd_gross")));
        lblGrossAmt.setText("Gross Salary: $" + MONEY.format(grossSalary));
        lblTotalDeduction.setText("Total Deductions: $" + MONEY.format(totalDeductions));
        lblNetAmt.setText("Net Salary: $" + MONEY.format(netSalary));

        // Convert net salary to words dynamically
        String amountInWords = convertAmountToWords(netSalary);
        lblAmountWords.setText("Amount in words: " + amountInWords);
        lblPayTo.setText("PAY TO THE ORDER OF: " + fullName);
    }
    
    private void populateCheque(ResultSet rs, String displayPayDate) throws SQLException {
        String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
        double netSalary = rs.getDouble("net_salary");
        String formattedNetSalary = MONEY.format(netSalary);
        String amountInWords = convertAmountToWords(netSalary);
        
        lblChequeDate.setText("Date: " + displayPayDate);
        lblChequePayee.setText(fullName);
        lblChequeAmount.setText(formattedNetSalary);
        
        String wordsForCheque = amountInWords.toUpperCase().replace(" DOLLARS", "").replace(" AND ", " AND ").replace(" CENTS", "/100");
        lblChequeWords.setText(wordsForCheque);
    }


    private void clearPayslipDisplay() {
        lblName.setText("Name: ");
        lblSSN.setText("SSN: xxx-xx-0000");
        lblCheckDate.setText("Check Date: ");

        ((DefaultTableModel) tblWeekWorked.getModel()).setRowCount(0);
        ((DefaultTableModel) tblTax.getModel()).setRowCount(0);
        ((DefaultTableModel) tblBank.getModel()).setRowCount(0);

        lblYTDGross.setText("YTD Gross: $");
        lblGrossAmt.setText("Gross Salary: $");
        lblTotalDeduction.setText("Total Deductions: $");
        lblNetAmt.setText("Net Salary: $");

        lblAmountWords.setText("Amount in words: ");
        lblPayTo.setText("PAY TO THE ORDER OF: ");
    }
    
    private void clearChequeDisplay() {
        lblChequeDate.setText("Date: (MM/DD/YYYY)");
        lblChequePayee.setText("___________________________________");
        lblChequeAmount.setText("0.00");
        lblChequeWords.setText("______________RK__________________________________________________");
    }

    // ------------------- Number to words converter -------------------
    private static final String[] tensNames = {
            "", " Ten", " Twenty", " Thirty", " Forty", " Fifty", " Sixty", " Seventy", " Eighty", " Ninety"
    };

    private static final String[] numNames = {
            "", " One", " Two", " Three", " Four", " Five", " Six", " Seven", " Eight", " Nine",
            " Ten", " Eleven", " Twelve", " Thirteen", " Fourteen", " Fifteen", " Sixteen", " Seventeen",
            " Eighteen", " Nineteen"
    };

    private String convertAmountToWords(double amount) {
        if (amount == 0) return "Zero Dollars";

        long dollars = (long) amount;
        long cents = Math.round((amount - dollars) * 100);

        String words = convertNumberToWords(dollars) + " Dollars";
        if (cents > 0) {
            words += " and " + convertNumberToWords(cents) + " Cents";
        }
        return words.trim();
    }
    
    private String convertNumberToWords(long number) {
        if (number == 0) return "";

        String current;
        int group = (int) (number % 1000);
        number /= 1000;
        
        if (group == 0) {
            current = "";
        } else if (group < 20) {
            current = numNames[group];
        } else if (group < 100) {
            int tens = group / 10;
            int ones = group % 10;
            current = tensNames[tens] + numNames[ones];
        } else {
            int hundreds = group / 100;
            int remainder = group % 100;
            if (remainder < 20) {
                current = numNames[hundreds] + " Hundred" + numNames[remainder];
            } else {
                int tens = remainder / 10;
                int ones = remainder % 10;
                current = numNames[hundreds] + " Hundred" + tensNames[tens] + numNames[ones];
            }
        }

        String result = current;
        
        long thousands = number % 1000;
        number /= 1000;

        if (thousands > 0) {
            result = convertHundreds(thousands) + " Thousand" + result;
        }

        long millions = number % 1000;
        number /= 1000;

        if (millions > 0) {
            result = convertHundreds(millions) + " Million" + result;
        }
        
        return result.trim();
    }
    
    private String convertHundreds(long number) {
        if (number == 0) return "";

        String current;
        int group = (int) number;

        if (group < 20) {
            current = numNames[group];
        } else if (group < 100) {
            int tens = group / 10;
            int ones = group % 10;
            current = tensNames[tens] + numNames[ones];
        } else {
            int hundreds = group / 100;
            int remainder = group % 100;
            if (remainder < 20) {
                current = numNames[hundreds] + " Hundred" + numNames[remainder];
            } else {
                int tens = remainder / 10;
                int ones = remainder % 10;
                current = numNames[hundreds] + " Hundred" + tensNames[tens] + numNames[ones];
            }
        }
        return current.trim();
    }
}