package com.controller;

import com.dao.VaultDAO;
import com.dao.LogDAO;
import com.model.Vault;
import com.util.EncryptionUtil;
import com.util.PasswordUtil;
import com.util.CSRFUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * VaultServlet.java - Handles all password vault operations.
 * 
 * URL Patterns and Actions:
 * GET  /VaultServlet?action=list     → List all credentials (decrypt for display)
 * GET  /VaultServlet?action=add      → Show add credential form
 * GET  /VaultServlet?action=edit&id= → Show edit form for specific credential
 * GET  /VaultServlet?action=delete&id= → Delete a credential
 * GET  /VaultServlet?action=generate&length= → Generate random password (AJAX)
 * GET  /VaultServlet?action=checkStrength&password= → Check password strength (AJAX)
 * POST /VaultServlet?action=add      → Process add credential form
 * POST /VaultServlet?action=update   → Process edit credential form
 * 
 * Security:
 * - All operations require authenticated session (enforced by SessionFilter)
 * - Vault passwords are AES-encrypted with per-user derived key
 * - Ownership verified on every edit/delete (user_id check in DAO)
 * - CSRF tokens validated on all POST requests
 */
@WebServlet("/VaultServlet")
public class VaultServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private VaultDAO vaultDAO = new VaultDAO();
    private LogDAO logDAO = new LogDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        
        int userId = (int) session.getAttribute("userId");
        String userPassword = (String) session.getAttribute("userPassword");
        String action = request.getParameter("action");
        
        if (action == null) action = "list";
        
        switch (action) {
            
            // ==================== LIST ALL CREDENTIALS ====================
            case "list":
                handleList(request, response, userId, userPassword);
                break;
                
            // ==================== SHOW ADD FORM ====================
            case "add":
                String csrfToken = CSRFUtil.generateToken(session);
                request.setAttribute("csrfToken", csrfToken);
                request.getRequestDispatcher("/addPassword.jsp").forward(request, response);
                break;
                
            // ==================== SHOW EDIT FORM ====================
            case "edit":
                handleEditForm(request, response, userId, userPassword, session);
                break;
                
            // ==================== DELETE CREDENTIAL ====================
            case "delete":
                handleDelete(request, response, userId, session);
                break;
                
            // ==================== GENERATE PASSWORD (AJAX) ====================
            case "generate":
                handleGenerate(request, response);
                break;
                
            // ==================== CHECK STRENGTH (AJAX) ====================
            case "checkStrength":
                handleCheckStrength(request, response);
                break;
                
            default:
                handleList(request, response, userId, userPassword);
                break;
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        
        int userId = (int) session.getAttribute("userId");
        String userPassword = (String) session.getAttribute("userPassword");
        String action = request.getParameter("action");
        String clientIP = request.getRemoteAddr();
        
        // CSRF validation for all POST requests
        String csrfToken = request.getParameter("csrfToken");
        if (!CSRFUtil.validateToken(session, csrfToken)) {
            request.setAttribute("error", "Invalid request. Please try again.");
            handleList(request, response, userId, userPassword);
            return;
        }
        
        if ("add".equals(action)) {
            handleAddPost(request, response, userId, userPassword, clientIP, session);
        } else if ("update".equals(action)) {
            handleUpdatePost(request, response, userId, userPassword, clientIP, session);
        } else {
            handleList(request, response, userId, userPassword);
        }
    }
    
    // ======================== Handler Methods ========================
    
    /**
     * Lists all credentials for the authenticated user.
     * Decrypts each password using the user's derived AES key.
     */
    private void handleList(HttpServletRequest request, HttpServletResponse response,
                            int userId, String userPassword) throws ServletException, IOException {
        
        List<Vault> credentials = vaultDAO.getCredentialsByUserId(userId);
        
        // Decrypt passwords for display to the authenticated user
        for (Vault vault : credentials) {
            try {
                String decrypted = EncryptionUtil.decrypt(vault.getEncryptedPassword(), userPassword);
                vault.setDecryptedPassword(decrypted);
            } catch (Exception e) {
                vault.setDecryptedPassword("[Decryption Error]");
                System.err.println("[VaultServlet] Decryption failed for vault ID: " + vault.getId());
            }
        }
        
        request.setAttribute("credentials", credentials);
        
        // Generate CSRF token for delete forms
        HttpSession session = request.getSession(false);
        if (session != null) {
            String csrfToken = CSRFUtil.generateToken(session);
            request.setAttribute("csrfToken", csrfToken);
        }
        
        request.getRequestDispatcher("/viewPasswords.jsp").forward(request, response);
    }
    
    /**
     * Shows the edit form pre-filled with existing credential data.
     * Decrypts the password for display in the edit form.
     */
    private void handleEditForm(HttpServletRequest request, HttpServletResponse response,
                                int userId, String userPassword, HttpSession session)
            throws ServletException, IOException {
        
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/VaultServlet?action=list");
            return;
        }
        
        try {
            int id = Integer.parseInt(idParam);
            Vault vault = vaultDAO.getCredentialById(id, userId);
            
            if (vault == null) {
                request.setAttribute("error", "Credential not found or access denied.");
                handleList(request, response, userId, userPassword);
                return;
            }
            
            // Decrypt password for display in the edit form
            try {
                String decrypted = EncryptionUtil.decrypt(vault.getEncryptedPassword(), userPassword);
                vault.setDecryptedPassword(decrypted);
            } catch (Exception e) {
                vault.setDecryptedPassword("");
            }
            
            String csrfToken = CSRFUtil.generateToken(session);
            request.setAttribute("csrfToken", csrfToken);
            request.setAttribute("vault", vault);
            request.getRequestDispatcher("/editPassword.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/VaultServlet?action=list");
        }
    }
    
    /**
     * Processes the add credential form submission.
     * Encrypts the password with AES before storing.
     */
    private void handleAddPost(HttpServletRequest request, HttpServletResponse response,
                               int userId, String userPassword, String clientIP, HttpSession session)
            throws ServletException, IOException {
        
        String website = request.getParameter("website");
        String siteUsername = request.getParameter("siteUsername");
        String sitePassword = request.getParameter("sitePassword");
        
        // Input validation
        if (website == null || website.trim().isEmpty() ||
            siteUsername == null || siteUsername.trim().isEmpty() ||
            sitePassword == null || sitePassword.trim().isEmpty()) {
            
            String csrfToken = CSRFUtil.generateToken(session);
            request.setAttribute("csrfToken", csrfToken);
            request.setAttribute("error", "All fields are required.");
            request.getRequestDispatcher("/addPassword.jsp").forward(request, response);
            return;
        }
        
        // Encrypt the site password with the user's derived AES key
        String encryptedPassword = EncryptionUtil.encrypt(sitePassword.trim(), userPassword);
        
        Vault vault = new Vault(userId, website.trim(), siteUsername.trim(), encryptedPassword);
        boolean success = vaultDAO.addCredential(vault);
        
        if (success) {
            logDAO.addLog(userId, "PASSWORD_ADDED: " + website.trim(), clientIP);
            response.sendRedirect(request.getContextPath() + 
                "/VaultServlet?action=list&success=Credential+added+successfully!");
        } else {
            String csrfToken = CSRFUtil.generateToken(session);
            request.setAttribute("csrfToken", csrfToken);
            request.setAttribute("error", "Failed to add credential.");
            request.getRequestDispatcher("/addPassword.jsp").forward(request, response);
        }
    }
    
    /**
     * Processes the edit credential form submission.
     * Re-encrypts the password with AES before updating.
     */
    private void handleUpdatePost(HttpServletRequest request, HttpServletResponse response,
                                  int userId, String userPassword, String clientIP, HttpSession session)
            throws ServletException, IOException {
        
        String idParam = request.getParameter("id");
        String website = request.getParameter("website");
        String siteUsername = request.getParameter("siteUsername");
        String sitePassword = request.getParameter("sitePassword");
        
        if (idParam == null || website == null || website.trim().isEmpty() ||
            siteUsername == null || siteUsername.trim().isEmpty() ||
            sitePassword == null || sitePassword.trim().isEmpty()) {
            
            request.setAttribute("error", "All fields are required.");
            handleList(request, response, userId, userPassword);
            return;
        }
        
        try {
            int id = Integer.parseInt(idParam);
            
            // Encrypt the updated password
            String encryptedPassword = EncryptionUtil.encrypt(sitePassword.trim(), userPassword);
            
            Vault vault = new Vault(userId, website.trim(), siteUsername.trim(), encryptedPassword);
            vault.setId(id);
            vault.setUserId(userId);
            
            boolean success = vaultDAO.updateCredential(vault);
            
            if (success) {
                logDAO.addLog(userId, "PASSWORD_UPDATED: " + website.trim(), clientIP);
                response.sendRedirect(request.getContextPath() + 
                    "/VaultServlet?action=list&success=Credential+updated+successfully!");
            } else {
                request.setAttribute("error", "Failed to update credential.");
                handleList(request, response, userId, userPassword);
            }
            
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid credential ID.");
            handleList(request, response, userId, userPassword);
        }
    }
    
    /**
     * Deletes a credential after verifying ownership.
     */
    private void handleDelete(HttpServletRequest request, HttpServletResponse response,
                              int userId, HttpSession session) throws IOException {
        
        String idParam = request.getParameter("id");
        String clientIP = request.getRemoteAddr();
        
        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                
                // Get credential info before deletion (for logging)
                Vault vault = vaultDAO.getCredentialById(id, userId);
                String website = vault != null ? vault.getWebsite() : "unknown";
                
                boolean success = vaultDAO.deleteCredential(id, userId);
                
                if (success) {
                    logDAO.addLog(userId, "PASSWORD_DELETED: " + website, clientIP);
                    response.sendRedirect(request.getContextPath() + 
                        "/VaultServlet?action=list&success=Credential+deleted+successfully!");
                } else {
                    response.sendRedirect(request.getContextPath() + 
                        "/VaultServlet?action=list&error=Failed+to+delete+credential.");
                }
            } catch (NumberFormatException e) {
                response.sendRedirect(request.getContextPath() + "/VaultServlet?action=list");
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/VaultServlet?action=list");
        }
    }
    
    /**
     * Generates a random password and returns it as plain text (AJAX endpoint).
     * Used by the JavaScript password generator on add/edit pages.
     */
    private void handleGenerate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        int length = 12;  // Default length
        String lengthParam = request.getParameter("length");
        
        if (lengthParam != null) {
            try {
                length = Integer.parseInt(lengthParam);
                if (length < 8) length = 8;
                if (length > 32) length = 32;
            } catch (NumberFormatException e) {
                length = 12;
            }
        }
        
        String generatedPassword = PasswordUtil.generatePassword(length);
        
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(generatedPassword);
        out.flush();
    }
    
    /**
     * Checks password strength and returns the result (AJAX endpoint).
     * Returns JSON with strength level and CSS class.
     */
    private void handleCheckStrength(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        String password = request.getParameter("password");
        String strength = PasswordUtil.checkStrength(password);
        String strengthClass = PasswordUtil.getStrengthClass(strength);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print("{\"strength\":\"" + strength + "\",\"class\":\"" + strengthClass + "\"}");
        out.flush();
    }
}
