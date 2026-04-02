package com.util;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * SessionFilter.java - Authentication & Route Protection Filter
 * 
 * Implements javax.servlet.Filter to intercept ALL requests
 * and enforce authentication on protected routes.
 * 
 * Protected routes: Any URL NOT in the whitelist below.
 * Public routes: login.jsp, register.jsp, LoginServlet, RegisterServlet, CSS
 * 
 * Behavior:
 * 1. Check if request URL is for a public resource → allow through
 * 2. Check if user has an active session with "userId" attribute
 * 3. If not authenticated → redirect to login page
 * 4. If authenticated → pass request to next filter/servlet
 * 
 * Configured in web.xml to intercept all URL patterns (/*).
 */
public class SessionFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("[SessionFilter] Authentication filter initialized.");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        
        // ==================== Public Resource Whitelist ====================
        // These URLs are accessible WITHOUT authentication
        boolean isPublicResource = 
            requestURI.equals(contextPath + "/") ||
            requestURI.equals(contextPath + "/login.jsp") ||
            requestURI.equals(contextPath + "/register.jsp") ||
            requestURI.endsWith("/LoginServlet") ||
            requestURI.endsWith("/RegisterServlet") ||
            requestURI.contains("/css/") ||
            requestURI.contains("/images/") ||
            requestURI.endsWith(".css") ||
            requestURI.endsWith(".png") ||
            requestURI.endsWith(".jpg") ||
            requestURI.endsWith(".ico");
        
        if (isPublicResource) {
            // Allow public resources through without authentication
            chain.doFilter(request, response);
            return;
        }
        
        // ==================== Session Validation ====================
        HttpSession session = httpRequest.getSession(false);  // Don't create new session
        
        if (session != null && session.getAttribute("userId") != null) {
            // User is authenticated — allow request to proceed
            chain.doFilter(request, response);
        } else {
            // User is NOT authenticated — redirect to login
            System.out.println("[SessionFilter] Unauthorized access attempt: " + requestURI 
                + " from IP: " + httpRequest.getRemoteAddr());
            httpResponse.sendRedirect(contextPath + "/login.jsp?error=session_expired");
        }
    }
    
    @Override
    public void destroy() {
        System.out.println("[SessionFilter] Authentication filter destroyed.");
    }
}
