package com.studentmanagement.servlet;

import com.studentmanagement.dao.StudentDAO;
import com.studentmanagement.dao.CourseDAO;
import com.studentmanagement.model.User;
import com.studentmanagement.model.Student;
import com.studentmanagement.model.Course;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * StudentDashboardServlet
 *
 * Shows the dashboard for logged-in students. Ensures authentication and role check.
 */
@WebServlet("/student/dashboard")
public class StudentDashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(StudentDashboardServlet.class.getName());

    private StudentDAO studentDAO;
    private CourseDAO courseDAO;

    @Override
    public void init() throws ServletException {
        studentDAO = new StudentDAO();
        courseDAO = new CourseDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check authentication
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Object userObj = session.getAttribute("user");
        if (!(userObj instanceof User)) {
            // unexpected session content — force login
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) userObj;
        if (user.getRole() == null || !"STUDENT".equalsIgnoreCase(user.getRole().trim())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            // Get student details
            Student student = studentDAO.getStudentByUserId(user.getUserId());
            if (student == null) {
                // Student not found, redirect to profile completion
                response.sendRedirect(request.getContextPath() + "/student/complete-profile");
                return;
            }

            // Get available courses (defensive null-check)
            List<Course> courses = courseDAO.getAllCourses();
            if (courses == null) courses = java.util.Collections.emptyList();

            // Set attributes for JSP
            request.setAttribute("student", student);
            request.setAttribute("courses", courses);

            // Forward to dashboard JSP
            request.getRequestDispatcher("/WEB-INF/views/student/dashboard.jsp").forward(request, response);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading student dashboard", e);
            request.setAttribute("error", "Error loading dashboard data.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    // Allow POST to reach the same page (useful for forms)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}
