package com.studentmanagement.dao;

import com.studentmanagement.model.Course;
import com.studentmanagement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {
    
    private static final String INSERT_COURSE = "INSERT INTO courses (course_code, course_name, description, duration_years) VALUES (?, ?, ?, ?)";
    private static final String SELECT_COURSE_BY_ID = "SELECT * FROM courses WHERE course_id = ?";
    private static final String SELECT_COURSE_BY_CODE = "SELECT * FROM courses WHERE course_code = ?";
    private static final String SELECT_ALL_COURSES = "SELECT * FROM courses WHERE is_active = TRUE ORDER BY course_name";
    private static final String UPDATE_COURSE = "UPDATE courses SET course_code = ?, course_name = ?, description = ?, duration_years = ? WHERE course_id = ?";
    private static final String DELETE_COURSE = "UPDATE courses SET is_active = FALSE WHERE course_id = ?";
    
    /**
     * Add a new course
     * @param course Course object
     * @return Course ID if successful, -1 if failed
     */
    public int addCourse(Course course) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(INSERT_COURSE, Statement.RETURN_GENERATED_KEYS);
            
            statement.setString(1, course.getCourseCode());
            statement.setString(2, course.getCourseName());
            statement.setString(3, course.getDescription());
            statement.setInt(4, course.getDurationYears());
            
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
            System.err.println("Error adding course: " + e.getMessage());
            e.printStackTrace();
            return -1;
        } finally {
            closeResources(connection, statement, generatedKeys);
        }
    }
    
    /**
     * Get course by ID
     * @param courseId Course ID
     * @return Course object or null
     */
    public Course getCourseById(int courseId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(SELECT_COURSE_BY_ID);
            statement.setInt(1, courseId);
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return extractCourseFromResultSet(resultSet);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting course by ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return null;
    }
    
    /**
     * Get course by code
     * @param courseCode Course code
     * @return Course object or null
     */
    public Course getCourseByCode(String courseCode) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(SELECT_COURSE_BY_CODE);
            statement.setString(1, courseCode);
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return extractCourseFromResultSet(resultSet);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting course by code: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return null;
    }
    
    /**
     * Get all active courses
     * @return List of courses
     */
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(SELECT_ALL_COURSES);
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                courses.add(extractCourseFromResultSet(resultSet));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all courses: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return courses;
    }
    
    /**
     * Update course details
     * @param course Course object with updated details
     * @return true if successful, false otherwise
     */
    public boolean updateCourse(Course course) {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(UPDATE_COURSE);
            
            statement.setString(1, course.getCourseCode());
            statement.setString(2, course.getCourseName());
            statement.setString(3, course.getDescription());
            statement.setInt(4, course.getDurationYears());
            statement.setInt(5, course.getCourseId());
            
            return statement.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating course: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            closeResources(connection, statement, null);
        }
    }
    
    /**
     * Delete course (soft delete)
     * @param courseId Course ID
     * @return true if successful, false otherwise
     */
    public boolean deleteCourse(int courseId) {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(DELETE_COURSE);
            statement.setInt(1, courseId);
            
            return statement.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting course: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            closeResources(connection, statement, null);
        }
    }
    
    /**
     * Extract course from ResultSet
     * @param resultSet ResultSet containing course data
     * @return Course object
     * @throws SQLException if extraction fails
     */
    private Course extractCourseFromResultSet(ResultSet resultSet) throws SQLException {
        Course course = new Course();
        course.setCourseId(resultSet.getInt("course_id"));
        course.setCourseCode(resultSet.getString("course_code"));
        course.setCourseName(resultSet.getString("course_name"));
        course.setDescription(resultSet.getString("description"));
        course.setDurationYears(resultSet.getInt("duration_years"));
        course.setActive(resultSet.getBoolean("is_active"));
        course.setCreatedAt(resultSet.getTimestamp("created_at"));
        return course;
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