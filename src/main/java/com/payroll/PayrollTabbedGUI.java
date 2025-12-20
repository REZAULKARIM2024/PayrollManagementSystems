package com.payroll;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

// ==============================================================
// PLACEHOLDER DEFINITIONS FOR DEPENDENT CLASSES
// NOTE: These are simplified or used to manage functionality.
// ==============================================================

// 1. ViewEmployeeTab - Manages reloading data
class ViewEmployeeTab extends JPanel {
    private PayrollTabbedGUI mainGui;
    public ViewEmployeeTab(PayrollTabbedGUI gui) {
        super(new BorderLayout()); 
        this.mainGui = gui;
        // Content handled by mainGui.addViewTab()
    }
    public void reloadEmployees() {
        System.out.println("LOG: View tab triggered data reload.");
        if (mainGui != null) {
            mainGui.loadEmployeesFromDB();
        }
    }
}

// 2. DeleteEmployeeTab
class DeleteEmployeeTab extends JPanel {
    public DeleteEmployeeTab() {
        super(new BorderLayout());
        add(new JLabel("Delete Employee Tab Logic Here", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}

// 3. WelcomeTab
class WelcomeTab extends JPanel {
    public WelcomeTab(JTabbedPane tabbedPane) {
        super(new BorderLayout());
        JLabel welcomeLabel = new JLabel("<html><h1 style='color: #4CAF50;'>Welcome to the Payroll System!</h1></html>", SwingConstants.CENTER);
        add(welcomeLabel, BorderLayout.CENTER);
    }
}

// 4. LoginTab
class LoginTab extends JPanel {
    public LoginTab(JTabbedPane tabbedPane) {
        super(new BorderLayout());
        add(new JLabel("Login Tab Logic Here", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}

// 5. RefreshTab
class RefreshTab extends JPanel {
    public RefreshTab(ViewEmployeeTab viewTab) {
        super(new BorderLayout());
        JButton refreshBtn = new JButton("Force Refresh Data");
        refreshBtn.addActionListener(e -> {
            viewTab.reloadEmployees();
            JOptionPane.showMessageDialog(this, "Data refresh triggered!", "Info", JOptionPane.INFORMATION_MESSAGE);
        });
        add(refreshBtn, BorderLayout.CENTER);
    }
}


// ==============================================================
// MAIN GUI CLASS
// ==============================================================
public class PayrollTabbedGUI extends JFrame implements AddEmployeeTab.EmployeeAddedListener {

    private static final long serialVersionUID = 1L;

    // ===============================================
    // 1. SHARED COMPONENTS & DB CONNECTION
    // ===============================================
    private DefaultTableModel tableModel;
    private JTable employeeTable;
    private JTabbedPane tabbedPane;
    private JTextField txtSearch;
    private Connection conn;

    // Tab references
    private UpdateEmployeeTab updateTab;
    private ViewEmployeeTab viewTab;

    // ===============================================
    // 2. MAIN CONSTRUCTOR
    // ===============================================
    public PayrollTabbedGUI() {
        setTitle("Payroll Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);

        connectToDB();

        // Table Columns (Must match loadEmployeesFromDB)
        String[] columnNames = {
                "ID", "First Name", "Last Name", "Department", "Designation",
                "Contact", "Email", "Pay Rate", "Hours", "OT Hours", "OT Amt",
                "DT Hours", "DT Amt", "Gross", "Tax", "Medical", "Deductions",
                "Net Salary", "Pay Date"
        };

        tableModel = new DefaultTableModel(columnNames, 0);
        employeeTable = new JTable(tableModel);

        tabbedPane = new JTabbedPane();

        // Instantiate Tabs (Using the fixed versions)
        updateTab = new UpdateEmployeeTab();
        viewTab = new ViewEmployeeTab(this);
        AddEmployeeTab addTab = new AddEmployeeTab(this); 
        
        DeleteEmployeeTab deleteTab = new DeleteEmployeeTab();
        SearchEmployeeTab searchTab = new SearchEmployeeTab(); // FIXED
        PayslipTab payslipTab = new PayslipTab(); // FIXED

        // Add Tabs to JTabbedPane
        tabbedPane.addTab("Welcome", new WelcomeTab(tabbedPane));
        tabbedPane.addTab("Login", new LoginTab(tabbedPane));
        tabbedPane.addTab("Add Employee", addTab);
        addViewTab(); // View Tab using shared JTable
        tabbedPane.addTab("Update Employee", updateTab);
        tabbedPane.addTab("Delete Employee", deleteTab);
        tabbedPane.addTab("Search Employee", searchTab);
        tabbedPane.addTab("Payslip", payslipTab);
        tabbedPane.addTab("Refresh", new RefreshTab(viewTab)); // Using viewTab for reload

        // --- Sidebar Button Panel ---
        JPanel sidebar = createSidebar();

        add(sidebar, BorderLayout.WEST);
        add(tabbedPane, BorderLayout.CENTER);

        // Apply theme/font
        setUIFont(new javax.swing.plaf.FontUIResource("Segoe UI", Font.PLAIN, 14));

        loadEmployeesFromDB(); 
    }

    // ===============================================
    // 3. UI Helper Methods
    // ===============================================

    private JPanel createSidebar() {
        // ... (Sidebar creation logic remains the same)
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(0, 1, 0, 3));
        sidebar.setPreferredSize(new Dimension(150, 0));

        String[] btnLabels = {
                "Welcome", "Login", "Add Employee", "View Employees", "Update Employee",
                "Delete Employee", "Search Employee", "Payslip", "Refresh", "Exit"
        };

        for (String label : btnLabels) {
            JButton btn = new JButton(label);
            btn.setFocusPainted(false);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            btn.addActionListener(e -> {
                if (label.equals("Exit")) {
                    System.exit(0);
                } else {
                    // Normalize label for JTabbedPane lookup (View Employees vs View Employees)
                    String tabTitle = label.equals("View Employees") ? "View Employees" : label; 
                    int index = tabbedPane.indexOfTab(tabTitle);
                    if (index != -1) tabbedPane.setSelectedIndex(index);
                }
            });
            sidebar.add(btn);
        }
        return sidebar;
    }

    private void addViewTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(employeeTable), BorderLayout.CENTER);

        // Search Bar Setup (Only for filtering the currently displayed table)
        JPanel searchPanel = new JPanel();
        txtSearch = new JTextField(20);
        searchPanel.add(new JLabel("Filter Table by Name:"));
        searchPanel.add(txtSearch);
        panel.add(searchPanel, BorderLayout.NORTH);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        employeeTable.setRowSorter(sorter);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { search(); }
            public void removeUpdate(DocumentEvent e) { search(); }
            public void changedUpdate(DocumentEvent e) { search(); }

            private void search() {
                String text = txtSearch.getText();
                // Filter rows based on First Name (col 1) and Last Name (col 2)
                sorter.setRowFilter(text.isEmpty() ? null :
                        RowFilter.regexFilter("(?i)" + text, 1, 2));
            }
        });

        // Row Selection Listener (Crucial for Update Tab loading)
        employeeTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && employeeTable.getSelectedRow() != -1) {
                int viewRow = employeeTable.getSelectedRow();
                int row = employeeTable.convertRowIndexToModel(viewRow);

                // Call the fixed method in UpdateEmployeeTab
                updateTab.loadSelectedRow(tableModel, row);
                tabbedPane.setSelectedIndex(tabbedPane.indexOfTab("Update Employee"));
            }
        });

