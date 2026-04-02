package com.controller;

import com.dao.LogDAO;
import com.model.Log;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

/**
 * LogsServlet.java - Displays activity/audit logs.
 * 
 * Shows the authenticated user's recent activity history.
 * GET /LogsServlet → Fetch logs and forward to logs.jsp
 */
@WebServlet("/LogsServlet")
public class LogsServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
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
        
        // Fetch the user's recent activity logs
        List<Log> logs = logDAO.getLogsByUserId(userId);
        request.setAttribute("logs", logs);
        
        request.getRequestDispatcher("/logs.jsp").forward(request, response);
    }
}
