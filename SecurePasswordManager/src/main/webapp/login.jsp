<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- 
    login.jsp - User Login Page
    
    Features:
    - Username and password form fields
    - CSRF token hidden field
    - Error/success message display
    - Link to registration page
    - XSS-safe output encoding
--%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Secure Password Manager - Login to access your encrypted password vault">
    <title>Login - Secure Password Manager</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="auth-container">
        <div class="auth-card">
            <!-- Logo Section -->
            <div class="logo-section">
                <div class="shield-icon">&#128274;</div>
                <h1>Welcome Back</h1>
                <p class="subtitle">Sign in to your password vault</p>
            </div>
            
            <%-- Display error messages (XSS-escaped) --%>
            <% String error = (String) request.getAttribute("error"); %>
            <% if (error != null && !error.isEmpty()) { %>
                <div class="alert alert-error">
                    &#9888; <%= error.replace("<", "&lt;").replace(">", "&gt;") %>
                </div>
            <% } %>
            
            <%-- Display success messages (from registration or logout) --%>
            <% String success = request.getParameter("success"); %>
            <% if (success != null && !success.isEmpty()) { %>
                <div class="alert alert-success">
                    &#10004; <%= success.replace("<", "&lt;").replace(">", "&gt;") %>
                </div>
            <% } %>
            
            <%-- Display session expired message --%>
            <% String sessionError = request.getParameter("error"); %>
            <% if ("session_expired".equals(sessionError)) { %>
                <div class="alert alert-warning">
                    &#9888; Your session has expired. Please login again.
                </div>
            <% } %>
            
            <!-- Login Form -->
            <form action="${pageContext.request.contextPath}/LoginServlet" method="POST" id="loginForm">
                <%-- CSRF Token - hidden field to prevent cross-site request forgery --%>
                <% 
                    String csrfToken = (String) request.getAttribute("csrfToken");
                    if (csrfToken == null) {
                        csrfToken = com.util.CSRFUtil.getToken(session);
                    }
                %>
                <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                
                <div class="form-group">
                    <label for="username">Username</label>
                    <input type="text" id="username" name="username" class="form-control" 
                           placeholder="Enter your username" required autocomplete="username">
                </div>
                
                <div class="form-group">
                    <label for="password">Password</label>
                    <div class="password-field">
                        <input type="password" id="password" name="password" class="form-control" 
                               placeholder="Enter your password" required autocomplete="current-password">
                        <button type="button" class="btn-icon" onclick="togglePassword('password', this)" 
                                title="Toggle password visibility">&#128065;</button>
                    </div>
                </div>
                
                <button type="submit" class="btn btn-primary btn-block" style="margin-top: 8px;">
                    Sign In
                </button>
            </form>
            
            <div class="auth-footer">
                Don't have an account? 
                <a href="${pageContext.request.contextPath}/RegisterServlet">Create one</a>
            </div>
        </div>
    </div>
    
    <script>
        // Toggle password visibility
        function togglePassword(fieldId, btn) {
            var field = document.getElementById(fieldId);
            if (field.type === 'password') {
                field.type = 'text';
                btn.innerHTML = '&#128064;'; // Open eye
            } else {
                field.type = 'password';
                btn.innerHTML = '&#128065;'; // Closed eye
            }
        }
    </script>
</body>
</html>
