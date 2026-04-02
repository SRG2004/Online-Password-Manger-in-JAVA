package com.dao;

import com.model.Log;
import com.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * LogDAO.java - Data Access Object for Activity Logging.
 * 
 * Records all security-relevant user actions for audit purposes.
 * Every log entry includes the action description, timestamp, and IP address.
 * 
 * Logged actions:
 * - LOGIN_SUCCESS, LOGIN_FAILED, LOGIN_LOCKED
 * - LOGOUT
 * - PASSWORD_ADDED, PASSWORD_UPDATED, PASSWORD_DELETED
 * - REGISTRATION
 * 
 * This audit trail is essential for:
 * - Security incident investigation
 * - User activity monitoring
 * - Compliance requirements
 */
public class LogDAO {
    
    /**
     * Records a new activity log entry.
     * 
     * @param userId    the user who performed the action
     * @param action    description of the action (e.g., "LOGIN_SUCCESS")
     * @param ipAddress the client's IP address (from request.getRemoteAddr())
     * @return true if log entry was recorded successfully
     */
    public boolean addLog(int userId, String action, String ipAddress) {
        String sql = "INSERT INTO logs (user_id, action, ip_address) VALUES (?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, action);
            pstmt.setString(3, ipAddress);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("[LogDAO] Error adding log: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Retrieves recent activity logs for a specific user.
     * Returns the last 50 log entries, ordered by most recent first.
     * 
     * @param userId the user whose logs to retrieve
     * @return List of Log objects
     */
    public List<Log> getLogsByUserId(int userId) {
        List<Log> logs = new ArrayList<>();
        String sql = "SELECT * FROM logs WHERE user_id = ? ORDER BY timestamp DESC LIMIT 50";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Log log = new Log();
                    log.setId(rs.getInt("id"));
                    log.setUserId(rs.getInt("user_id"));
                    log.setAction(rs.getString("action"));
                    log.setIpAddress(rs.getString("ip_address"));
                    log.setTimestamp(rs.getTimestamp("timestamp"));
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            System.err.println("[LogDAO] Error fetching logs: " + e.getMessage());
        }
        return logs;
    }
    
    /**
     * Gets the most recent login timestamp for a user.
     * Used for "Last Login" display on the dashboard.
     * 
     * @param userId the user's database ID
     * @return formatted timestamp string, or "Never" if no login recorded
     */
    public String getLastLoginTime(int userId) {
        String sql = "SELECT timestamp FROM logs WHERE user_id = ? AND action = 'LOGIN_SUCCESS' " +
                     "ORDER BY timestamp DESC LIMIT 1";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("timestamp");
                    return ts.toString();
                }
            }
        } catch (SQLException e) {
            System.err.println("[LogDAO] Error getting last login: " + e.getMessage());
        }
        return "Never";
    }
}
