package com.studentmanagement.servlet;

import com.studentmanagement.dao.UserDAO;
import com.studentmanagement.model.User;
import com.studentmanagement.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * LoginServlet
 *
 * - Uses UserDAO.authenticateUser(username, password)
 * - Uses ValidationUtil.areRequiredFieldsFilled(...)
 * - Changes session id after successful login to prevent fixation
 * - Minimal IP-based brute-force protection (in-memory)
 */
//@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(LoginServlet.class.getName());
    private static final long serialVersionUID = 1L;

    private UserDAO userDAO;

    // Simple in-memory brute-force protection
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_SECONDS = 10 * 60; // 10 minutes

    private static class FailedInfo {
        int attempts;
        long lockedUntilEpochSec; // 0 if not locked
    }

    // map IP address -> FailedInfo
    private static final Map<String, FailedInfo> failedMap = new ConcurrentHashMap<>();

    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // If already logged in send to appropriate dashboard
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            User u = (User) session.getAttribute("user");
            if (u != null && "ADMIN".equalsIgnoreCase(u.getRole())) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            } else {
                response.sendRedirect(request.getContextPath() + "/student/dashboard");
            }
            return;
        }

        // show login page
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        String clientIp = getClientIp(request);
        if (isIpLocked(clientIp)) {
            logger.warning("Blocked login attempt from locked IP: " + clientIp);
            request.setAttribute("error", "Too many failed attempts. Try again later.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        // Validate presence of fields
        if (!ValidationUtil.areRequiredFieldsFilled(username, password)) {
            request.setAttribute("error", "Username and password are required.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        try {
            User user = userDAO.authenticateUser(username, password);

            if (user != null) {
                // successful login: reset failed attempts for IP
                clearFailedAttempts(clientIp);

                // create or use session & prevent session fixation
                HttpSession session = request.getSession(true);
                // changeSessionId is present in Servlet 3.1+; safe to call if supported
                try {
                    request.changeSessionId();
                } catch (Throwable ignored) {
                    // older containers may not support it
                }

                session.setAttribute("user", user);
                session.setMaxInactiveInterval(30 * 60); // 30 minutes

                // debug: confirm session attribute
                System.out.println("LoginServlet: session user = " + session.getAttribute("user"));

                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                } else {
                    response.sendRedirect(request.getContextPath() + "/student/dashboard");
                }
            } else {
                // failed login
                recordFailedAttempt(clientIp);
                logger.info("Failed login for username=" + username + " from IP=" + clientIp);
                request.setAttribute("error", "Invalid username or password.");
                request.setAttribute("username", username);
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during login process", e);
            request.setAttribute("error", "An internal error occurred. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    /* ------------------- Brute-force protection helpers ------------------- */

    private boolean isIpLocked(String ip) {
        FailedInfo info = failedMap.get(ip);
        if (info == null) return false;
        long now = Instant.now().getEpochSecond();
        if (info.lockedUntilEpochSec > now) {
            return true; // still locked
        }
        // lock expired: reset
        if (info.attempts >= MAX_ATTEMPTS) {
            failedMap.remove(ip);
        }
        return false;
    }

    private void recordFailedAttempt(String ip) {
        long now = Instant.now().getEpochSecond();
        failedMap.compute(ip, (k, info) -> {
            if (info == null) {
                info = new FailedInfo();
                info.attempts = 1;
                info.lockedUntilEpochSec = 0;
                return info;
            } else {
                info.attempts++;
                if (info.attempts >= MAX_ATTEMPTS) {
                    info.lockedUntilEpochSec = now + LOCK_DURATION_SECONDS;
                    logger.warning("IP " + ip + " locked until epoch " + info.lockedUntilEpochSec);
                }
                return info;
            }
        });
    }

    private void clearFailedAttempts(String ip) {
        failedMap.remove(ip);
    }

    private String getClientIp(HttpServletRequest request) {
        // Try X-Forwarded-For header first (if behind proxy); otherwise remote addr
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isEmpty()) {
            // might be comma-separated list; take first
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
