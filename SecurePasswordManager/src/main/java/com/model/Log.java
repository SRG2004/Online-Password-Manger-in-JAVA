package com.model;

import java.sql.Timestamp;

/**
 * Log.java - Model Bean for activity/audit logs.
 * 
 * Represents a single audit log entry tracking user actions.
 * Maps directly to the 'logs' table in the database.
 * 
 * Every security-relevant action (login, logout, add/delete password)
 * is recorded with timestamp and client IP address for forensics.
 */
public class Log implements java.io.Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int id;
    private int userId;
    private String action;
    private String ipAddress;
    private Timestamp timestamp;
    
    // Default constructor
    public Log() {}
    
    // Constructor for creating new log entries
    public Log(int userId, String action, String ipAddress) {
        this.userId = userId;
        this.action = action;
        this.ipAddress = ipAddress;
    }
    
    // ======================== Getters & Setters ========================
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    
    @Override
    public String toString() {
        return "Log [userId=" + userId + ", action=" + action + ", timestamp=" + timestamp + "]";
    }
}
