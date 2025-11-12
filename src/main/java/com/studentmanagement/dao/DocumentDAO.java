package com.studentmanagement.dao;

import com.studentmanagement.model.Document;
import com.studentmanagement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentDAO {
    
    private static final String INSERT_DOCUMENT = "INSERT INTO documents (student_id, document_type, file_name, file_path, file_size, mime_type) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_DOCUMENT_BY_ID = "SELECT d.*, s.first_name, s.last_name, u.username as verified_by_name FROM documents d JOIN students s ON d.student_id = s.student_id LEFT JOIN users u ON d.verified_by = u.user_id WHERE d.document_id = ?";
    private static final String SELECT_DOCUMENTS_BY_STUDENT = "SELECT d.*, s.first_name, s.last_name, u.username as verified_by_name FROM documents d JOIN students s ON d.student_id = s.student_id LEFT JOIN users u ON d.verified_by = u.user_id WHERE d.student_id = ? ORDER BY d.uploaded_at DESC";
    private static final String SELECT_ALL_DOCUMENTS = "SELECT d.*, s.first_name, s.last_name, u.username as verified_by_name FROM documents d JOIN students s ON d.student_id = s.student_id LEFT JOIN users u ON d.verified_by = u.user_id ORDER BY d.uploaded_at DESC";
    private static final String SELECT_UNVERIFIED_DOCUMENTS = "SELECT d.*, s.first_name, s.last_name, u.username as verified_by_name FROM documents d JOIN students s ON d.student_id = s.student_id LEFT JOIN users u ON d.verified_by = u.user_id WHERE d.verified = FALSE ORDER BY d.uploaded_at DESC";
    private static final String UPDATE_DOCUMENT_VERIFICATION = "UPDATE documents SET verified = ?, verified_by = ?, verified_at = ? WHERE document_id = ?";
    private static final String DELETE_DOCUMENT = "DELETE FROM documents WHERE document_id = ?";
    
    /**
     * Add a new document
     * @param document Document object
     * @return true if successful, false otherwise
     */
    public boolean addDocument(Document document) {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(INSERT_DOCUMENT);
            
            statement.setInt(1, document.getStudentId());
            statement.setString(2, document.getDocumentType());
            statement.setString(3, document.getFileName());
            statement.setString(4, document.getFilePath());
            statement.setLong(5, document.getFileSize());
            statement.setString(6, document.getMimeType());
            
            return statement.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding document: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            closeResources(connection, statement, null);
        }
    }
    
    /**
     * Get document by ID
     * @param documentId Document ID
     * @return Document object or null
     */
    public Document getDocumentById(int documentId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(SELECT_DOCUMENT_BY_ID);
            statement.setInt(1, documentId);
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return extractDocumentFromResultSet(resultSet);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting document by ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return null;
    }
    
    /**
     * Get documents by student ID
     * @param studentId Student ID
     * @return List of documents
     */
    public List<Document> getDocumentsByStudentId(int studentId) {
        List<Document> documents = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(SELECT_DOCUMENTS_BY_STUDENT);
            statement.setInt(1, studentId);
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                documents.add(extractDocumentFromResultSet(resultSet));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting documents by student ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return documents;
    }
    
    /**
     * Get all documents
     * @return List of documents
     */
    public List<Document> getAllDocuments() {
        List<Document> documents = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(SELECT_ALL_DOCUMENTS);
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                documents.add(extractDocumentFromResultSet(resultSet));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all documents: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return documents;
    }
    
    /**
     * Get unverified documents
     * @return List of unverified documents
     */
    public List<Document> getUnverifiedDocuments() {
        List<Document> documents = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(SELECT_UNVERIFIED_DOCUMENTS);
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                documents.add(extractDocumentFromResultSet(resultSet));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting unverified documents: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(connection, statement, resultSet);
        }
        
        return documents;
    }
    
    /**
     * Update document verification status
     * @param documentId Document ID
     * @param verified Verification status
     * @param verifiedBy User ID who verified
     * @return true if successful, false otherwise
     */
    public boolean updateDocumentVerification(int documentId, boolean verified, int verifiedBy) {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(UPDATE_DOCUMENT_VERIFICATION);
            
            statement.setBoolean(1, verified);
            statement.setInt(2, verifiedBy);
            statement.setTimestamp(3, verified ? new Timestamp(System.currentTimeMillis()) : null);
            statement.setInt(4, documentId);
            
            return statement.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating document verification: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            closeResources(connection, statement, null);
        }
    }
    
    /**
     * Delete document
     * @param documentId Document ID
     * @return true if successful, false otherwise
     */
    public boolean deleteDocument(int documentId) {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DBConnection.getConnection();
            statement = connection.prepareStatement(DELETE_DOCUMENT);
            statement.setInt(1, documentId);
            
            return statement.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting document: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            closeResources(connection, statement, null);
        }
    }
    
    /**
     * Extract document from ResultSet
     * @param resultSet ResultSet containing document data
     * @return Document object
     * @throws SQLException if extraction fails
     */
    private Document extractDocumentFromResultSet(ResultSet resultSet) throws SQLException {
        Document document = new Document();
        document.setDocumentId(resultSet.getInt("document_id"));
        document.setStudentId(resultSet.getInt("student_id"));
        document.setDocumentType(resultSet.getString("document_type"));
        document.setFileName(resultSet.getString("file_name"));
        document.setFilePath(resultSet.getString("file_path"));
        document.setFileSize(resultSet.getLong("file_size"));
        document.setMimeType(resultSet.getString("mime_type"));
        document.setUploadedAt(resultSet.getTimestamp("uploaded_at"));
        document.setVerified(resultSet.getBoolean("verified"));
        
        int verifiedBy = resultSet.getInt("verified_by");
        document.setVerifiedBy(resultSet.wasNull() ? null : verifiedBy);
        document.setVerifiedAt(resultSet.getTimestamp("verified_at"));
        
        // Additional fields
        String firstName = resultSet.getString("first_name");
        String lastName = resultSet.getString("last_name");
        document.setStudentName(firstName + " " + lastName);
        document.setVerifiedByName(resultSet.getString("verified_by_name"));
        
        return document;
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