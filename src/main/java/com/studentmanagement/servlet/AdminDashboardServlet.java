package com.studentmanagement.servlet;

import com.studentmanagement.dao.CourseDAO;
import com.studentmanagement.dao.StudentDAO;
import com.studentmanagement.dao.UserDAO;
import com.studentmanagement.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AdminDashboardServlet.class.getName());

    private UserDAO userDAO;
    private StudentDAO studentDAO;
    private CourseDAO courseDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = new UserDAO();
        studentDAO = new StudentDAO();
        courseDAO = new CourseDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1) Authentication check
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Object userObj = session.getAttribute("user");
        if (!(userObj instanceof User)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) userObj;
        String role = user.getRole();
        if (role == null || !"ADMIN".equalsIgnoreCase(role.trim())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // 2) Load dashboard data (null-safe)
        try {
            List<?> allStudents = studentDAO.getAllStudents();
            int totalStudents = (allStudents != null) ? allStudents.size() : 0;

            int pendingStudents = safeCountStudentsByStatus("PENDING");
            int approvedStudents = safeCountStudentsByStatus("APPROVED");
            int rejectedStudents = safeCountStudentsByStatus("REJECTED");
            int incompleteStudents = safeCountStudentsByStatus("INCOMPLETE");

            int totalAdmins = safeCountUsersByRole("ADMIN");

            List<?> allCourses = courseDAO.getAllCourses();
            int totalCourses = (allCourses != null) ? allCourses.size() : 0;

            // Set attributes for JSP
            request.setAttribute("totalStudents", totalStudents);
            request.setAttribute("pendingStudents", pendingStudents);
            request.setAttribute("approvedStudents", approvedStudents);
            request.setAttribute("rejectedStudents", rejectedStudents);
            request.setAttribute("incompleteStudents", incompleteStudents);
            request.setAttribute("totalAdmins", totalAdmins);
            request.setAttribute("totalCourses", totalCourses);

            // Forward to dashboard JSP
            request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp")
                   .forward(request, response);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading admin dashboard", e);
            request.setAttribute("error", "Error loading dashboard data.");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }

    // Helpers
    private int safeCountStudentsByStatus(String status) {
        try {
            Integer c = studentDAO.countStudentsByStatus(status);
            return (c != null) ? c : 0;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "countStudentsByStatus failed for: " + status, e);
            return 0;
        }
    }

    private int safeCountUsersByRole(String role) {
        try {
            Integer c = userDAO.countUsersByRole(role);
            return (c != null) ? c : 0;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "countUsersByRole failed for: " + role, e);
            return 0;
        }
    }
}
