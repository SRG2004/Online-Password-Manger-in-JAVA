package com.model;

import java.sql.Timestamp;

/**
 * User.java - Model Bean for user accounts.
 * 
 * Represents a registered user in the password manager system.
 * Maps directly to the 'users' table in the database.
 * 
 * Security notes:
 * - password_hash stores SHA-256 hash, never plain text
 * - failedAttempts tracks consecutive failed login attempts
 * - accountLocked prevents login after 5 failed attempts
 */
public class User implements java.io.Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private int failedAttempts;
    private boolean accountLocked;
    private Timestamp lastLogin;
    private Timestamp createdAt;
    
    // Default constructor required for JavaBeans
    public User() {}
    
    // Parameterized constructor for registration
    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.failedAttempts = 0;
        this.accountLocked = false;
    }
    
    // ======================== Getters & Setters ========================
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }
    
    public boolean isAccountLocked() { return accountLocked; }
    public void setAccountLocked(boolean accountLocked) { this.accountLocked = accountLocked; }
    
    public Timestamp getLastLogin() { return lastLogin; }
    public void setLastLogin(Timestamp lastLogin) { this.lastLogin = lastLogin; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return "User [id=" + id + ", username=" + username + ", email=" + email + "]";
    }
}
