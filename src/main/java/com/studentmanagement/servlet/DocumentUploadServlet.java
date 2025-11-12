package com.studentmanagement.servlet;

import com.studentmanagement.dao.DocumentDAO;
import com.studentmanagement.dao.StudentDAO;
import com.studentmanagement.model.User;
import com.studentmanagement.model.Student;
import com.studentmanagement.model.Document;
import com.studentmanagement.util.ValidationUtil;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

@WebServlet("/student/upload-document")
public class DocumentUploadServlet extends HttpServlet {
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {"pdf", "jpg", "jpeg", "png"};
    
    private DocumentDAO documentDAO;
    private StudentDAO studentDAO;
    private String uploadPath;
    
    @Override
    public void init() throws ServletException {
        documentDAO = new DocumentDAO();
        studentDAO = new StudentDAO();
        uploadPath = getServletContext().getRealPath("") + File.separator + "uploads" + File.separator + "documents";
        
        // Create upload directory if it doesn't exist
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
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
                response.sendRedirect(request.getContextPath() + "/student/complete-profile");
                return;
            }
            
            // Get existing documents
            List<Document> documents = documentDAO.getDocumentsByStudentId(student.getStudentId());
            
            request.setAttribute("student", student);
            request.setAttribute("documents", documents);
            request.getRequestDispatcher("/WEB-INF/views/student/upload-documents.jsp").forward(request, response);
            
        } catch (Exception e) {
            System.err.println("Error loading document upload page: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "Error loading document upload page.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
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
                response.sendRedirect(request.getContextPath() + "/student/complete-profile");
                return;
            }
            
            // Check if upload is multipart
            if (!ServletFileUpload.isMultipartContent(request)) {
                request.setAttribute("error", "Invalid file upload request.");
                doGet(request, response);
                return;
            }
            
            // Configure upload settings
            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setSizeThreshold(1024 * 1024); // 1MB threshold
            factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
            
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setFileSizeMax(MAX_FILE_SIZE);
            upload.setSizeMax(MAX_FILE_SIZE * 5); // Max 5 files
            
            // Parse request
            List<FileItem> formItems = upload.parseRequest(request);
            String documentType = null;
            FileItem fileItem = null;
            
            for (FileItem item : formItems) {
                if (item.isFormField()) {
                    if ("documentType".equals(item.getFieldName())) {
                        documentType = item.getString();
                    }
                } else {
                    fileItem = item;
                }
            }
            
            // Validate form data
            if (documentType == null || documentType.trim().isEmpty()) {
                request.setAttribute("error", "Please select document type.");
                doGet(request, response);
                return;
            }
            
            if (fileItem == null || fileItem.getSize() == 0) {
                request.setAttribute("error", "Please select a file to upload.");
                doGet(request, response);
                return;
            }
            
            // Validate file
            String fileName = fileItem.getName();
            long fileSize = fileItem.getSize();
            
            if (!ValidationUtil.isValidFileUpload(fileName, fileSize, MAX_FILE_SIZE, ALLOWED_EXTENSIONS)) {
                request.setAttribute("error", "Invalid file. Please upload PDF, JPG, or PNG files only (max 5MB).");
                doGet(request, response);
                return;
            }
            
            // Generate unique file name
            String fileExtension = ValidationUtil.getFileExtension(fileName);
            String uniqueFileName = System.currentTimeMillis() + "_" + student.getStudentId() + "_" + documentType + "." + fileExtension;
            String filePath = uploadPath + File.separator + uniqueFileName;
            
            // Save file
            File storeFile = new File(filePath);
            fileItem.write(storeFile);
            
            // Create document record
            Document document = new Document();
            document.setStudentId(student.getStudentId());
            document.setDocumentType(documentType);
            document.setFileName(fileName);
            document.setFilePath("uploads/documents/" + uniqueFileName);
            document.setFileSize(fileSize);
            document.setMimeType(getServletContext().getMimeType(fileName));
            document.setVerified(false);
            
            boolean success = documentDAO.addDocument(document);
            
            if (success) {
                request.setAttribute("success", "Document uploaded successfully!");
                
                // Check if all required documents are uploaded
                checkAndUpdateStudentStatus(student);
            } else {
                // Delete uploaded file if database operation failed
                storeFile.delete();
                request.setAttribute("error", "Failed to save document information.");
            }
            
        } catch (Exception e) {
            System.err.println("Document upload error: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "Error uploading document: " + e.getMessage());
        }
        
        // Redirect back to upload page
        doGet(request, response);
    }
    
    /**
     * Check if all required documents are uploaded and update student status
     * @param student Student object
     */
    private void checkAndUpdateStudentStatus(Student student) {
        try {
            List<String> requiredDocuments = new ArrayList<>();
            requiredDocuments.add("PHOTO");
            requiredDocuments.add("MARKSHEET_10");
            requiredDocuments.add("MARKSHEET_12");
            requiredDocuments.add("AADHAR");
            
            List<Document> uploadedDocuments = documentDAO.getDocumentsByStudentId(student.getStudentId());
            
            // Check if all required documents are uploaded
            boolean allDocumentsUploaded = true;
            for (String requiredDoc : requiredDocuments) {
                boolean found = false;
                for (Document uploadedDoc : uploadedDocuments) {
                    if (requiredDoc.equals(uploadedDoc.getDocumentType())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    allDocumentsUploaded = false;
                    break;
                }
            }
            
            // Update student status if all documents are uploaded
            if (allDocumentsUploaded && "INCOMPLETE".equals(student.getStatus())) {
                studentDAO.updateStudentStatus(student.getStudentId(), "PENDING", null, null);
            }
            
        } catch (Exception e) {
            System.err.println("Error checking document status: " + e.getMessage());
        }
    }
}