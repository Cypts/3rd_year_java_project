package com.studentmanagement.filter;

import com.studentmanagement.model.User;
import com.studentmanagement.util.DBConnection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AuthenticationFilter
 *
 * Protects /student/* and /admin/* routes and logs sensitive operations.
 */
@WebFilter(filterName = "AuthenticationFilter", urlPatterns = {"/student/*", "/admin/*"})
public class AuthenticationFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("AuthenticationFilter.init()");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  httpRequest  = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        LOGGER.info("AuthenticationFilter.doFilter() - URI=" + httpRequest.getRequestURI());

        HttpSession session = httpRequest.getSession(false);

        // Ensure there is a session
        if (session == null) {
            LOGGER.fine("AuthenticationFilter: no session -> redirect to login");
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }

        // Accept either a full User object or a username string fallback
        Object userObj = session.getAttribute("user");
        if (userObj == null) {
            Object usernameObj = session.getAttribute("username");
            if (usernameObj == null) {
                LOGGER.fine("AuthenticationFilter: no user/username in session -> redirect to login");
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
                return;
            } else {
                LOGGER.fine("AuthenticationFilter: found username in session (fallback): " + usernameObj);
                // proceed without a User object (role checks below will be skipped)
            }
        } else if (!(userObj instanceof User) && !(userObj instanceof String)) {
            // unexpected type
            LOGGER.fine("AuthenticationFilter: session 'user' is unexpected type -> redirect to login");
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }

        User user = (userObj instanceof User) ? (User) userObj : null;
        String role = (user != null && user.getRole() != null) ? user.getRole().trim().toUpperCase() : null;

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();

        // Role-based access control only if we have a role
        if (requestURI.startsWith(contextPath + "/admin/")) {
            if (!"ADMIN".equals(role)) {
                // If no role (null) or not ADMIN, redirect appropriately
                LOGGER.fine("AuthenticationFilter: denied access to admin area for role=" + role);
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/student/dashboard");
                return;
            }
        }

        if (requestURI.startsWith(contextPath + "/student/")) {
            if (!"STUDENT".equals(role)) {
                LOGGER.fine("AuthenticationFilter: denied access to student area for role=" + role);
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/admin/dashboard");
                return;
            }
        }

        // Ensure minimum session timeout
        try {
            if (session.getMaxInactiveInterval() < 30 * 60) {
                session.setMaxInactiveInterval(30 * 60); // 30 minutes
            }
        } catch (IllegalStateException ise) {
            LOGGER.fine("Session invalid when adjusting timeout: " + ise.getMessage());
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }

        // Log access for security monitoring (best-effort)
        try {
            if (user != null) {
                logAccess(user, httpRequest);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Access logging failed", e);
        }

        // Continue
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        LOGGER.info("AuthenticationFilter.destroy()");
    }

    /**
     * Log user access for security monitoring — best-effort; doesn't interrupt request flow.
     */
    private void logAccess(User user, HttpServletRequest request) {
        try {
            String uri = request.getRequestURI();
            String method = request.getMethod();

            // Only log potentially-sensitive operations
            if (uri.contains("delete") || uri.contains("update") || uri.contains("approve") || uri.contains("reject")) {
                String sql = "INSERT INTO activity_log (user_id, action, description, ip_address, user_agent) VALUES (?, ?, ?, ?, ?)";

                try (java.sql.Connection connection = DBConnection.getConnection();
                     java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {

                    statement.setInt(1, user.getUserId());
                    statement.setString(2, "ACCESS_" + method);
                    statement.setString(3, "Accessed: " + uri);
                    statement.setString(4, request.getRemoteAddr());
                    String ua = request.getHeader("User-Agent");
                    statement.setString(5, ua != null ? ua : "");

                    statement.executeUpdate();
                }
            }
        } catch (Exception e) {
            // best-effort logging — swallow exceptions but record locally
            LOGGER.log(Level.WARNING, "Error logging access: " + e.getMessage(), e);
        }
    }
}
