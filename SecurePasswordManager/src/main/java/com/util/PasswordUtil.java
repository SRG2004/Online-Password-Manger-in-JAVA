package com.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * PasswordUtil.java - Password Hashing & Generation Utility
 * 
 * Provides:
 * 1. SHA-256 hashing for user login passwords (one-way)
 * 2. Strong random password generation
 * 3. Password strength evaluation
 * 
 * Security design:
 * - SHA-256 is a one-way hash — passwords cannot be recovered
 * - SecureRandom is used for cryptographically secure generation
 * - Strength checker uses multiple criteria (length, char variety)
 * 
 * Note: For production systems, use bcrypt/scrypt/Argon2 instead
 * of plain SHA-256. This project uses SHA-256 per requirements.
 */
public class PasswordUtil {
    
    // Character pools for password generation
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS    = "0123456789";
    private static final String SYMBOLS   = "!@#$%^&*()-_=+[]{}|;:',.<>?/~`";
    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SYMBOLS;
    
    /**
     * Hashes a plain-text password using SHA-256.
     * 
     * Process:
     * 1. Convert password string to bytes (UTF-8)
     * 2. Apply SHA-256 digest algorithm
     * 3. Convert resulting bytes to hex string
     * 
     * @param password the plain-text password to hash
     * @return 64-character hexadecimal SHA-256 hash
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes("UTF-8"));
            
            // Convert byte array to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                // Use 0xff mask to handle signed byte → unsigned conversion
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');  // Pad single digits with leading zero
                }
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            // SHA-256 and UTF-8 are guaranteed to exist in all JVMs
            throw new RuntimeException("Hashing failed", e);
        }
    }
    
    /**
     * Generates a cryptographically strong random password.
     * 
     * Guarantees at least one character from each category:
     * uppercase, lowercase, digit, symbol. The remaining characters
     * are randomly selected from the full character pool.
     * 
     * @param length desired password length (clamped to 8–32)
     * @return randomly generated password string
     */
    public static String generatePassword(int length) {
        // Clamp length to safe bounds
        if (length < 8) length = 8;
        if (length > 32) length = 32;
        
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);
        
        // Guarantee at least one from each category
        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));
        
        // Fill remaining length with random characters from all pools
        for (int i = 4; i < length; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }
        
        // Shuffle to avoid predictable pattern (first 4 chars always same types)
        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        
        return new String(chars);
    }
    
    /**
     * Evaluates password strength based on multiple criteria.
     * 
     * Scoring system (0–5 points):
     * +1 for length >= 8
     * +1 for containing uppercase letters
     * +1 for containing lowercase letters
     * +1 for containing digits
     * +1 for containing special characters
     * 
     * Ratings:
     * 0-2 = Weak, 3-4 = Medium, 5 = Strong
     * 
     * @param password the password to evaluate
     * @return "Weak", "Medium", or "Strong"
     */
    public static String checkStrength(String password) {
        if (password == null || password.isEmpty()) {
            return "Weak";
        }
        
        int score = 0;
        
        // Criterion 1: Minimum length
        if (password.length() >= 8) score++;
        
        // Criterion 2: Has uppercase letters
        if (password.matches(".*[A-Z].*")) score++;
        
        // Criterion 3: Has lowercase letters
        if (password.matches(".*[a-z].*")) score++;
        
        // Criterion 4: Has digits
        if (password.matches(".*[0-9].*")) score++;
        
        // Criterion 5: Has special characters
        if (password.matches(".*[^A-Za-z0-9].*")) score++;
        
        // Map score to human-readable strength
        if (score <= 2) return "Weak";
        if (score <= 4) return "Medium";
        return "Strong";
    }
    
    /**
     * Returns a CSS class name for styling the strength indicator.
     * Used by JSP pages to color-code the strength display.
     */
    public static String getStrengthClass(String strength) {
        switch (strength) {
            case "Strong": return "strength-strong";
            case "Medium": return "strength-medium";
            default:       return "strength-weak";
        }
    }
}
