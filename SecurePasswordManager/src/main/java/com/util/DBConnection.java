package com.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection.java - Database Connection Utility
 * 
 * Manages JDBC connections to the MySQL database.
 * Uses the MySQL Connector/J driver for connectivity.
 * 
 * Configuration:
 * - Update URL, USER, PASSWORD before deployment
 * - The JDBC driver is loaded once via static initializer
 * - Each call to getConnection() returns a NEW connection
 *   (caller is responsible for closing it)
 * 
 * Production note: In a real system, use connection pooling
 * (e.g., Apache DBCP). This simple approach is suitable for
 * learning and small-scale deployments.
 */
public class DBConnection {
    
    // ======================== Database Configuration ========================
    // Update these values to match your MySQL setup
    private static final String URL = "jdbc:mysql://localhost:3306/password_manager_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "root";  // Change this!
    
    // Load the MySQL JDBC driver once when the class is first loaded
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[DBConnection] MySQL JDBC Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] FATAL: MySQL JDBC Driver not found!");
            System.err.println("[DBConnection] Ensure mysql-connector-j-*.jar is in WEB-INF/lib/");
            throw new RuntimeException("MySQL Driver not found", e);
        }
    }
    
    /**
     * Returns a new database connection.
     * Caller MUST close the connection when done (use try-with-resources).
     * 
     * @return Connection object to the password_manager_db database
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    /**
     * Safely closes a connection (null-safe).
     * Convenience method for use in finally blocks.
     * 
     * @param conn the connection to close
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("[DBConnection] Error closing connection: " + e.getMessage());
            }
        }
    }
}
