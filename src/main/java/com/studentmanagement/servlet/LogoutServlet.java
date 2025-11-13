package com.studentmanagement.servlet;

import com.studentmanagement.model.User;
import com.studentmanagement.util.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("serial")
//@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LogoutServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Integer userId = null;

        if (session != null) {
            // Prefer stored User object (most of your app stores User)
            Object userObj = session.getAttribute("user");
            if (userObj instanceof User) {
                userId = ((User) userObj).getUserId();
            } else {
                // Fallback for older code that stored userId directly
                Object uid = session.getAttribute("userId");
                if (uid instanceof Integer) {
                    userId = (Integer) uid;
                }
            }

            // Log user activity before invalidating session
            if (userId != null) {
                try {
                    logUserActivity(userId, "LOGOUT", "User logged out", request);
                } catch (Exception e) {
                    // don't block logout for logging failures
                    LOGGER.log(Level.WARNING, "Error logging logout activity", e);
                }
            }

            // Invalidate session
            try {
                session.invalidate();
            } catch (IllegalStateException ise) {
                // session already invalidated; ignore
                LOGGER.fine("Session already invalidated during logout.");
            }
        }

        // Clear cookies (use path "/" to ensure cookies are removed)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if ("username".equals(name) || "rememberMe".equals(name)) {
                    Cookie clear = new Cookie(name, "");
                    clear.setMaxAge(0);
                    clear.setPath("/"); // ensure cookie removal
                    response.addCookie(clear);
                }
            }
        }

        // Redirect to login page with a success flag (no new session required)
        String loginUrl = request.getContextPath() + "/login?success=loggedout";
        response.sendRedirect(loginUrl);
    }

    /**
     * Log user activity into DB (non-blocking; errors are logged)
     */
    private void logUserActivity(int userId, String action, String description, HttpServletRequest request) {
        String sql = "INSERT INTO activity_log (user_id, action, description, ip_address, user_agent) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, action);
            statement.setString(3, description);
            statement.setString(4, request.getRemoteAddr());
            String ua = request.getHeader("User-Agent");
            statement.setString(5, ua != null ? ua : "");
            statement.executeUpdate();

        } catch (Exception e) {
            // Logging failure shouldn't prevent logout — log and continue
            LOGGER.log(Level.WARNING, "Error logging user activity", e);
        }
    }
}
