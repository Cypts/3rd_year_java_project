package com.studentmanagement.servlet;

import com.studentmanagement.dao.UserDAO;
import com.studentmanagement.model.User;
import com.studentmanagement.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    
    private UserDAO userDAO;
    
    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is already logged in
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            if ("ADMIN".equals(user.getRole())) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            } else {
                response.sendRedirect(request.getContextPath() + "/student/dashboard");
            }
            return;
        }
        
        // Forward to login page
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Get parameters
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String rememberMe = request.getParameter("rememberMe");
        
        // Validate input
        if (!ValidationUtil.areRequiredFieldsFilled(username, password)) {
            request.setAttribute("error", "Username and password are required.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }
        
        try {
            // Authenticate user
            User user = userDAO.authenticateUser(username, password);
            
            if (user != null) {
                if (!user.isActive()) {
                    request.setAttribute("error", "Your account has been deactivated. Please contact administrator.");
                    request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
                    return;
                }
                
                // Create session
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
                session.setMaxInactiveInterval(30 * 60); // 30 minutes
                
                // Handle remember me
                if ("on".equals(rememberMe)) {
                    Cookie usernameCookie = new Cookie("username", username);
                    Cookie rememberMeCookie = new Cookie("rememberMe", "true");
                    
                    usernameCookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
                    rememberMeCookie.setMaxAge(30 * 24 * 60 * 60);
                    
                    response.addCookie(usernameCookie);
                    response.addCookie(rememberMeCookie);
                } else {
                    // Clear cookies
                    Cookie usernameCookie = new Cookie("username", "");
                    Cookie rememberMeCookie = new Cookie("rememberMe", "");
                    
                    usernameCookie.setMaxAge(0);
                    rememberMeCookie.setMaxAge(0);
                    
                    response.addCookie(usernameCookie);
                    response.addCookie(rememberMeCookie);
                }
                
                // Log successful login
                logUserActivity(user.getUserId(), "LOGIN", "Successful login from IP: " + request.getRemoteAddr(), request);
                
                // Redirect based on role
                if ("ADMIN".equals(user.getRole())) {
                    response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                } else {
                    response.sendRedirect(request.getContextPath() + "/student/dashboard");
                }
                
            } else {
                // Log failed login attempt
                logFailedLogin(username, request);
                
                request.setAttribute("error", "Invalid username or password.");
                request.setAttribute("username", username);
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "An error occurred during login. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
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
            
            try (Connection connection = com.studentmanagement.util.DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                
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
    
    /**
     * Log failed login attempt
     * @param username Username that attempted login
     * @param request HTTP request
     */
    private void logFailedLogin(String username, HttpServletRequest request) {
        try {
            String sql = "INSERT INTO activity_log (user_id, action, description, ip_address, user_agent) VALUES (?, ?, ?, ?, ?)";
            
            try (Connection connection = com.studentmanagement.util.DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                
                statement.setNull(1, java.sql.Types.INTEGER);
                statement.setString(2, "FAILED_LOGIN");
                statement.setString(3, "Failed login attempt for username: " + username);
                statement.setString(4, request.getRemoteAddr());
                statement.setString(5, request.getHeader("User-Agent"));
                
                statement.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("Error logging failed login: " + e.getMessage());
        }
    }
}