package com.dao;

import com.model.Vault;
import com.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * VaultDAO.java - Data Access Object for Password Vault operations.
 * 
 * Handles all CRUD operations on the 'vault' table.
 * All queries use PreparedStatement for SQL injection prevention.
 * 
 * Important: This DAO stores and retrieves ENCRYPTED passwords.
 * Encryption/decryption is handled at the servlet layer using EncryptionUtil.
 * The DAO layer never sees plain-text vault passwords.
 */
public class VaultDAO {
    
    /**
     * Adds a new credential entry to the user's vault.
     * The password must already be AES-encrypted before calling.
     * 
     * @param vault Vault object with userId, website, siteUsername, encryptedPassword
     * @return true if INSERT succeeds
     */
    public boolean addCredential(Vault vault) {
        String sql = "INSERT INTO vault (user_id, website, site_username, encrypted_password) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, vault.getUserId());
            pstmt.setString(2, vault.getWebsite());
            pstmt.setString(3, vault.getSiteUsername());
            pstmt.setString(4, vault.getEncryptedPassword());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("[VaultDAO] Error adding credential: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Retrieves all credentials for a specific user.
     * Returns encrypted passwords — decryption happens at the servlet layer.
     * 
     * Results are ordered by creation date (newest first).
     * 
     * @param userId the user's database ID
     * @return List of Vault objects (with encrypted passwords)
     */
    public List<Vault> getCredentialsByUserId(int userId) {
        List<Vault> credentials = new ArrayList<>();
        String sql = "SELECT * FROM vault WHERE user_id = ? ORDER BY created_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Vault vault = new Vault();
                    vault.setId(rs.getInt("id"));
                    vault.setUserId(rs.getInt("user_id"));
                    vault.setWebsite(rs.getString("website"));
                    vault.setSiteUsername(rs.getString("site_username"));
                    vault.setEncryptedPassword(rs.getString("encrypted_password"));
                    vault.setCreatedAt(rs.getTimestamp("created_at"));
                    credentials.add(vault);
                }
            }
        } catch (SQLException e) {
            System.err.println("[VaultDAO] Error fetching credentials: " + e.getMessage());
        }
        return credentials;
    }
    
    /**
     * Retrieves a single credential by its ID.
     * Used for edit/delete operations.
     * Validates ownership by checking user_id.
     * 
     * @param id     the vault entry ID
     * @param userId the authenticated user's ID (ownership check)
     * @return Vault object if found and owned by user, null otherwise
     */
    public Vault getCredentialById(int id, int userId) {
        String sql = "SELECT * FROM vault WHERE id = ? AND user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Vault vault = new Vault();
                    vault.setId(rs.getInt("id"));
                    vault.setUserId(rs.getInt("user_id"));
                    vault.setWebsite(rs.getString("website"));
                    vault.setSiteUsername(rs.getString("site_username"));
                    vault.setEncryptedPassword(rs.getString("encrypted_password"));
                    vault.setCreatedAt(rs.getTimestamp("created_at"));
                    return vault;
                }
            }
        } catch (SQLException e) {
            System.err.println("[VaultDAO] Error fetching credential by ID: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Updates an existing credential entry.
     * Only updates if the entry belongs to the specified user (security check).
     * 
     * @param vault Vault object with updated fields
     * @return true if UPDATE succeeds
     */
    public boolean updateCredential(Vault vault) {
        String sql = "UPDATE vault SET website = ?, site_username = ?, encrypted_password = ? " +
                     "WHERE id = ? AND user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, vault.getWebsite());
            pstmt.setString(2, vault.getSiteUsername());
            pstmt.setString(3, vault.getEncryptedPassword());
            pstmt.setInt(4, vault.getId());
            pstmt.setInt(5, vault.getUserId());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("[VaultDAO] Error updating credential: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Deletes a credential entry from the vault.
     * Only deletes if the entry belongs to the specified user (security check).
     * 
     * @param id     the vault entry ID to delete
     * @param userId the authenticated user's ID (ownership verification)
     * @return true if DELETE succeeds
     */
    public boolean deleteCredential(int id, int userId) {
        String sql = "DELETE FROM vault WHERE id = ? AND user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("[VaultDAO] Error deleting credential: " + e.getMessage());
            return false;
        }
    }
}
