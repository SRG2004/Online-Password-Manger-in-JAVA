package com.model;

import java.sql.Timestamp;

/**
 * Vault.java - Model Bean for stored credentials.
 * 
 * Represents a single credential entry in the user's password vault.
 * Maps directly to the 'vault' table in the database.
 * 
 * Security notes:
 * - encryptedPassword is AES-encrypted, NEVER plain text
 * - Decryption happens only for authenticated, authorized users
 * - The decryptedPassword field is transient (not stored in DB)
 */
public class Vault implements java.io.Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int id;
    private int userId;
    private String website;
    private String siteUsername;
    private String encryptedPassword;   // Stored in DB (AES-encrypted)
    private String decryptedPassword;   // Transient - only populated for display
    private Timestamp createdAt;
    
    // Default constructor
    public Vault() {}
    
    // Constructor for creating new entries
    public Vault(int userId, String website, String siteUsername, String encryptedPassword) {
        this.userId = userId;
        this.website = website;
        this.siteUsername = siteUsername;
        this.encryptedPassword = encryptedPassword;
    }
    
    // ======================== Getters & Setters ========================
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    
    public String getSiteUsername() { return siteUsername; }
    public void setSiteUsername(String siteUsername) { this.siteUsername = siteUsername; }
    
    public String getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; }
    
    public String getDecryptedPassword() { return decryptedPassword; }
    public void setDecryptedPassword(String decryptedPassword) { this.decryptedPassword = decryptedPassword; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return "Vault [id=" + id + ", website=" + website + ", siteUsername=" + siteUsername + "]";
    }
}
