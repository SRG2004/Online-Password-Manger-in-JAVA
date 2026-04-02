<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- 
    addPassword.jsp - Add New Credential to Vault
    
    Features:
    - Website, username, and password input fields
    - Built-in password generator with length selector
    - Real-time password strength checker
    - CSRF token protection
    - XSS-safe output
--%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Add Password - Secure Password Manager</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="dashboard-layout">
        <%@ include file="includes/sidebar.jsp" %>
        
        <div class="main-content">
            <div class="page-header">
                <h1>Add New Password</h1>
                <a href="${pageContext.request.contextPath}/VaultServlet?action=list" class="btn btn-secondary">
                    &larr; Back to Vault
                </a>
            </div>
            
            <%-- Error Message --%>
            <% String error = (String) request.getAttribute("error"); %>
            <% if (error != null) { %>
                <div class="alert alert-error">
                    &#9888; <%= error.replace("<", "&lt;").replace(">", "&gt;") %>
                </div>
            <% } %>
            
            <div class="card">
                <form action="${pageContext.request.contextPath}/VaultServlet" method="POST" id="addForm">
                    <input type="hidden" name="action" value="add">
                    
                    <%-- CSRF Token --%>
                    <% 
                        String csrfToken = (String) request.getAttribute("csrfToken");
                        if (csrfToken == null) csrfToken = com.util.CSRFUtil.getToken(session);
                    %>
                    <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                    
                    <div class="form-group">
                        <label for="website">Website / Service</label>
                        <input type="text" id="website" name="website" class="form-control" 
                               placeholder="e.g., github.com, gmail.com" required>
                    </div>
                    
                    <div class="form-group">
                        <label for="siteUsername">Username / Email</label>
                        <input type="text" id="siteUsername" name="siteUsername" class="form-control" 
                               placeholder="Your username or email for this site" required>
                    </div>
                    
                    <div class="form-group">
                        <label for="sitePassword">Password</label>
                        <div class="password-field">
                            <input type="password" id="sitePassword" name="sitePassword" class="form-control" 
                                   placeholder="Enter or generate a password" required 
                                   onkeyup="checkStrength()">
                            <button type="button" class="btn-icon" onclick="togglePassword('sitePassword', this)">&#128065;</button>
                        </div>
                        <div class="strength-bar-container" id="strengthBarContainer" style="display:none;">
                            <div class="strength-bar" id="strengthBar"></div>
                        </div>
                        <span class="strength-indicator" id="strengthLabel" style="display:none;margin-top:6px;"></span>
                    </div>
                    
                    <!-- Password Generator -->
                    <div class="generator-section">
                        <h4>&#9889; Password Generator</h4>
                        <div class="generator-controls">
                            <label for="genLength">Length:</label>
                            <select id="genLength" class="form-control" style="width:auto;">
                                <option value="8">8</option>
                                <option value="10">10</option>
                                <option value="12" selected>12</option>
                                <option value="14">14</option>
                                <option value="16">16</option>
                            </select>
                            <button type="button" class="btn btn-success btn-sm" onclick="generatePassword()">
                                Generate Strong Password
                            </button>
                        </div>
                    </div>
                    
                    <button type="submit" class="btn btn-primary btn-block" style="margin-top:20px;">
                        &#128274; Save to Vault
                    </button>
                </form>
            </div>
        </div>
    </div>
    
    <script>
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
        
        // Generate password via AJAX call to VaultServlet
        function generatePassword() {
            var length = document.getElementById('genLength').value;
            var xhr = new XMLHttpRequest();
            xhr.open('GET', '${pageContext.request.contextPath}/VaultServlet?action=generate&length=' + length, true);
            xhr.onreadystatechange = function() {
                if (xhr.readyState === 4 && xhr.status === 200) {
                    var passwordField = document.getElementById('sitePassword');
                    passwordField.value = xhr.responseText;
                    passwordField.type = 'text'; // Show generated password
                    checkStrength();
                }
            };
            xhr.send();
        }
        
        // Check password strength via AJAX
        function checkStrength() {
            var password = document.getElementById('sitePassword').value;
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
        }
    </script>
</body>
</html>
