Payroll Management System

A robust desktop application built with Java and MySQL designed to streamline employee salary management, attendance tracking, and payroll generation.
🚀 Features

    Secure Authentication: User-friendly login and signup system.

    Employee Records: Add, update, and delete employee details.

    Salary Calculation: Automated calculation of HRA, DA, PF, and net salary.

    Attendance Tracking: Monitor daily presence and leave records.

    Database Integration: Persistent data storage using MySQL.

    Generate Pay Slips: View and print professional salary slips.

🛠️ Tech Stack

    Frontend/Logic: Java (Swing/AWT for GUI)

    Database: MySQL

    Tools: JDBC (Java Database Connectivity)

📋 Prerequisites

Before you begin, ensure you have the following installed:

    Java Development Kit (JDK) 8 or higher.

    MySQL Server and MySQL Workbench.

    MySQL Connector/J (the JDBC driver).

⚙️ Installation & Setup
1. Database Configuration

    Open MySQL Workbench and create a new database:
    SQL

    CREATE DATABASE payroll_db;

    Import the provided .sql file (if available in the repo) or create the necessary tables for employees and login.

2. Project Setup

    Clone the repository:
    Bash

    git clone https://github.com/your-username/PayrollManagementSystems.git

    Open the project in your favorite IDE (IntelliJ, Eclipse, or NetBeans).

    Add the MySQL Connector JAR file to your project’s build path.

3. Update Connection Strings

Navigate to your database connection class (e.g., Conn.java) and update your credentials:
Java

connection = DriverManager.getConnection("jdbc:mysql:///payroll_db", "root", "YOUR_PASSWORD");

🖥️ Usage

    Run the Login.java or Splash.java file to start the application.

    Sign Up: Create a new account if you are a first-time user.

    Login: Use your credentials to access the dashboard.

    Manage: Use the navigation menu to add employees or calculate monthly payroll.
