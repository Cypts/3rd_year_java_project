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

@WebServlet("/student/dashboard")
public class StudentDashboardServlet extends HttpServlet {
    
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
        
        User user = (User) session.getAttribute("user");
        if (!"STUDENT".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        try {
            // Get student details
            Student student = studentDAO.getStudentByUserId(user.getUserId());
            
            if (student == null) {
                // Student not found, redirect to registration completion
                response.sendRedirect(request.getContextPath() + "/student/complete-profile");
                return;
            }
            
            // Get available courses for dropdown
            List<Course> courses = courseDAO.getAllCourses();
            
            // Set attributes for JSP
            request.setAttribute("student", student);
            request.setAttribute("courses", courses);
            
            // Forward to dashboard
            request.getRequestDispatcher("/WEB-INF/views/student/dashboard.jsp").forward(request, response);
            
        } catch (Exception e) {
            System.err.println("Error loading student dashboard: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "Error loading dashboard data.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
}