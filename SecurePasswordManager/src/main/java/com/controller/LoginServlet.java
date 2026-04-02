package com.controller;

import com.dao.UserDAO;
import com.dao.LogDAO;
import com.model.User;
import com.util.PasswordUtil;
import com.util.CSRFUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * LoginServlet.java - Handles user authentication.
 * 
 * Flow:
 * GET  /LoginServlet → Forward to login.jsp (with CSRF token)
 * POST /LoginServlet → Validate credentials and create session
 * 
 * Security features:
 * 1. SHA-256 password comparison (never stores plain text)
 * 2. Account lockout after 5 failed attempts
 * 3. CSRF token validation on form submission
 * 4. Session creation with userId and username
 * 5. Session timeout: 5 minutes (300 seconds)
 * 6. Activity logging with IP address
 * 7. Stores user's plain password in session for AES key derivation
 *    (needed to decrypt vault passwords later)
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO = new UserDAO();
    private LogDAO logDAO = new LogDAO();
    
    /**
     * Displays the login page with a fresh CSRF token.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Generate CSRF token for the login form
        HttpSession session = request.getSession(true);
        String csrfToken = CSRFUtil.generateToken(session);
        request.setAttribute("csrfToken", csrfToken);
        
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
    
    /**
     * Processes login form submission.
     * 
     * Steps:
     * 1. Extract and validate form data
     * 2. Verify CSRF token
     * 3. Look up user by username
     * 4. Check if account is locked
     * 5. Compare password hashes
     * 6. On success: create session, log event, redirect to dashboard
     * 7. On failure: increment failed attempts, log event, show error
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // ==================== Input Extraction ====================
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String csrfToken = request.getParameter("csrfToken");
        String clientIP = request.getRemoteAddr();
        
        // ==================== Input Validation ====================
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            redirectWithError(request, response, "All fields are required.");
            return;
        }
        
        username = username.trim();
        password = password.trim();
        
        // ==================== CSRF Validation ====================
        HttpSession session = request.getSession(false);
        if (session == null || !CSRFUtil.validateToken(session, csrfToken)) {
            redirectWithError(request, response, "Invalid request. Please try again.");
            return;
        }
        
        // ==================== User Lookup ====================
        User user = userDAO.getUserByUsername(username);
        
        if (user == null) {
            // User does not exist — generic error to prevent username enumeration
            redirectWithError(request, response, "Invalid username or password.");
            return;
        }
        
        // ==================== Account Lock Check ====================
        if (user.isAccountLocked()) {
            logDAO.addLog(user.getId(), "LOGIN_LOCKED", clientIP);
            redirectWithError(request, response, 
                "Account is locked due to too many failed attempts. Contact administrator.");
            return;
        }
        
        // ==================== Password Verification ====================
        String inputHash = PasswordUtil.hashPassword(password);
        
        if (inputHash.equals(user.getPasswordHash())) {
            // ===== LOGIN SUCCESS =====
            
            // Reset failed attempts counter
            userDAO.resetFailedAttempts(username);
            
            // Create new session (invalidate old one for security)
            if (session != null) {
                session.invalidate();
            }
            session = request.getSession(true);
            
            // Set session attributes
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("email", user.getEmail());
            // Store password in session for AES key derivation (vault decryption)
            session.setAttribute("userPassword", password);
            
            // Set session timeout: 5 minutes (300 seconds)
            session.setMaxInactiveInterval(300);
            
            // Log successful login
            logDAO.addLog(user.getId(), "LOGIN_SUCCESS", clientIP);
            
            // Redirect to dashboard
            response.sendRedirect(request.getContextPath() + "/dashboard.jsp");
            
        } else {
            // ===== LOGIN FAILURE =====
            
            // Increment failed attempts (may trigger account lock)
            userDAO.incrementFailedAttempts(username);
            
            // Log failed login attempt
            logDAO.addLog(user.getId(), "LOGIN_FAILED", clientIP);
            
            // Calculate remaining attempts
            int remaining = 4 - user.getFailedAttempts();
            String message = "Invalid username or password.";
            if (remaining > 0 && remaining <= 3) {
                message += " " + remaining + " attempt(s) remaining before lockout.";
            } else if (remaining <= 0) {
                message = "Account has been locked due to too many failed attempts.";
            }
            
            redirectWithError(request, response, message);
        }
    }
    
    /**
     * Helper: Redirects back to login page with an error message.
     * Generates a new CSRF token for the retry.
     */
    private void redirectWithError(HttpServletRequest request, HttpServletResponse response,
                                    String errorMessage) throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        String csrfToken = CSRFUtil.generateToken(session);
        request.setAttribute("csrfToken", csrfToken);
        request.setAttribute("error", errorMessage);
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
}
