package com.studentmanagement.servlet;

import com.studentmanagement.dao.DocumentDAO;
import com.studentmanagement.dao.StudentDAO;
import com.studentmanagement.model.Document;
import com.studentmanagement.model.Student;
import com.studentmanagement.model.User;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.*;

@WebServlet("/student/upload-document")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5L * 1024L * 1024L)   // 5 MB
public class DocumentUploadServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DocumentUploadServlet.class.getName());
    private static final String[] ALLOWED_EXTENSIONS = {"pdf", "jpg", "jpeg", "png"};

    private DocumentDAO documentDAO;
    private StudentDAO studentDAO;
    private String uploadPath;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        documentDAO = new DocumentDAO();
        studentDAO  = new StudentDAO();

        String configured = getServletContext().getInitParameter("uploadPath");
        uploadPath = (configured != null && !configured.trim().isEmpty())
                ? configured
                : getServletContext().getRealPath("") + File.separator + "uploads" + File.separator + "documents";

        File dir = new File(uploadPath);
        if (!dir.exists() && !dir.mkdirs())
            throw new ServletException("Unable to create upload directory: " + uploadPath);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (user == null || !"STUDENT".equalsIgnoreCase(user.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            Student student = studentDAO.getStudentByUserId(user.getUserId());
            if (student == null) {
                resp.sendRedirect(req.getContextPath() + "/student/complete-profile");
                return;
            }
            req.setAttribute("student", student);
            req.setAttribute("documents",
                    documentDAO.getDocumentsByStudentId(student.getStudentId()));
            req.getRequestDispatcher("/WEB-INF/views/student/upload-documents.jsp")
               .forward(req, resp);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading upload page", e);
            req.setAttribute("error", "Error loading document upload page.");
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (user == null || !"STUDENT".equalsIgnoreCase(user.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Student student = studentDAO.getStudentByUserId(user.getUserId());
        if (student == null) {
            resp.sendRedirect(req.getContextPath() + "/student/complete-profile");
            return;
        }

        String documentType = req.getParameter("documentType");
        Part filePart       = req.getPart("file");   // name="file" in your form

        if (filePart == null || filePart.getSize() == 0 || documentType == null) {
            req.setAttribute("error", "Please select a file and document type.");
            doGet(req, resp);
            return;
        }

        String originalName = Path.of(filePart.getSubmittedFileName()).getFileName().toString();
        String ext = getFileExtension(originalName);
        if (!isAllowedExtension(ext)) {
            req.setAttribute("error", "Invalid file type. Allowed: PDF, JPG, PNG.");
            doGet(req, resp);
            return;
        }

        String safeDocType = sanitize(documentType);
        String uniqueName  = UUID.randomUUID() + "_" + student.getStudentId() + "_" +
                             safeDocType + "." + ext;

        File storeFile = new File(uploadPath, uniqueName);
        try (InputStream in = filePart.getInputStream()) {
            Files.copy(in, storeFile.toPath());
        }

        Document doc = new Document();
        doc.setStudentId(student.getStudentId());
        doc.setDocumentType(safeDocType);
        doc.setFileName(originalName);
        doc.setFilePath("uploads/documents/" + uniqueName);
        doc.setFileSize(filePart.getSize());
        doc.setMimeType(filePart.getContentType());
        doc.setVerified(false);

        boolean success = documentDAO.addDocument(doc);
        if (success) {
            req.setAttribute("success", "Document uploaded successfully!");
        } else {
            storeFile.delete();
            req.setAttribute("error", "Failed to save document information.");
        }

        doGet(req, resp);
    }

    private static String getFileExtension(String name) {
        int i = name.lastIndexOf('.');
        return (i == -1) ? "" : name.substring(i + 1).toLowerCase();
    }

    private static boolean isAllowedExtension(String ext) {
        return Arrays.stream(ALLOWED_EXTENSIONS).anyMatch(e -> e.equalsIgnoreCase(ext));
    }

    private static String sanitize(String token) {
        return (token == null) ? "unknown"
                : token.replaceAll("[^A-Za-z0-9_\\-]", "_");
    }
}
