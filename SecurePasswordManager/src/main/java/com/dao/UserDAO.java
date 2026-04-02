package com.dao;

import com.model.User;
import com.util.DBConnection;
import java.sql.*;

/**
 * UserDAO.java - Data Access Object for User operations.
 * 
 * Encapsulates ALL database queries related to the 'users' table.
 * Every method uses PreparedStatement to prevent SQL injection.
 * 
 * Responsibilities:
 * - User registration (INSERT)
 * - User lookup by username (SELECT)
 * - Failed login tracking & account locking (UPDATE)
 * - Last login time recording (UPDATE)
 * - Dashboard statistics (SELECT COUNT)
 */
public class UserDAO {
    
    /**
     * Registers a new user in the database.
     * 
     * The password must already be hashed (SHA-256) before calling this method.
     * Returns true if INSERT succeeds, false if username/email already exists.
     * 
     * @param user User object with username, email, and passwordHash set
     * @return true if registration successful, false otherwise
     */
    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPasswordHash());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLIntegrityConstraintViolationException e) {
            // Username or email already exists (UNIQUE constraint violated)
            System.err.println("[UserDAO] Registration failed - duplicate entry: " + e.getMessage());
            return false;
        } catch (SQLException e) {
            System.err.println("[UserDAO] Registration error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Retrieves a user by username.
     * Used during login to validate credentials.
     * 
     * @param username the username to search for
     * @return User object if found, null if not found
     */
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setFailedAttempts(rs.getInt("failed_attempts"));
                    user.setAccountLocked(rs.getBoolean("account_locked"));
                    user.setLastLogin(rs.getTimestamp("last_login"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error fetching user: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Checks if a username already exists in the database.
     * Used during registration to provide immediate feedback.
     * 
     * @param username the username to check
     * @return true if username is taken, false if available
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error checking username: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Checks if an email already exists in the database.
     * 
     * @param email the email to check
     * @return true if email is taken, false if available
     */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error checking email: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Increments the failed login attempt counter.
     * If attempts reach 5, the account is automatically locked.
     * 
     * This is called when a user enters an incorrect password.
     * 
     * @param username the username that failed login
     */
    public void incrementFailedAttempts(String username) {
        String sql = "UPDATE users SET failed_attempts = failed_attempts + 1, " +
                     "account_locked = CASE WHEN failed_attempts >= 4 THEN TRUE ELSE FALSE END " +
                     "WHERE username = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error incrementing failed attempts: " + e.getMessage());
        }
    }
    
    /**
     * Resets failed attempts to 0 after a successful login.
     * Also updates the last_login timestamp.
     * 
     * @param username the username that logged in successfully
     */
    public void resetFailedAttempts(String username) {
        String sql = "UPDATE users SET failed_attempts = 0, account_locked = FALSE, " +
                     "last_login = CURRENT_TIMESTAMP WHERE username = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error resetting failed attempts: " + e.getMessage());
        }
    }
    
    /**
     * Gets the total number of passwords stored by a user.
     * Used for dashboard statistics.
     * 
     * @param userId the user's database ID
     * @return count of vault entries
     */
    public int getPasswordCount(int userId) {
        String sql = "SELECT COUNT(*) FROM vault WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error getting password count: " + e.getMessage());
        }
        return 0;
    }
}
