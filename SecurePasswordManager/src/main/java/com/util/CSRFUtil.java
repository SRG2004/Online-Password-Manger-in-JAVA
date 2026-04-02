package com.util;

import java.security.SecureRandom;
import java.util.Base64;
import javax.servlet.http.HttpSession;

/**
 * CSRFUtil.java - Cross-Site Request Forgery Protection
 * 
 * Implements the Synchronizer Token Pattern to prevent CSRF attacks:
 * 1. Generate a random token and store it in the user's session
 * 2. Include the token as a hidden field in every form
 * 3. On form submission, validate that the submitted token matches the session token
 * 
 * Attack scenario prevented:
 * A malicious site cannot forge a POST request because it doesn't
 * know the CSRF token stored in the victim's session.
 */
public class CSRFUtil {
    
    private static final String CSRF_TOKEN_ATTR = "csrfToken";
    
    /**
     * Generates a new CSRF token and stores it in the session.
     * 
     * Uses SecureRandom for cryptographic strength — the token
     * cannot be predicted by an attacker.
     * 
     * @param session the user's HttpSession
     * @return the generated CSRF token string
     */
    public static String generateToken(HttpSession session) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(CSRF_TOKEN_ATTR, token);
        return token;
    }
    
    /**
     * Validates a submitted CSRF token against the session token.
     * 
     * Uses constant-time comparison to prevent timing attacks.
     * After validation, the token is regenerated (one-time use).
     * 
     * @param session        the user's HttpSession
     * @param submittedToken the token from the form submission
     * @return true if the token is valid, false otherwise
     */
    public static boolean validateToken(HttpSession session, String submittedToken) {
        if (session == null || submittedToken == null) {
            return false;
        }
        
        String sessionToken = (String) session.getAttribute(CSRF_TOKEN_ATTR);
        if (sessionToken == null) {
            return false;
        }
        
        // Constant-time comparison to prevent timing attacks
        boolean valid = MessageDigestIsEqual(sessionToken, submittedToken);
        
        // Regenerate token after validation (one-time use)
        if (valid) {
            generateToken(session);
        }
        
        return valid;
    }
    
    /**
     * Constant-time string comparison.
     * Prevents timing side-channel attacks where an attacker
     * measures response time to guess the token character by character.
     */
    private static boolean MessageDigestIsEqual(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
    
    /**
     * Gets the current CSRF token from session, or generates a new one.
     * Used by JSP pages to include in forms.
     * 
     * @param session the user's HttpSession
     * @return the CSRF token
     */
    public static String getToken(HttpSession session) {
        String token = (String) session.getAttribute(CSRF_TOKEN_ATTR);
        if (token == null) {
            token = generateToken(session);
        }
        return token;
    }
}
