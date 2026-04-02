<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, com.model.Log" %>
<%-- 
    logs.jsp - Activity / Audit Logs Page
    
    Displays the user's recent activity history in a table.
    Each log entry shows the action, IP address, and timestamp.
    Actions are color-coded for easy identification.
--%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Activity Logs - Secure Password Manager</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="dashboard-layout">
        <%@ include file="includes/sidebar.jsp" %>
        
        <div class="main-content">
            <div class="page-header">
                <h1>Activity Logs</h1>
                <span class="text-sm text-muted">Last 50 actions</span>
            </div>
            
            <%
                List<Log> logs = (List<Log>) request.getAttribute("logs");
            %>
            
            <% if (logs != null && !logs.isEmpty()) { %>
                <div class="card" style="padding:0;">
                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Action</th>
                                    <th>IP Address</th>
                                    <th>Timestamp</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% int index = 1; %>
                                <% for (Log log : logs) { 
                                    String action = log.getAction();
                                    String actionClass = "registration";
                                    String icon = "&#128204;";
                                    
                                    if (action.contains("LOGIN_SUCCESS")) {
                                        actionClass = "login-success"; icon = "&#9989;";
                                    } else if (action.contains("LOGIN_FAILED")) {
                                        actionClass = "login-failed"; icon = "&#10060;";
                                    } else if (action.contains("LOGIN_LOCKED")) {
                                        actionClass = "login-locked"; icon = "&#128274;";
                                    } else if (action.contains("LOGOUT")) {
                                        actionClass = "logout"; icon = "&#128682;";
                                    } else if (action.contains("PASSWORD_ADDED")) {
                                        actionClass = "password-added"; icon = "&#10133;";
                                    } else if (action.contains("PASSWORD_UPDATED")) {
                                        actionClass = "password-updated"; icon = "&#9998;";
                                    } else if (action.contains("PASSWORD_DELETED")) {
                                        actionClass = "password-deleted"; icon = "&#128465;";
                                    } else if (action.contains("REGISTRATION")) {
                                        actionClass = "registration"; icon = "&#127881;";
                                    }
                                %>
                                    <tr>
                                        <td class="text-muted"><%= index++ %></td>
                                        <td>
                                            <span class="log-action <%= actionClass %>">
                                                <span class="log-icon"><%= icon %></span>
                                                <%= action.replace("<", "&lt;").replace(">", "&gt;") %>
                                            </span>
                                        </td>
                                        <td>
                                            <code style="font-size:13px; color:var(--text-secondary);">
                                                <%= log.getIpAddress() != null ? log.getIpAddress().replace("<", "&lt;") : "N/A" %>
                                            </code>
                                        </td>
                                        <td class="text-sm text-muted">
                                            <%= log.getTimestamp() != null ? log.getTimestamp().toString().substring(0, 19) : "N/A" %>
                                        </td>
                                    </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                </div>
                
                <p class="text-sm text-muted text-center">
                    Showing <%= logs.size() %> log entries. All actions are permanently recorded.
                </p>
                
            <% } else { %>
                <div class="card">
                    <div class="empty-state">
                        <div class="empty-icon">&#128203;</div>
                        <h3>No Activity Logged Yet</h3>
                        <p>Your actions will appear here as you use the password manager.</p>
                    </div>
                </div>
            <% } %>
        </div>
    </div>
</body>
</html>
