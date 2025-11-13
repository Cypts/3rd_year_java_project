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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RegisterServlet
 *
 * Handles student registration: validates input, registers a User and a Student.
 * If student creation fails after user creation, user is removed (basic rollback).
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(RegisterServlet.class.getName());

    private UserDAO userDAO;
    private StudentDAO studentDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = new UserDAO();
        studentDAO = new StudentDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // If already logged in send to dashboard
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
            // Validate form input and build model objects
            User user = validateAndExtractUserData(request);
            Student student = validateAndExtractStudentData(request);

            if (user == null || student == null) {
                // validation errors are set as request attributes inside helper methods
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
                return;
            }

            // Check uniqueness (set field-specific errors)
            if (userDAO.usernameExists(user.getUsername())) {
                request.setAttribute("usernameError", "Username already exists. Please choose a different username.");
                request.setAttribute("username", user.getUsername());
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
                return;
            }
            if (userDAO.emailExists(user.getEmail())) {
                request.setAttribute("emailError", "Email address already registered. Please use a different email.");
                request.setAttribute("email", user.getEmail());
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
                return;
            }

            // Register user
            int userId = userDAO.registerUser(user);
            if (userId == -1) {
                request.setAttribute("generalError", "Failed to register user. Please try again.");
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
                return;
            }

            // Link student -> user and register student
            student.setUserId(userId);
            int studentId = studentDAO.registerStudent(student);
            if (studentId == -1) {
                // rollback user on student failure
                try {
                    userDAO.deleteUser(userId);
                } catch (Exception ex) {
                    logger.log(Level.WARNING, "Failed to rollback user after student creation failure (userId=" + userId + ")", ex);
                }
                request.setAttribute("generalError", "Failed to register student details. Please try again.");
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
                return;
            }

            // success -> set flash message in session and redirect to login
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("success", "Registration successful! Please login to continue.");
            response.sendRedirect(request.getContextPath() + "/login");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Registration error", e);
            request.setAttribute("generalError", "An error occurred during registration. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
        }
    }

    /* ---------------- Helper methods ---------------- */

    /**
     * Validate and extract user data from request
     * @return User or null if validation fails (also sets request attributes for feedback)
     */
    private User validateAndExtractUserData(HttpServletRequest request) {
        String username = safeTrim(request.getParameter("username"));
        String email = safeTrim(request.getParameter("email"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // Preserve values for redisplay (avoid putting sensitive data like password back)
        request.setAttribute("username", username);
        request.setAttribute("email", email);

        // Required fields
        if (!ValidationUtil.areRequiredFieldsFilled(username, email, password, confirmPassword)) {
            request.setAttribute("generalError", "Please fill all required fields.");
            return null;
        }

        if (!ValidationUtil.isValidUsername(username)) {
            request.setAttribute("usernameError", "Username must be 3-20 characters long and contain only letters, numbers, and underscores.");
            return null;
        }

        if (!ValidationUtil.isValidEmail(email)) {
            request.setAttribute("emailError", "Please enter a valid email address.");
            return null;
        }

        if (!ValidationUtil.isValidPassword(password)) {
            request.setAttribute("passwordError", "Password must be at least 8 characters long and contain uppercase, lowercase, digit, and special character.");
            return null;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("passwordError", "Passwords do not match.");
            request.setAttribute("confirmPasswordError", "Passwords do not match.");
            return null;
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPassword(password); // hashing typically handled in DAO.registerUser
        user.setRole("STUDENT");

        return user;
    }

    /**
     * Validate and extract student data from request
     * @return Student or null if validation fails (also sets request attributes for feedback)
     */
    private Student validateAndExtractStudentData(HttpServletRequest request) {
        String firstName = safeTrim(request.getParameter("firstName"));
        String lastName = safeTrim(request.getParameter("lastName"));
        String dateOfBirth = safeTrim(request.getParameter("dateOfBirth")); // expected "yyyy-MM-dd"
        String gender = safeTrim(request.getParameter("gender"));
        String phone = safeTrim(request.getParameter("phone"));
        String address = safeTrim(request.getParameter("address"));
        String city = safeTrim(request.getParameter("city"));
        String state = safeTrim(request.getParameter("state"));
        String zipCode = safeTrim(request.getParameter("zipCode"));
        String courseId = safeTrim(request.getParameter("courseId"));
        String enrollmentYear = safeTrim(request.getParameter("enrollmentYear"));

        // Preserve form values for redisplay
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

        // Required fields
        if (!ValidationUtil.areRequiredFieldsFilled(firstName, lastName, dateOfBirth, gender, phone,
                address, city, state, zipCode, courseId, enrollmentYear)) {
            request.setAttribute("generalError", "Please fill all student information fields.");
            return null;
        }

        if (!ValidationUtil.isValidName(firstName)) {
            request.setAttribute("firstNameError", "First name must contain only letters and spaces (2-50 characters).");
            return null;
        }
        if (!ValidationUtil.isValidName(lastName)) {
            request.setAttribute("lastNameError", "Last name must contain only letters and spaces (2-50 characters).");
            return null;
        }

        LocalDate dobLocal;
        try {
            dobLocal = LocalDate.parse(dateOfBirth); // ISO yyyy-MM-dd
            if (!ValidationUtil.isValidDateOfBirth(dobLocal.getYear(), dobLocal.getMonthValue(), dobLocal.getDayOfMonth())) {
                request.setAttribute("dateOfBirthError", "Student must be at least 16 years old.");
                return null;
            }
        } catch (DateTimeParseException e) {
            request.setAttribute("dateOfBirthError", "Please enter a valid date of birth (YYYY-MM-DD).");
            return null;
        }

        if (!ValidationUtil.isValidPhone(phone)) {
            request.setAttribute("phoneError", "Please enter a valid 10-digit phone number.");
            return null;
        }

        if (!ValidationUtil.isValidZipCode(zipCode)) {
            request.setAttribute("zipCodeError", "Please enter a valid 6-digit ZIP code.");
            return null;
        }

        int courseIdInt;
        int enrollmentYearInt;
        try {
            courseIdInt = Integer.parseInt(courseId);
            enrollmentYearInt = Integer.parseInt(enrollmentYear);

            if (!ValidationUtil.isValidEnrollmentYear(enrollmentYearInt)) {
                request.setAttribute("enrollmentYearError", "Please enter a valid enrollment year.");
                return null;
            }
        } catch (NumberFormatException e) {
            request.setAttribute("enrollmentYearError", "Please enter valid numeric values for course and enrollment year.");
            return null;
        }

        // convert LocalDate -> java.util.Date (start of day in system zone)
        Date dob = Date.from(dobLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());

        Student student = new Student();
        student.setFirstName(ValidationUtil.sanitizeInput(firstName));
        student.setLastName(ValidationUtil.sanitizeInput(lastName));
        student.setDateOfBirth(dob);
        student.setGender(gender);
        student.setPhone(phone);
        student.setAddress(ValidationUtil.sanitizeInput(address));
        student.setCity(ValidationUtil.sanitizeInput(city));
        student.setState(ValidationUtil.sanitizeInput(state));
        student.setZipCode(zipCode);
        student.setCountry("India");
        student.setCourseId(courseIdInt);
        student.setEnrollmentYear(enrollmentYearInt);
        student.setStatus("INCOMPLETE");

        return student;
    }

    /** Safely trim a possibly-null string */
    private String safeTrim(String s) {
        return (s == null) ? null : s.trim();
    }
}
