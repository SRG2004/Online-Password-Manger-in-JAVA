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
 * RegisterServlet.java - Handles new user registration.
 * 
 * Flow:
 * GET  /RegisterServlet → Forward to register.jsp (with CSRF token)
 * POST /RegisterServlet → Validate input, hash password, create user
 * 
 * Security features:
 * 1. Server-side input validation (length, format)
 * 2. CSRF token validation
 * 3. SHA-256 password hashing before storage
 * 4. Duplicate username/email detection
 * 5. Password strength enforcement (minimum: Medium)
 */
@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO = new UserDAO();
    private LogDAO logDAO = new LogDAO();
    
    /**
     * Displays the registration page with a CSRF token.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(true);
        String csrfToken = CSRFUtil.generateToken(session);
        request.setAttribute("csrfToken", csrfToken);
        
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }
    
    /**
     * Processes registration form submission.
     * 
     * Validation steps:
     * 1. All fields are required and non-empty
     * 2. Username: 3–50 characters, alphanumeric + underscore only
     * 3. Email: basic format validation
     * 4. Password: minimum 8 characters, at least "Medium" strength
     * 5. Passwords must match (password + confirm)
     * 6. Username and email must be unique
     * 7. CSRF token must be valid
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // ==================== Input Extraction ====================
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String csrfToken = request.getParameter("csrfToken");
        String clientIP = request.getRemoteAddr();
        
        // ==================== CSRF Validation ====================
        HttpSession session = request.getSession(true);
        if (!CSRFUtil.validateToken(session, csrfToken)) {
            redirectWithError(request, response, "Invalid request. Please try again.");
            return;
        }
        
        // ==================== Required Fields Check ====================
        if (username == null || username.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            confirmPassword == null || confirmPassword.trim().isEmpty()) {
            redirectWithError(request, response, "All fields are required.");
            return;
        }
        
        username = username.trim();
        email = email.trim();
        password = password.trim();
        confirmPassword = confirmPassword.trim();
        
        // ==================== Username Validation ====================
        if (username.length() < 3 || username.length() > 50) {
            redirectWithError(request, response, "Username must be 3–50 characters.");
            return;
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            redirectWithError(request, response, "Username can only contain letters, numbers, and underscores.");
            return;
        }
        
        // ==================== Email Validation ====================
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            redirectWithError(request, response, "Invalid email format.");
            return;
        }
        
        // ==================== Password Validation ====================
        if (password.length() < 8) {
            redirectWithError(request, response, "Password must be at least 8 characters.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            redirectWithError(request, response, "Passwords do not match.");
            return;
        }
        
        // Check password strength
        String strength = PasswordUtil.checkStrength(password);
        if ("Weak".equals(strength)) {
            redirectWithError(request, response, 
                "Password is too weak. Include uppercase, lowercase, numbers, and symbols.");
            return;
        }
        
        // ==================== Duplicate Check ====================
        if (userDAO.usernameExists(username)) {
            redirectWithError(request, response, "Username already taken.");
            return;
        }
        if (userDAO.emailExists(email)) {
            redirectWithError(request, response, "Email already registered.");
            return;
        }
        
        // ==================== Create User ====================
        String passwordHash = PasswordUtil.hashPassword(password);
        User newUser = new User(username, email, passwordHash);
        
        boolean success = userDAO.registerUser(newUser);
        
        if (success) {
            // Log the registration event
            User registeredUser = userDAO.getUserByUsername(username);
            if (registeredUser != null) {
                logDAO.addLog(registeredUser.getId(), "REGISTRATION", clientIP);
            }
            
            // Redirect to login with success message
            response.sendRedirect(request.getContextPath() + 
                "/login.jsp?success=Registration+successful!+Please+login.");
        } else {
            redirectWithError(request, response, "Registration failed. Please try again.");
        }
    }
    
    /**
     * Helper: Forward back to registration page with error message.
     */
    private void redirectWithError(HttpServletRequest request, HttpServletResponse response,
                                    String errorMessage) throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        String csrfToken = CSRFUtil.generateToken(session);
        request.setAttribute("csrfToken", csrfToken);
        request.setAttribute("error", errorMessage);
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }
}
