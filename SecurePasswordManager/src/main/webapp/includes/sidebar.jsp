<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- 
    sidebar.jsp - Reusable sidebar navigation component
    
    Included by all authenticated pages via: <%@ include file="includes/sidebar.jsp" %>
    
    Provides consistent navigation across:
    - Dashboard
    - View Passwords
    - Add Password
    - Activity Logs
    - Logout
--%>
<%
    // Get current page for active state highlighting
    String currentPage = request.getRequestURI();
    String ctx = request.getContextPath();
    String sidebarUsername = (String) session.getAttribute("username");
    if (sidebarUsername == null) sidebarUsername = "User";
%>

<nav class="sidebar">
    <!-- Logo -->
    <div class="sidebar-logo">
        <div class="logo-icon">&#128274;</div>
        <h2>SecureVault</h2>
    </div>
    
    <!-- Navigation Links -->
    <ul class="sidebar-nav">
        <li>
            <a href="<%= ctx %>/dashboard.jsp" 
               class="<%= currentPage.contains("dashboard") ? "active" : "" %>">
                &#127968; Dashboard
            </a>
        </li>
        <li>
            <a href="<%= ctx %>/VaultServlet?action=list" 
               class="<%= currentPage.contains("viewPasswords") ? "active" : "" %>">
                &#128273; My Passwords
            </a>
        </li>
        <li>
            <a href="<%= ctx %>/VaultServlet?action=add" 
               class="<%= currentPage.contains("addPassword") || currentPage.contains("editPassword") ? "active" : "" %>">
                &#10133; Add Password
            </a>
        </li>
        <li>
            <a href="<%= ctx %>/LogsServlet" 
               class="<%= currentPage.contains("logs") ? "active" : "" %>">
                &#128203; Activity Logs
            </a>
        </li>
    </ul>
    
    <!-- User Section -->
    <div class="sidebar-user">
        <div class="user-info">
            <div class="user-avatar"><%= sidebarUsername.substring(0, 1).toUpperCase() %></div>
            <span><%= sidebarUsername.replace("<", "&lt;") %></span>
        </div>
        <a href="<%= ctx %>/LogoutServlet" class="btn-logout">
            &#128682; Sign Out
        </a>
    </div>
</nav>
