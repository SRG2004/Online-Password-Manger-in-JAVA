<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true"%>
<%-- 
    error.jsp - Generic Error Page
    
    Handles 404 and 500 errors gracefully.
    Shows a user-friendly message with a link back to safety.
--%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error - Secure Password Manager</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="error-container">
        <div class="error-code">
            <%= response.getStatus() %>
        </div>
        <h1 style="margin-bottom: 8px;">Something went wrong</h1>
        <p class="text-muted" style="margin-bottom: 24px;">
            <% if (response.getStatus() == 404) { %>
                The page you're looking for doesn't exist.
            <% } else { %>
                An unexpected error occurred. Please try again later.
            <% } %>
        </p>
        <div class="btn-group">
            <a href="${pageContext.request.contextPath}/dashboard.jsp" class="btn btn-primary">
                Go to Dashboard
            </a>
            <a href="${pageContext.request.contextPath}/login.jsp" class="btn btn-secondary">
                Back to Login
            </a>
        </div>
    </div>
</body>
</html>
