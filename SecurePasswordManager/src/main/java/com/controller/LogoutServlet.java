package com.controller;

import com.dao.LogDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * LogoutServlet.java - Handles user logout.
 * 
 * Flow:
 * GET /LogoutServlet → Log the event, invalidate session, redirect to login
 * 
 * Security:
 * - Logs the logout action with IP address before destroying the session
 * - Invalidates the entire session (removes all attributes)
 * - Redirects to login page with success message
 */
@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private LogDAO logDAO = new LogDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        String clientIP = request.getRemoteAddr();
        
        if (session != null) {
            // Log the logout event before destroying the session
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId != null) {
                logDAO.addLog(userId, "LOGOUT", clientIP);
            }
            
            // Destroy the session — removes all attributes including userId, password
            session.invalidate();
        }
        
        // Redirect to login page with logout success message
        response.sendRedirect(request.getContextPath() + "/login.jsp?success=Logged+out+successfully.");
    }
}
