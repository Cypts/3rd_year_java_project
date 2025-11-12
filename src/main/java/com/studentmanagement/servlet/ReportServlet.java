package com.studentmanagement.servlet;

import com.studentmanagement.dao.StudentDAO;
import com.studentmanagement.dao.CourseDAO;
import com.studentmanagement.model.Student;
import com.studentmanagement.model.Course;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@WebServlet("/admin/reports")
public class ReportServlet extends HttpServlet {
    
    private StudentDAO studentDAO;
    private CourseDAO courseDAO;
    private SimpleDateFormat dateFormat;
    
    @Override
    public void init() throws ServletException {
        studentDAO = new StudentDAO();
        courseDAO = new CourseDAO();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
            return;
        }
        
        String action = request.getParameter("action");
        String format = request.getParameter("format");
        String filter = request.getParameter("filter");
        String value = request.getParameter("value");
        
        try {
            List<Student> students = null;
            
            // Apply filters
            if ("course".equals(filter) && value != null && !value.trim().isEmpty()) {
                int courseId = Integer.parseInt(value);
                students = studentDAO.getStudentsByCourse(courseId);
            } else if ("status".equals(filter) && value != null && !value.trim().isEmpty()) {
                students = studentDAO.getStudentsByStatus(value);
            } else if ("year".equals(filter) && value != null && !value.trim().isEmpty()) {
                int year = Integer.parseInt(value);
                // Custom query for year filter would be implemented here
                students = studentDAO.getAllStudents(); // Placeholder
            } else {
                students = studentDAO.getAllStudents();
            }
            
            // Generate report based on format
            if ("csv".equalsIgnoreCase(format)) {
                generateCSVReport(response, students, filter, value);
            } else if ("excel".equalsIgnoreCase(format)) {
                generateExcelReport(response, students, filter, value);
            } else {
                // Default to HTML view
                request.setAttribute("students", students);
                request.setAttribute("courses", courseDAO.getAllCourses());
                request.getRequestDispatcher("/WEB-INF/views/admin/reports.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            System.err.println("Error generating report: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "Error generating report: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    /**
     * Generate CSV report
     * @param response HTTP response
     * @param students List of students
     * @param filter Applied filter
     * @param value Filter value
     * @throws IOException if writing fails
     */
    private void generateCSVReport(HttpServletResponse response, List<Student> students, String filter, String value) 
            throws IOException {
        
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", 
            "attachment; filename=&quot;students_report_" + new Date().getTime() + ".csv&quot;");
        
        try (PrintWriter writer = response.getWriter()) {
            // Write CSV header
            writer.println("Student ID,Name,Email,Course,Enrollment Year,Status,Registration Date,Approved Date,Phone,Address");
            
            // Write data rows
            for (Student student : students) {
                writer.printf("%d,&quot;%s&quot;,&quot;%s&quot;,&quot;%s&quot;,%d,&quot;%s&quot;,&quot;%s&quot;,&quot;%s&quot;,&quot;%s&quot;,&quot;%s&quot;%n",
                    student.getStudentId(),
                    escapeCSV(student.getFullName()),
                    escapeCSV(student.getEmail()),
                    escapeCSV(student.getCourseName()),
                    student.getEnrollmentYear(),
                    student.getStatus(),
                    formatDate(student.getRegistrationDate()),
                    formatDate(student.getApprovedDate()),
                    escapeCSV(student.getPhone()),
                    escapeCSV(student.getAddress() + ", " + student.getCity() + ", " + student.getState() + " - " + student.getZipCode())
                );
            }
        }
    }
    
    /**
     * Generate Excel report
     * @param response HTTP response
     * @param students List of students
     * @param filter Applied filter
     * @param value Filter value
     * @throws IOException if writing fails
     */
    private void generateExcelReport(HttpServletResponse response, List<Student> students, String filter, String value) 
            throws IOException {
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", 
            "attachment; filename=&quot;students_report_" + new Date().getTime() + ".xlsx&quot;");
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Students Report");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Student ID", "Name", "Email", "Course", "Enrollment Year", 
                "Status", "Registration Date", "Approved Date", "Phone", "Address"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            int rowNum = 1;
            for (Student student : students) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(student.getStudentId());
                row.createCell(1).setCellValue(student.getFullName());
                row.createCell(2).setCellValue(student.getEmail());
                row.createCell(3).setCellValue(student.getCourseName());
                row.createCell(4).setCellValue(student.getEnrollmentYear());
                row.createCell(5).setCellValue(student.getStatus());
                row.createCell(6).setCellValue(formatDate(student.getRegistrationDate()));
                row.createCell(7).setCellValue(formatDate(student.getApprovedDate()));
                row.createCell(8).setCellValue(student.getPhone());
                row.createCell(9).setCellValue(student.getAddress() + ", " + student.getCity() + ", " + student.getState() + " - " + student.getZipCode());
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Set minimum width to ensure readability
                if (sheet.getColumnWidth(i) < 4000) {
                    sheet.setColumnWidth(i, 4000);
                }
            }
            
            // Write to response
            workbook.write(response.getOutputStream());
        }
    }
    
    /**
     * Escape CSV special characters
     * @param value String to escape
     * @return Escaped string
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        // Escape quotes by doubling them
        return value.replace("&quot;", "&quot;&quot;");
    }
    
    /**
     * Format date for display
     * @param date Date to format
     * @return Formatted date string
     */
    private String formatDate(java.util.Date date) {
        if (date == null) {
            return "";
        }
        return dateFormat.format(date);
    }
}