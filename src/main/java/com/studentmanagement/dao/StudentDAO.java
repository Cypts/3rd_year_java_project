package com.studentmanagement.dao;

import com.studentmanagement.model.Student;
import com.studentmanagement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
    
    private static final String INSERT_STUDENT = "INSERT INTO students (user_id, first_name, last_name, date_of_birth, gender, phone, address, city, state, zip_code, country, course_id, enrollment_year) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_STUDENT_BY_ID = "SELECT s.*, u.email, u.username, c.course_name FROM students s JOIN users u ON s.user_id = u.user_id JOIN courses c ON s.course_id = c.course_id WHERE s.student_id = ?";
    private static final String SELECT_STUDENT_BY_USER_ID = "SELECT s.*, u.email, u.username, c.course_name FROM students s JOIN users u ON s.user_id = u.user_id JOIN courses c ON s.course_id = c.course_id WHERE s.user_id = ?";
    private static final String SELECT_ALL_STUDENTS = "SELECT s.*, u.email, u.username, c.course_name FROM students s JOIN users u ON s.user_id = u.user_id JOIN courses c ON s.course_id = c.course_id ORDER BY s.registration_date DESC";
    private static final String SELECT_STUDENTS_BY_STATUS = "SELECT s.*, u.email, u.username, c.course_name FROM students s JOIN users u ON s.user_id = u.user_id JOIN courses c ON s.course_id = c.course_id WHERE s.status = ? ORDER BY s.registration_date DESC";
    private static final String SELECT_STUDENTS_BY_COURSE = "SELECT s.*, u.email, u.username, c.course_name FROM students s JOIN users u ON s.user_id = u.user_id JOIN courses c ON s.course_id = c.course_id WHERE s.course_id = ? ORDER BY s.registration_date DESC";
    private static final String UPDATE_STUDENT = "UPDATE students SET first_name = ?, last_name = ?, date_of_birth = ?, gender = ?, phone = ?, address = ?, city = ?, state = ?, zip_code = ?, country = ?, course_id = ?, enrollment_year = ? WHERE student_id = ?";
    private static final String UPDATE_STUDENT_STATUS = "UPDATE students SET status = ?, approved_by = ?, approved_date = ?, rejection_reason = ? WHERE student_id = ?";
    private static final String DELETE_STUDENT = "DELETE FROM students WHERE student_id = ?";
    private static final String COUNT_STUDENTS_BY_STATUS = "SELECT COUNT(*) FROM students WHERE status = ?";
    private static final String SEARCH_STUDENTS = "SELECT s.*, u.email, u.username, c.course_name FROM students s JOIN users u ON s.user_id = u.user_id JOIN courses c ON s.course_id = c.course_id WHERE s.first_name LIKE ? OR s.last_name LIKE ? OR u.email LIKE ? OR u.username LIKE ? ORDER BY s.registration_date DESC";
    
    /**
     * Register a new student
     * @param student Student object with registration details
     * @return Student ID if successful, -1 if failed
     */
    public int registerStudent(Student student) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(INSERT_STUDENT, Statement.RETURN_GENERATED_KEYS);
            
            statement.setInt(1, student.getUserId());
            statement.setString(2, student.getFirstName());
            statement.setString(3, student.getLastName());
            statement.setDate(4, new java.sql.Date(student.getDateOfBirth().getTime()));
            statement.setString(5, student.getGender());
            statement.setString(6, student.getPhone());
            statement.setString(7, student.getAddress());
            statement.setString(8, student.getCity());
            statement.setString(9, student.getState());
            statement.setString(10, student.getZipCode());
            statement.setString(11, student.getCountry());
            statement.setInt(12, student.getCourseId());
            statement.setInt(13, student.getEnrollmentYear());
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                return -1;
            }
            
            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                return -1;
            }
            
        } catch (SQLException e) {
            System.err.println("Error registering student: " + e.getMessage());
            e.printStackTrace();
            return -1;
        } finally {
            closeResources(connection, statement, generatedKeys);
        }
    }
    
    /**
     * Get student by ID
     * @param studentId Student ID
     * @return Student object or null
     */
    public Student getStudentById(int studentId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(SELECT_STUDENT_BY_ID);
            statement.setInt(1, studentId);
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return extractStudentFromResultSet(resultSet);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting student by ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return null;
    }
    
    /**
     * Get student by user ID
     * @param userId User ID
     * @return Student object or null
     */
    public Student getStudentByUserId(int userId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(SELECT_STUDENT_BY_USER_ID);
            statement.setInt(1, userId);
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return extractStudentFromResultSet(resultSet);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting student by user ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return null;
    }
    
    /**
     * Get all students
     * @return List of students
     */
    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(SELECT_ALL_STUDENTS);
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                students.add(extractStudentFromResultSet(resultSet));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all students: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return students;
    }
    
    /**
     * Get students by status
     * @param status Student status
     * @return List of students
     */
    public List<Student> getStudentsByStatus(String status) {
        List<Student> students = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(SELECT_STUDENTS_BY_STATUS);
            statement.setString(1, status);
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                students.add(extractStudentFromResultSet(resultSet));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting students by status: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return students;
    }
    
    /**
     * Get students by course
     * @param courseId Course ID
     * @return List of students
     */
    public List<Student> getStudentsByCourse(int courseId) {
        List<Student> students = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(SELECT_STUDENTS_BY_COURSE);
            statement.setInt(1, courseId);
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                students.add(extractStudentFromResultSet(resultSet));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting students by course: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return students;
    }
    
    /**
     * Search students
     * @param searchTerm Search term
     * @return List of students matching search criteria
     */
    public List<Student> searchStudents(String searchTerm) {
        List<Student> students = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(SEARCH_STUDENTS);
            
            String likeTerm = "%" + searchTerm + "%";
            statement.setString(1, likeTerm);
            statement.setString(2, likeTerm);
            statement.setString(3, likeTerm);
            statement.setString(4, likeTerm);
            
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                students.add(extractStudentFromResultSet(resultSet));
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching students: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return students;
    }
    
    /**
     * Update student details
     * @param student Student object with updated details
     * @return true if successful, false otherwise
     */
    public boolean updateStudent(Student student) {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(UPDATE_STUDENT);
            
            statement.setString(1, student.getFirstName());
            statement.setString(2, student.getLastName());
            statement.setDate(3, new java.sql.Date(student.getDateOfBirth().getTime()));
            statement.setString(4, student.getGender());
            statement.setString(5, student.getPhone());
            statement.setString(6, student.getAddress());
            statement.setString(7, student.getCity());
            statement.setString(8, student.getState());
            statement.setString(9, student.getZipCode());
            statement.setString(10, student.getCountry());
            statement.setInt(11, student.getCourseId());
            statement.setInt(12, student.getEnrollmentYear());
            statement.setInt(13, student.getStudentId());
            
            return statement.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating student: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            closeResources(connection, statement, null);
        }
    }
    
    /**
     * Update student status
     * @param studentId Student ID
     * @param status New status
     * @param approvedBy Admin user ID who approved/rejected
     * @param rejectionReason Rejection reason (if applicable)
     * @return true if successful, false otherwise
     */
    public boolean updateStudentStatus(int studentId, String status, Integer approvedBy, String rejectionReason) {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(UPDATE_STUDENT_STATUS);
            
            statement.setString(1, status);
            
            if (approvedBy != null) {
                statement.setInt(2, approvedBy);
            } else {
                statement.setNull(2, java.sql.Types.INTEGER);
            }
            
            if ("APPROVED".equals(status)) {
                statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            } else {
                statement.setNull(3, java.sql.Types.TIMESTAMP);
            }
            
            statement.setString(4, rejectionReason);
            statement.setInt(5, studentId);
            
            return statement.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating student status: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            closeResources(connection, statement, null);
        }
    }
    
    /**
     * Delete student
     * @param studentId Student ID
     * @return true if successful, false otherwise
     */
    public boolean deleteStudent(int studentId) {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(DELETE_STUDENT);
            statement.setInt(1, studentId);
            
            return statement.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting student: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            closeResources(connection, statement, null);
        }
    }
    
    /**
     * Count students by status
     * @param status Student status
     * @return Number of students
     */
    public int countStudentsByStatus(String status) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(COUNT_STUDENTS_BY_STATUS);
            statement.setString(1, status);
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error counting students by status: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return 0;
    }
    
    /**
     * Extract student from ResultSet
     * @param resultSet ResultSet containing student data
     * @return Student object
     * @throws SQLException if extraction fails
     */
    private Student extractStudentFromResultSet(ResultSet resultSet) throws SQLException {
        Student student = new Student();
        student.setStudentId(resultSet.getInt("student_id"));
        student.setUserId(resultSet.getInt("user_id"));
        student.setFirstName(resultSet.getString("first_name"));
        student.setLastName(resultSet.getString("last_name"));
        student.setDateOfBirth(resultSet.getDate("date_of_birth"));
        student.setGender(resultSet.getString("gender"));
        student.setPhone(resultSet.getString("phone"));
        student.setAddress(resultSet.getString("address"));
        student.setCity(resultSet.getString("city"));
        student.setState(resultSet.getString("state"));
        student.setZipCode(resultSet.getString("zip_code"));
        student.setCountry(resultSet.getString("country"));
        student.setCourseId(resultSet.getInt("course_id"));
        student.setEnrollmentYear(resultSet.getInt("enrollment_year"));
        student.setStatus(resultSet.getString("status"));
        student.setRegistrationDate(resultSet.getTimestamp("registration_date"));
        
        int approvedBy = resultSet.getInt("approved_by");
        student.setApprovedBy(resultSet.wasNull() ? null : approvedBy);
        student.setApprovedDate(resultSet.getTimestamp("approved_date"));
        student.setRejectionReason(resultSet.getString("rejection_reason"));
        
        // Additional fields
        student.setCourseName(resultSet.getString("course_name"));
        student.setEmail(resultSet.getString("email"));
        student.setUsername(resultSet.getString("username"));
        
        return student;
    }
    
    /**
     * Close database resources
     * @param connection Connection to close
     * @param statement Statement to close
     * @param resultSet ResultSet to close
     */
    private void closeResources(Connection connection, PreparedStatement statement, ResultSet resultSet) {
        try {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        } catch (SQLException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }
}