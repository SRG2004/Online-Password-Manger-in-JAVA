<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- 
    register.jsp - User Registration Page
    
    Features:
    - Username, email, password, confirm password fields
    - CSRF token protection
    - Client-side password strength checker
    - Server-side validation error display
    - XSS-safe output
--%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Create your Secure Password Manager account">
    <title>Register - Secure Password Manager</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="auth-container">
        <div class="auth-card">
            <!-- Logo Section -->
            <div class="logo-section">
                <div class="shield-icon">&#128274;</div>
                <h1>Create Account</h1>
                <p class="subtitle">Start securing your passwords today</p>
            </div>
            
            <%-- Display error messages --%>
            <% String error = (String) request.getAttribute("error"); %>
            <% if (error != null && !error.isEmpty()) { %>
                <div class="alert alert-error">
                    &#9888; <%= error.replace("<", "&lt;").replace(">", "&gt;") %>
                </div>
            <% } %>
            
            <!-- Registration Form -->
            <form action="${pageContext.request.contextPath}/RegisterServlet" method="POST" id="registerForm">
                <%-- CSRF Token --%>
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
                           placeholder="Letters, numbers, underscore (3-50 chars)" required
                           pattern="^[a-zA-Z0-9_]{3,50}$" autocomplete="username">
                </div>
                
                <div class="form-group">
                    <label for="email">Email Address</label>
                    <input type="email" id="email" name="email" class="form-control" 
                           placeholder="you@example.com" required autocomplete="email">
                </div>
                
                <div class="form-group">
                    <label for="password">Password</label>
                    <div class="password-field">
                        <input type="password" id="password" name="password" class="form-control" 
                               placeholder="Min 8 characters, mixed case & symbols" required 
                               minlength="8" onkeyup="checkPasswordStrength()"
                               autocomplete="new-password">
                        <button type="button" class="btn-icon" onclick="togglePassword('password', this)">&#128065;</button>
                    </div>
                    <!-- Password strength indicator -->
                    <div class="strength-bar-container" id="strengthBarContainer" style="display:none;">
                        <div class="strength-bar" id="strengthBar"></div>
                    </div>
                    <span class="strength-indicator" id="strengthLabel" style="display:none;"></span>
                </div>
                
                <div class="form-group">
                    <label for="confirmPassword">Confirm Password</label>
                    <div class="password-field">
                        <input type="password" id="confirmPassword" name="confirmPassword" class="form-control" 
                               placeholder="Re-enter your password" required autocomplete="new-password">
                        <button type="button" class="btn-icon" onclick="togglePassword('confirmPassword', this)">&#128065;</button>
                    </div>
                    <span class="text-sm text-muted" id="matchMessage" style="margin-top:4px;display:none;"></span>
                </div>
                
                <button type="submit" class="btn btn-primary btn-block" style="margin-top: 8px;">
                    Create Account
                </button>
            </form>
            
            <div class="auth-footer">
                Already have an account? 
                <a href="${pageContext.request.contextPath}/LoginServlet">Sign in</a>
            </div>
        </div>
    </div>
    
    <script>
        // Toggle password visibility
        function togglePassword(fieldId, btn) {
            var field = document.getElementById(fieldId);
            if (field.type === 'password') {
                field.type = 'text';
                btn.innerHTML = '&#128064;';
            } else {
                field.type = 'password';
                btn.innerHTML = '&#128065;';
            }
        }
        
        // Client-side password strength checker
        function checkPasswordStrength() {
            var password = document.getElementById('password').value;
            var bar = document.getElementById('strengthBar');
            var label = document.getElementById('strengthLabel');
            var container = document.getElementById('strengthBarContainer');
            
            if (password.length === 0) {
                container.style.display = 'none';
                label.style.display = 'none';
                return;
            }
            
            container.style.display = 'block';
            label.style.display = 'inline-block';
            label.style.marginTop = '6px';
            
            var score = 0;
            if (password.length >= 8) score++;
            if (/[A-Z]/.test(password)) score++;
            if (/[a-z]/.test(password)) score++;
            if (/[0-9]/.test(password)) score++;
            if (/[^A-Za-z0-9]/.test(password)) score++;
            
            if (score <= 2) {
                bar.className = 'strength-bar weak';
                label.className = 'strength-indicator strength-weak';
                label.textContent = 'Weak';
            } else if (score <= 4) {
                bar.className = 'strength-bar medium';
                label.className = 'strength-indicator strength-medium';
                label.textContent = 'Medium';
            } else {
                bar.className = 'strength-bar strong';
                label.className = 'strength-indicator strength-strong';
                label.textContent = 'Strong';
            }
            
            checkPasswordMatch();
        }
        
        // Check if passwords match
        function checkPasswordMatch() {
            var password = document.getElementById('password').value;
            var confirm = document.getElementById('confirmPassword').value;
            var msg = document.getElementById('matchMessage');
            
            if (confirm.length === 0) {
                msg.style.display = 'none';
                return;
            }
            
            msg.style.display = 'block';
            if (password === confirm) {
                msg.textContent = '✓ Passwords match';
                msg.style.color = 'var(--success)';
            } else {
                msg.textContent = '✗ Passwords do not match';
                msg.style.color = 'var(--danger)';
            }
        }
        
        document.getElementById('confirmPassword').addEventListener('keyup', checkPasswordMatch);
    </script>
</body>
</html>