        tabbedPane.addTab("View Employees", panel);
    }
    
    // ===============================================
    // 4. DB & DATA METHODS
    // ===============================================

    private void connectToDB() {
        try {
            conn = DBConnection.getConnection();
            System.out.println("LOG: Connected to MySQL!");
        } catch (SQLException e) {
            conn = null;
            System.err.println("ERROR: Cannot connect to DB: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "ERROR: Database connection failed. Check DBConnection.java and MySQL server status.\n" + e.getMessage(), 
                "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadEmployeesFromDB() {
        tableModel.setRowCount(0);

        if (conn == null) {
            System.err.println("DB connection is NULL! Cannot load employees.");
            return;
        }

        // Ensure you select all 19 columns required by the table model
        String sql = "SELECT employee_id, first_name, last_name, department, designation, contact_no, email_id, pay_rate_hourly, Total_hours, overtime_hours, overtime_amount, doubletime_hours, doubletime_amount, gross_salary, tax_deduction, medical_insurance, total_deductions, net_salary, pay_date FROM employees WHERE active = 1 ORDER BY employee_id";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                // Populate row vector based on 19 columns
                row.add(rs.getInt("employee_id"));             
                row.add(rs.getString("first_name"));           
                row.add(rs.getString("last_name"));            
                row.add(rs.getString("department"));           
                row.add(rs.getString("designation"));          
                row.add(rs.getString("contact_no"));           
                row.add(rs.getString("email_id"));             
                row.add(rs.getDouble("pay_rate_hourly"));      
                row.add(rs.getDouble("Total_hours"));          
                row.add(rs.getDouble("overtime_hours"));       
                row.add(rs.getDouble("overtime_amount"));      
                row.add(rs.getDouble("doubletime_hours"));     
                row.add(rs.getDouble("doubletime_amount"));    
                row.add(rs.getDouble("gross_salary"));         
                row.add(rs.getDouble("tax_deduction"));        
                row.add(rs.getDouble("medical_insurance"));    
                row.add(rs.getDouble("total_deductions"));     
                row.add(rs.getDouble("net_salary"));           
                row.add(rs.getDate("pay_date"));               

                tableModel.addRow(row);
            }
            System.out.println("LOG: Employee data loaded from MySQL. Total rows: " + tableModel.getRowCount());

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading employees: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===============================================
    // 5. LISTENER IMPLEMENTATION
    // ===============================================
    @Override
    public void onEmployeeAdded() {
        // Reloads the table view after an employee is added
        if (viewTab != null) {
            viewTab.reloadEmployees();
        }
    }

    // ===============================================
    // 6. UTILITY
    // ===============================================
    public static void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration<?> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

    // ===============================================
    // 7. MAIN METHOD
    // ===============================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            new PayrollTabbedGUI().setVisible(true);
        });
    }
}