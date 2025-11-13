package com.studentmanagement.dao;

import com.studentmanagement.model.User;
import com.studentmanagement.util.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    
    private static final String INSERT_USER = "INSERT INTO users (username, email, password, role) VALUES (?, ?, ?, ?)";
    private static final String SELECT_USER_BY_ID = "SELECT * FROM users WHERE user_id = ?";
    private static final String SELECT_USER_BY_USERNAME = "SELECT * FROM users WHERE username = ?";
    private static final String SELECT_USER_BY_EMAIL = "SELECT * FROM users WHERE email = ?";
    private static final String SELECT_ALL_USERS = "SELECT * FROM users ORDER BY created_at DESC";
    private static final String UPDATE_USER = "UPDATE users SET username = ?, email = ?, is_active = ? WHERE user_id = ?";
    private static final String UPDATE_PASSWORD = "UPDATE users SET password = ? WHERE user_id = ?";
    private static final String DELETE_USER = "DELETE FROM users WHERE user_id = ?";
    private static final String COUNT_USERS_BY_ROLE = "SELECT COUNT(*) FROM users WHERE role = ?";
    
    /**
     * Register a new user
     * @param user User object with registration details
     * @return User ID if successful, -1 if failed
     */
    public int registerUser(User user) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS);
            
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            statement.setString(4, user.getRole());
            
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
            System.err.println("Error registering user: " + e.getMessage());
            e.printStackTrace();
            return -1;
        } finally {
            closeResources(connection, statement, generatedKeys);
        }
    }
    
    /**
     * Authenticate user login
     * @param username Username or email
     * @param password Plain text password
     * @return User object if authenticated, null otherwise
     */
    public User authenticateUser(String username, String password) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            
            // Try to authenticate by username first, then by email
            statement = connection.prepareStatement(SELECT_USER_BY_USERNAME);
            statement.setString(1, username);
            resultSet = statement.executeQuery();
            
            if (!resultSet.next()) {
                // Try email if username not found
                closeResources(null, statement, resultSet);
                statement = connection.prepareStatement(SELECT_USER_BY_EMAIL);
                statement.setString(1, username);
                resultSet = statement.executeQuery();
                
                if (!resultSet.next()) {
                    return null;
                }
            }
            
            String hashedPassword = resultSet.getString("password");
            
            // Verify password
            if (BCrypt.checkpw(password, hashedPassword)) {
                return extractUserFromResultSet(resultSet);
            } else {
                return null;
            }
            
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }
    
    /**
     * Get user by ID
     * @param userId User ID
     * @return User object or null
     */
    public User getUserById(int userId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(SELECT_USER_BY_ID);
            statement.setInt(1, userId);
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return extractUserFromResultSet(resultSet);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return null;
    }
    
    /**
     * Get user by username
     * @param username Username
     * @return User object or null
     */
    public User getUserByUsername(String username) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(SELECT_USER_BY_USERNAME);
            statement.setString(1, username);
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return extractUserFromResultSet(resultSet);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting user by username: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return null;
    }
    
    /**
     * Check if username exists
     * @param username Username to check
     * @return true if exists, false otherwise
     */
    public boolean usernameExists(String username) {
        return getUserByUsername(username) != null;
    }
    
    /**
     * Check if email exists
     * @param email Email to check
     * @return true if exists, false otherwise
     */
    public boolean emailExists(String email) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(SELECT_USER_BY_EMAIL);
            statement.setString(1, email);
            resultSet = statement.executeQuery();
            
            return resultSet.next();
            
        } catch (SQLException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }
    
    /**
     * Get all users
     * @return List of users
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(SELECT_ALL_USERS);
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                users.add(extractUserFromResultSet(resultSet));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return users;
    }
    
    /**
     * Update user details
     * @param user User object with updated details
     * @return true if successful, false otherwise
     */
    public boolean updateUser(User user) {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(UPDATE_USER);
            
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setBoolean(3, user.isActive());
            statement.setInt(4, user.getUserId());
            
            return statement.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            closeResources(connection, statement, null);
        }
    }
    
    /**
     * Update user password
     * @param userId User ID
     * @param newPassword New plain text password
     * @return true if successful, false otherwise
     */
    public boolean updatePassword(int userId, String newPassword) {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(UPDATE_PASSWORD);
            
            statement.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            statement.setInt(2, userId);
            
            return statement.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            closeResources(connection, statement, null);
        }
    }
    
    /**
     * Delete user
     * @param userId User ID
     * @return true if successful, false otherwise
     */
    public boolean deleteUser(int userId) {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(DELETE_USER);
            statement.setInt(1, userId);
            
            return statement.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            closeResources(connection, statement, null);
        }
    }
    
    /**
     * Count users by role
     * @param role User role (STUDENT or ADMIN)
     * @return Number of users
     */
    public int countUsersByRole(String role) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(COUNT_USERS_BY_ROLE);
            statement.setString(1, role);
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error counting users by role: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return 0;
    }
    
    /**
     * Extract user from ResultSet
     * @param resultSet ResultSet containing user data
     * @return User object
     * @throws SQLException if extraction fails
     */
    private User extractUserFromResultSet(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setUserId(resultSet.getInt("user_id"));
        user.setUsername(resultSet.getString("username"));
        user.setEmail(resultSet.getString("email"));
        user.setPassword(resultSet.getString("password"));
        user.setRole(resultSet.getString("role"));
        user.setActive(resultSet.getBoolean("is_active"));
        user.setCreatedAt(resultSet.getTimestamp("created_at"));
        user.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        return user;
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