<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, com.model.Vault" %>
<%-- 
    viewPasswords.jsp - Password Vault View
    
    Displays all saved credentials in a table.
    Passwords are shown masked by default with toggle to reveal.
    Provides edit, delete, and copy actions for each entry.
    
    Security:
    - Passwords arrive pre-decrypted from the servlet (for authenticated user only)
    - XSS-escaped output in all table cells
    - Delete confirmations prevent accidental deletion
--%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Passwords - Secure Password Manager</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="dashboard-layout">
        <%@ include file="includes/sidebar.jsp" %>
        
        <div class="main-content">
            <div class="page-header">
                <h1>My Passwords</h1>
                <a href="${pageContext.request.contextPath}/VaultServlet?action=add" class="btn btn-primary">
                    &#43; Add New
                </a>
            </div>
            
            <%-- Success Message --%>
            <% String success = request.getParameter("success"); %>
            <% if (success != null && !success.isEmpty()) { %>
                <div class="alert alert-success">
                    &#10004; <%= success.replace("<", "&lt;").replace(">", "&gt;") %>
                </div>
            <% } %>
            
            <%-- Error Message --%>
            <% 
                String error = (String) request.getAttribute("error");
                if (error == null) error = request.getParameter("error");
            %>
            <% if (error != null && !error.isEmpty()) { %>
                <div class="alert alert-error">
                    &#9888; <%= error.replace("<", "&lt;").replace(">", "&gt;") %>
                </div>
            <% } %>
            
            <%
                List<Vault> credentials = (List<Vault>) request.getAttribute("credentials");
            %>
            
            <% if (credentials != null && !credentials.isEmpty()) { %>
                <div class="card" style="padding:0;">
                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Website</th>
                                    <th>Username</th>
                                    <th>Password</th>
                                    <th>Added On</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% int index = 1; %>
                                <% for (Vault v : credentials) { %>
                                    <tr>
                                        <td><%= index++ %></td>
                                        <td><strong><%= v.getWebsite().replace("<", "&lt;").replace(">", "&gt;") %></strong></td>
                                        <td><%= v.getSiteUsername().replace("<", "&lt;").replace(">", "&gt;") %></td>
                                        <td>
                                            <div class="password-display">
                                                <span class="password-mask" id="pwd-mask-<%= v.getId() %>">••••••••</span>
                                                <span style="display:none;" id="pwd-text-<%= v.getId() %>"><%= v.getDecryptedPassword() != null ? v.getDecryptedPassword().replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;") : "" %></span>
                                                <button class="btn-copy" onclick="toggleVaultPwd(<%= v.getId() %>)" title="Show/Hide">&#128065;</button>
                                                <button class="btn-copy" onclick="copyToClipboard(<%= v.getId() %>)" title="Copy">&#128203;</button>
                                            </div>
                                        </td>
                                        <td class="text-sm text-muted">
                                            <%= v.getCreatedAt() != null ? v.getCreatedAt().toString().substring(0, 16) : "N/A" %>
                                        </td>
                                        <td>
                                            <div class="btn-group">
                                                <a href="${pageContext.request.contextPath}/VaultServlet?action=edit&id=<%= v.getId() %>" 
                                                   class="btn btn-secondary btn-sm">Edit</a>
                                                <a href="${pageContext.request.contextPath}/VaultServlet?action=delete&id=<%= v.getId() %>" 
                                                   class="btn btn-danger btn-sm"
                                                   onclick="return confirm('Delete this credential for <%= v.getWebsite().replace("'", "\\'").replace("<", "&lt;") %>? This cannot be undone.');">Delete</a>
                                            </div>
                                        </td>
                                    </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                </div>
                
                <p class="text-sm text-muted text-center">
                    Showing <%= credentials.size() %> credential(s). Passwords are AES-encrypted at rest.
                </p>
                
            <% } else { %>
                <!-- Empty State -->
                <div class="card">
                    <div class="empty-state">
                        <div class="empty-icon">&#128274;</div>
                        <h3>No Passwords Stored Yet</h3>
                        <p>Start by adding your first credential to the vault.</p>
                        <a href="${pageContext.request.contextPath}/VaultServlet?action=add" class="btn btn-primary">
                            &#43; Add Your First Password
                        </a>
                    </div>
                </div>
            <% } %>
        </div>
    </div>
    
    <script>
        // Toggle individual password visibility in the table
        function toggleVaultPwd(id) {
            var mask = document.getElementById('pwd-mask-' + id);
            var text = document.getElementById('pwd-text-' + id);
            
            if (mask.style.display !== 'none') {
                mask.style.display = 'none';
                text.style.display = 'inline';
            } else {
                mask.style.display = 'inline';
                text.style.display = 'none';
            }
        }
        
        // Copy password to clipboard
        function copyToClipboard(id) {
            var text = document.getElementById('pwd-text-' + id).textContent;
            
            if (navigator.clipboard) {
                navigator.clipboard.writeText(text).then(function() {
                    showCopyNotification();
                });
            } else {
                // Fallback for older browsers
                var textarea = document.createElement('textarea');
                textarea.value = text;
                document.body.appendChild(textarea);
                textarea.select();
                document.execCommand('copy');
                document.body.removeChild(textarea);
                showCopyNotification();
            }
        }
        
        function showCopyNotification() {
            var notification = document.createElement('div');
            notification.className = 'alert alert-success';
            notification.style.position = 'fixed';
            notification.style.top = '20px';
            notification.style.right = '20px';
            notification.style.zIndex = '9999';
            notification.style.animation = 'fadeIn 0.3s ease-out';
            notification.innerHTML = '&#10004; Password copied to clipboard!';
            document.body.appendChild(notification);
            
            setTimeout(function() {
                notification.remove();
            }, 2000);
        }
    </script>
</body>
</html>
