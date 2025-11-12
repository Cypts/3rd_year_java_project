package com.studentmanagement.servlet;

import com.studentmanagement.dao.*;
import com.studentmanagement.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {
    
    private UserDAO userDAO;
    private StudentDAO studentDAO;
    private CourseDAO courseDAO;
    
    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
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
        
        User user = (User) session.getAttribute("user");
        if (!"ADMIN".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        try {
            // Get dashboard statistics
            int totalStudents = studentDAO.getAllStudents().size();
            int pendingStudents = studentDAO.countStudentsByStatus("PENDING");
            int approvedStudents = studentDAO.countStudentsByStatus("APPROVED");
            int rejectedStudents = studentDAO.countStudentsByStatus("REJECTED");
            int incompleteStudents = studentDAO.countStudentsByStatus("INCOMPLETE");
            int totalAdmins = userDAO.countUsersByRole("ADMIN");
            int totalCourses = courseDAO.getAllCourses().size();
            
            // Set attributes for JSP
            request.setAttribute("totalStudents", totalStudents);
            request.setAttribute("pendingStudents", pendingStudents);
            request.setAttribute("approvedStudents", approvedStudents);
            request.setAttribute("rejectedStudents", rejectedStudents);
            request.setAttribute("incompleteStudents", incompleteStudents);
            request.setAttribute("totalAdmins", totalAdmins);
            request.setAttribute("totalCourses", totalCourses);
            
            // Forward to dashboard
            request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);
            
        } catch (Exception e) {
            System.err.println("Error loading admin dashboard: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "Error loading dashboard data.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
}