package com.payroll;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // --- UPDATE THESE WITH YOUR ACTUAL DATABASE DETAILS ---
    private static final String URL = "jdbc:mysql://localhost:3306/payroll_db";
    private static final String USER = "admin"; // Common default, change if necessary
    private static final String PASSWORD = "admin123"; // Change if necessary
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}