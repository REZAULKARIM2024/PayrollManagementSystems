package com.payroll;

import javax.swing.*;
import java.awt.*;

public class ViewEmployeeTab extends JPanel {

    private static final long serialVersionUID = 1L;
    
    // Reference to the main GUI frame to access shared methods like loading data
    private PayrollTabbedGUI mainGui;

    // ===============================================
    // 1. Constructor
    // ===============================================
    public ViewEmployeeTab(PayrollTabbedGUI mainGui) {
        
        // FIX: The call to the superclass constructor (super() or this()) 
        // must always be the first statement in the constructor.
        super(new BorderLayout()); 
        
        this.mainGui = mainGui;
        
        // The rest of the setup now follows.
        
        // This tab is intentionally simple because the actual JTable and 
        // search bar for viewing employees are created and managed by the 
        // main PayrollTabbedGUI.addViewTab() method.
        
        // This is a placeholder/confirmation that the tab exists.
        JLabel confirmationLabel = new JLabel(
            "<html><div style='text-align: center;'>"
            + "<h1>Employee Data View</h1>"
            + "The table and search functionality are handled by the main window."
            + "<br>Select the <b>View Employees</b> tab to see the live data."
            + "</div></html>", SwingConstants.CENTER);
        
        add(confirmationLabel, BorderLayout.CENTER);
    }

    // ===============================================
    // 2. Data Reload Method
    // ===============================================
    /**
     * This method is called by the main GUI (via the EmployeeAddedListener 
     * callback) to force a reload of the data from the database.
     */
    public void reloadEmployees() {
        System.out.println("LOG: View tab triggered data reload.");
        // Call the data loading method on the main GUI thread
        if (mainGui != null) {
            mainGui.loadEmployeesFromDB();
        }
    }
}
// Note: Removed the duplicated class definition that was causing the error.