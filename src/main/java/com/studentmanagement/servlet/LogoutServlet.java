package com.studentmanagement.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            // Log user activity before logout
            try {
                Integer userId = (Integer) session.getAttribute("userId");
                if (userId != null) {
                    logUserActivity(userId, "LOGOUT", "User logged out from IP: " + request.getRemoteAddr(), request);
                }
            } catch (Exception e) {
                System.err.println("Error logging logout activity: " + e.getMessage());
            }
            
            // Invalidate session
            session.invalidate();
        }
        
        // Clear cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("username".equals(cookie.getName()) || "rememberMe".equals(cookie.getName())) {
                    cookie.setValue("");
                    cookie.setMaxAge(0);
                    cookie.setPath(request.getContextPath());
                    response.addCookie(cookie);
                }
            }
        }
        
        // Redirect to login page with success message
        request.getSession().setAttribute("success", "You have been successfully logged out.");
        response.sendRedirect(request.getContextPath() + "/login");
    }
    
    /**
     * Log user activity
     * @param userId User ID
     * @param action Action performed
     * @param description Description of the action
     * @param request HTTP request
     */
    private void logUserActivity(int userId, String action, String description, HttpServletRequest request) {
        try {
            String sql = "INSERT INTO activity_log (user_id, action, description, ip_address, user_agent) VALUES (?, ?, ?, ?, ?)";
            
            try (java.sql.Connection connection = com.studentmanagement.util.DBConnection.getConnection();
                 java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
                
                statement.setInt(1, userId);
                statement.setString(2, action);
                statement.setString(3, description);
                statement.setString(4, request.getRemoteAddr());
                statement.setString(5, request.getHeader("User-Agent"));
                
                statement.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("Error logging user activity: " + e.getMessage());
        }
    }
}