package com.studentmanagement.filter;

import com.studentmanagement.model.User;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "AuthenticationFilter", urlPatterns = {"/student/*", "/admin/*"})
public class AuthenticationFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code if needed
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        
        // Check if user is logged in
        if (session == null || session.getAttribute("user") == null) {
            // User is not logged in, redirect to login page
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        
        // Role-based access control
        if (requestURI.startsWith(contextPath + "/admin/") && !"ADMIN".equals(user.getRole())) {
            // Non-admin user trying to access admin pages
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/student/dashboard");
            return;
        }
        
        if (requestURI.startsWith(contextPath + "/student/") && !"STUDENT".equals(user.getRole())) {
            // Non-student user trying to access student pages
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/admin/dashboard");
            return;
        }
        
        // Check session timeout and extend it
        if (session.getMaxInactiveInterval() < 30 * 60) {
            session.setMaxInactiveInterval(30 * 60); // Extend to 30 minutes
        }
        
        // Log access for security monitoring
        logAccess(user, httpRequest);
        
        // User is authenticated and authorized, continue with the request
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
        // Cleanup code if needed
    }
    
    /**
     * Log user access for security monitoring
     * @param user User object
     * @param request HTTP request
     */
    private void logAccess(User user, HttpServletRequest request) {
        try {
            // Only log important access patterns or suspicious activities
            String uri = request.getRequestURI();
            String method = request.getMethod();
            
            // Log sensitive operations
            if (uri.contains("delete") || uri.contains("update") || uri.contains("approve") || uri.contains("reject")) {
                String sql = "INSERT INTO activity_log (user_id, action, description, ip_address, user_agent) VALUES (?, ?, ?, ?, ?)";
                
                try (java.sql.Connection connection = com.studentmanagement.util.DBConnection.getConnection();
                     java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
                    
                    statement.setInt(1, user.getUserId());
                    statement.setString(2, "ACCESS_" + method);
                    statement.setString(3, "Accessed: " + uri);
                    statement.setString(4, request.getRemoteAddr());
                    statement.setString(5, request.getHeader("User-Agent"));
                    
                    statement.executeUpdate();
                }
            }
        } catch (Exception e) {
            System.err.println("Error logging access: " + e.getMessage());
            // Don't throw exception to avoid disrupting user experience
        }
    }
}