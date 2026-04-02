package com.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * EncryptionUtil.java - AES Encryption/Decryption Utility
 * 
 * Provides symmetric AES encryption for vault passwords.
 * Each user gets a UNIQUE encryption key derived from their login password.
 * 
 * Key derivation process:
 * 1. Take the user's plain-text password (at login time)
 * 2. Hash it with SHA-256 to get 32 bytes
 * 3. Truncate to 16 bytes for AES-128 key
 * 
 * This means:
 * - Even if the database is stolen, passwords can't be decrypted
 *   without knowing each user's login password
 * - Different users have different encryption keys
 * - The master key is derived, never stored
 * 
 * Security note: AES/ECB mode is used for simplicity. Production
 * systems should use AES/GCM or AES/CBC with IV for better security.
 */
public class EncryptionUtil {
    
    private static final String ALGORITHM = "AES";
    
    /**
     * Derives a per-user AES-128 key from the user's raw password.
     * 
     * Process:
     * 1. SHA-256 hash the password → 32 bytes
     * 2. Truncate to first 16 bytes (AES-128 requires 128 bits)
     * 3. Create SecretKeySpec for AES
     * 
     * @param userPassword the user's plain-text password
     * @return AES SecretKeySpec derived from the password
     */
    private static SecretKeySpec deriveKey(String userPassword) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] key = sha.digest(userPassword.getBytes("UTF-8"));
            // Use only first 16 bytes for AES-128
            key = Arrays.copyOf(key, 16);
            return new SecretKeySpec(key, ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Key derivation failed", e);
        }
    }
    
    /**
     * Encrypts a plain-text password using AES with a per-user key.
     * 
     * Called when a user adds/updates a credential in the vault.
     * The encrypted result is Base64-encoded for safe storage in MySQL.
     * 
     * @param plainText    the password to encrypt (from the vault entry)
     * @param userPassword the logged-in user's password (for key derivation)
     * @return Base64-encoded encrypted string
     */
    public static String encrypt(String plainText, String userPassword) {
        try {
            SecretKeySpec key = deriveKey(userPassword);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    /**
     * Decrypts an AES-encrypted password using the user's derived key.
     * 
     * Called ONLY when displaying credentials to an authenticated user.
     * The encrypted value is first Base64-decoded, then AES-decrypted.
     * 
     * @param encryptedText Base64-encoded encrypted string (from DB)
     * @param userPassword  the logged-in user's password (for key derivation)
     * @return the original plain-text password
     */
    public static String decrypt(String encryptedText, String userPassword) {
        try {
            SecretKeySpec key = deriveKey(userPassword);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
