package com.studentmanagement.servlet;

import com.studentmanagement.dao.UserDAO;
import com.studentmanagement.dao.StudentDAO;
import com.studentmanagement.model.User;
import com.studentmanagement.model.Student;
import com.studentmanagement.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    
    private UserDAO userDAO;
    private StudentDAO studentDAO;
    private SimpleDateFormat dateFormat;
    
    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
        studentDAO = new StudentDAO();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is already logged in
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
            return;
        }
        
        // Forward to registration page
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Validate and extract form data
            User user = validateAndExtractUserData(request);
            Student student = validateAndExtractStudentData(request);
            
            if (user == null || student == null) {
                // Validation errors are already set in request attributes
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
                return;
            }
            
            // Check if username already exists
            if (userDAO.usernameExists(user.getUsername())) {
                request.setAttribute("error", "Username already exists. Please choose a different username.");
                request.setAttribute("username", user.getUsername());
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
                return;
            }
            
            // Check if email already exists
            if (userDAO.emailExists(user.getEmail())) {
                request.setAttribute("error", "Email address already registered. Please use a different email.");
                request.setAttribute("email", user.getEmail());
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
                return;
            }
            
            // Register user
            int userId = userDAO.registerUser(user);
            if (userId == -1) {
                request.setAttribute("error", "Failed to register user. Please try again.");
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
                return;
            }
            
            // Set user ID for student
            student.setUserId(userId);
            
            // Register student
            int studentId = studentDAO.registerStudent(student);
            if (studentId == -1) {
                // Rollback user registration if student registration fails
                userDAO.deleteUser(userId);
                request.setAttribute("error", "Failed to register student details. Please try again.");
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
                return;
            }
            
            // Registration successful - redirect to login with success message
            request.getSession().setAttribute("success", "Registration successful! Please login to continue.");
            response.sendRedirect(request.getContextPath() + "/login");
            
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "An error occurred during registration. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
        }
    }
    
    /**
     * Validate and extract user data from request
     * @param request HTTP request
     * @return User object or null if validation fails
     */
    private User validateAndExtractUserData(HttpServletRequest request) {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        
        // Preserve form data
        request.setAttribute("username", username);
        request.setAttribute("email", email);
        
        // Validate required fields
        if (!ValidationUtil.areRequiredFieldsFilled(username, email, password, confirmPassword)) {
            request.setAttribute("error", "All fields are required.");
            return null;
        }
        
        // Validate username
        if (!ValidationUtil.isValidUsername(username)) {
            request.setAttribute("error", "Username must be 3-20 characters long and contain only letters, numbers, and underscores.");
            return null;
        }
        
        // Validate email
        if (!ValidationUtil.isValidEmail(email)) {
            request.setAttribute("error", "Please enter a valid email address.");
            return null;
        }
        
        // Validate password
        if (!ValidationUtil.isValidPassword(password)) {
            request.setAttribute("error", "Password must be at least 8 characters long and contain uppercase, lowercase, digit, and special character.");
            return null;
        }
        
        // Check password confirmation
        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match.");
            return null;
        }
        
        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPassword(password);
        user.setRole("STUDENT");
        
        return user;
    }
    
    /**
     * Validate and extract student data from request
     * @param request HTTP request
     * @return Student object or null if validation fails
     */
    private Student validateAndExtractStudentData(HttpServletRequest request) {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String dateOfBirth = request.getParameter("dateOfBirth");
        String gender = request.getParameter("gender");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String city = request.getParameter("city");
        String state = request.getParameter("state");
        String zipCode = request.getParameter("zipCode");
        String courseId = request.getParameter("courseId");
        String enrollmentYear = request.getParameter("enrollmentYear");
        
        // Preserve form data
        request.setAttribute("firstName", firstName);
        request.setAttribute("lastName", lastName);
        request.setAttribute("dateOfBirth", dateOfBirth);
        request.setAttribute("gender", gender);
        request.setAttribute("phone", phone);
        request.setAttribute("address", address);
        request.setAttribute("city", city);
        request.setAttribute("state", state);
        request.setAttribute("zipCode", zipCode);
        request.setAttribute("courseId", courseId);
        request.setAttribute("enrollmentYear", enrollmentYear);
        
        // Validate required fields
        if (!ValidationUtil.areRequiredFieldsFilled(firstName, lastName, dateOfBirth, gender, phone, address, city, state, zipCode, courseId, enrollmentYear)) {
            request.setAttribute("error", "All student information fields are required.");
            return null;
        }
        
        // Validate names
        if (!ValidationUtil.isValidName(firstName)) {
            request.setAttribute("error", "First name must contain only letters and spaces (2-50 characters).");
            return null;
        }
        
        if (!ValidationUtil.isValidName(lastName)) {
            request.setAttribute("error", "Last name must contain only letters and spaces (2-50 characters).");
            return null;
        }
        
        // Validate date of birth
        Date dob;
        try {
            dob = dateFormat.parse(dateOfBirth);
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(dob);
            
            if (!ValidationUtil.isValidDateOfBirth(cal.get(java.util.Calendar.YEAR), 
                                                   cal.get(java.util.Calendar.MONTH) + 1, 
                                                   cal.get(java.util.Calendar.DAY_OF_MONTH))) {
                request.setAttribute("error", "Student must be at least 16 years old.");
                return null;
            }
        } catch (ParseException e) {
            request.setAttribute("error", "Please enter a valid date of birth.");
            return null;
        }
        
        // Validate phone
        if (!ValidationUtil.isValidPhone(phone)) {
            request.setAttribute("error", "Please enter a valid 10-digit phone number.");
            return null;
        }
        
        // Validate ZIP code
        if (!ValidationUtil.isValidZipCode(zipCode)) {
            request.setAttribute("error", "Please enter a valid 6-digit ZIP code.");
            return null;
        }
        
        // Validate course and enrollment year
        int courseIdInt, enrollmentYearInt;
        try {
            courseIdInt = Integer.parseInt(courseId);
            enrollmentYearInt = Integer.parseInt(enrollmentYear);
            
            if (!ValidationUtil.isValidEnrollmentYear(enrollmentYearInt)) {
                request.setAttribute("error", "Please enter a valid enrollment year.");
                return null;
            }
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Please enter valid course and enrollment year.");
            return null;
        }
        
        Student student = new Student();
        student.setFirstName(ValidationUtil.sanitizeInput(firstName.trim()));
        student.setLastName(ValidationUtil.sanitizeInput(lastName.trim()));
        student.setDateOfBirth(dob);
        student.setGender(gender);
        student.setPhone(phone.trim());
        student.setAddress(ValidationUtil.sanitizeInput(address.trim()));
        student.setCity(ValidationUtil.sanitizeInput(city.trim()));
        student.setState(ValidationUtil.sanitizeInput(state.trim()));
        student.setZipCode(zipCode.trim());
        student.setCountry("India");
        student.setCourseId(courseIdInt);
        student.setEnrollmentYear(enrollmentYearInt);
        student.setStatus("INCOMPLETE");
        
        return student;
    }
}