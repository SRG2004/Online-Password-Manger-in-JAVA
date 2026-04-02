<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.dao.UserDAO, com.dao.LogDAO" %>
<%-- 
    dashboard.jsp - Main Dashboard Page
    
    Displays:
    - Welcome message with username
    - Total passwords stored (stat card)
    - Last login time (stat card)
    - Account creation date (stat card)
    - Quick action links
    - Session timeout warning
    
    Protected: Requires authenticated session (enforced by SessionFilter)
--%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - Secure Password Manager</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <%
        // Session data (guaranteed by SessionFilter)
        int userId = (int) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");
        String email = (String) session.getAttribute("email");
        
        // Fetch dashboard stats
        UserDAO userDAO = new UserDAO();
        LogDAO logDAO = new LogDAO();
        int passwordCount = userDAO.getPasswordCount(userId);
        String lastLogin = logDAO.getLastLoginTime(userId);
    %>
    
    <div class="dashboard-layout">
        <!-- ==================== Sidebar ==================== -->
        <%@ include file="includes/sidebar.jsp" %>
        
        <!-- ==================== Main Content ==================== -->
        <div class="main-content">
            <div class="page-header">
                <h1>Dashboard</h1>
                <span class="text-sm text-muted">Welcome back, <strong><%= username.replace("<", "&lt;") %></strong></span>
            </div>
            
            <!-- Stats Cards -->
            <div class="stats-grid">
                <div class="stat-card accent">
                    <div class="stat-label">Stored Passwords</div>
                    <div class="stat-value"><%= passwordCount %></div>
                </div>
                <div class="stat-card success">
                    <div class="stat-label">Last Login</div>
                    <div class="stat-value" style="font-size:16px;"><%= lastLogin.replace("<", "&lt;") %></div>
                </div>
                <div class="stat-card warning">
                    <div class="stat-label">Session Timeout</div>
                    <div class="stat-value" id="sessionTimer">5:00</div>
                </div>
            </div>
            
            <!-- Quick Actions -->
            <div class="card">
                <div class="card-header">
                    <h3>Quick Actions</h3>
                </div>
                <div class="btn-group" style="flex-wrap:wrap; gap:12px;">
                    <a href="${pageContext.request.contextPath}/VaultServlet?action=add" class="btn btn-primary">
                        &#43; Add New Password
                    </a>
                    <a href="${pageContext.request.contextPath}/VaultServlet?action=list" class="btn btn-secondary">
                        &#128270; View All Passwords
                    </a>
                    <a href="${pageContext.request.contextPath}/LogsServlet" class="btn btn-secondary">
                        &#128203; Activity Logs
                    </a>
                </div>
            </div>
            
            <!-- Security Info -->
            <div class="card">
                <div class="card-header">
                    <h3>Security Information</h3>
                </div>
                <div class="alert alert-info" style="margin-bottom:8px;">
                    &#128274; Your passwords are encrypted with AES using a key derived from your login password. 
                    Only you can decrypt them.
                </div>
                <div class="alert alert-warning" style="margin-bottom:0;">
                    &#9200; Your session will expire after 5 minutes of inactivity. 
                    You will need to log in again.
                </div>
            </div>
        </div>
    </div>
    
    <script>
        // Session countdown timer (5 minutes = 300 seconds)
        var timeLeft = 300;
        var timerDisplay = document.getElementById('sessionTimer');
        
        var countdown = setInterval(function() {
            timeLeft--;
            var minutes = Math.floor(timeLeft / 60);
            var seconds = timeLeft % 60;
            timerDisplay.textContent = minutes + ':' + (seconds < 10 ? '0' : '') + seconds;
            
            if (timeLeft <= 60) {
                timerDisplay.style.color = 'var(--danger)';
            } else if (timeLeft <= 120) {
                timerDisplay.style.color = 'var(--warning)';
            }
            
            if (timeLeft <= 0) {
                clearInterval(countdown);
                alert('Session expired! You will be redirected to login.');
                window.location.href = '${pageContext.request.contextPath}/login.jsp?error=session_expired';
            }
        }, 1000);
    </script>
</body>
</html>
