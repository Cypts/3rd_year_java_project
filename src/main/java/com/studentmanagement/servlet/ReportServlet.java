package com.studentmanagement.servlet;

import com.studentmanagement.dao.CourseDAO;
import com.studentmanagement.dao.StudentDAO;
import com.studentmanagement.model.Student;
import com.studentmanagement.model.User;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Admin reports servlet — generates HTML view, CSV or Excel exports.
 */
@WebServlet("/admin/reports")
public class ReportServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(ReportServlet.class.getName());

    private StudentDAO studentDAO;
    private CourseDAO courseDAO;
    // thread-safe formatter using java.time
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    @Override
    public void init() throws ServletException {
        studentDAO = new StudentDAO();
        courseDAO = new CourseDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Authentication
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() == null || !"ADMIN".equalsIgnoreCase(user.getRole().trim())) {
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
            return;
        }

        String format = safeTrim(request.getParameter("format"));
        String filter = safeTrim(request.getParameter("filter"));
        String value = safeTrim(request.getParameter("value"));

        try {
            List<Student> students;

            // Apply filters (course/status/year)
            if ("course".equalsIgnoreCase(filter) && value != null && !value.isEmpty()) {
                try {
                    int courseId = Integer.parseInt(value);
                    students = studentDAO.getStudentsByCourse(courseId);
                } catch (NumberFormatException nfe) {
                    students = studentDAO.getAllStudents();
                }
            } else if ("status".equalsIgnoreCase(filter) && value != null && !value.isEmpty()) {
                students = studentDAO.getStudentsByStatus(value);
            } else if ("year".equalsIgnoreCase(filter) && value != null && !value.isEmpty()) {
                try {
                    int year = Integer.parseInt(value);
                    try {
                        students = studentDAO.getStudentsByYear(year);
                    } catch (NoSuchMethodError | UnsupportedOperationException e) {
                        students = studentDAO.getAllStudents();
                    }
                } catch (NumberFormatException nfe) {
                    students = studentDAO.getAllStudents();
                }
            } else {
                students = studentDAO.getAllStudents();
            }

            if (students == null) {
                students = java.util.Collections.emptyList();
            }

            if ("csv".equalsIgnoreCase(format)) {
                generateCSVReport(response, students);
            } else if ("excel".equalsIgnoreCase(format)) {
                generateExcelReport(response, students);
            } else {
                request.setAttribute("students", students);
                request.setAttribute("courses", courseDAO.getAllCourses());
                request.getRequestDispatcher("/WEB-INF/views/admin/reports.jsp").forward(request, response);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error generating report", e);
            request.setAttribute("error", "Error generating report.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    private void generateCSVReport(HttpServletResponse response, List<Student> students) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"students_report_" + new Date().getTime() + ".csv\"");

        // Write BOM for Excel to recognize UTF-8
        try (PrintWriter writer = response.getWriter()) {
            writer.write('\uFEFF');

            // Header
            writer.println("Student ID,Name,Email,Course,Enrollment Year,Status,Registration Date,Approved Date,Phone,Address");

            for (Student student : students) {
                String address = (student.getAddress() == null) ? "" :
                        (student.getAddress() + ", " + nullSafe(student.getCity()) + ", " + nullSafe(student.getState()) + " - " + nullSafe(student.getZipCode()));
                writer.printf("%d,\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        student.getStudentId(),
                        escapeCSV(nullSafe(student.getFullName())),
                        escapeCSV(nullSafe(student.getEmail())),
                        escapeCSV(nullSafe(student.getCourseName())),
                        student.getEnrollmentYear(),
                        nullSafe(student.getStatus()),
                        formatDate(student.getRegistrationDate()),
                        formatDate(student.getApprovedDate()),
                        escapeCSV(nullSafe(student.getPhone())),
                        escapeCSV(address));
            }
        }
    }

    private void generateExcelReport(HttpServletResponse response, List<Student> students) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"students_report_" + new Date().getTime() + ".xlsx\"");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Students Report");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

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

            int rowNum = 1;
            for (Student student : students) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(student.getStudentId());
                row.createCell(1).setCellValue(nullSafe(student.getFullName()));
                row.createCell(2).setCellValue(nullSafe(student.getEmail()));
                row.createCell(3).setCellValue(nullSafe(student.getCourseName()));
                row.createCell(4).setCellValue(student.getEnrollmentYear());
                row.createCell(5).setCellValue(nullSafe(student.getStatus()));
                row.createCell(6).setCellValue(formatDate(student.getRegistrationDate()));
                row.createCell(7).setCellValue(formatDate(student.getApprovedDate()));
                row.createCell(8).setCellValue(nullSafe(student.getPhone()));
                row.createCell(9).setCellValue(nullSafe(student.getAddress()) + ", " + nullSafe(student.getCity()) + ", " + nullSafe(student.getState()) + " - " + nullSafe(student.getZipCode()));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                if (sheet.getColumnWidth(i) < 4000) sheet.setColumnWidth(i, 4000);
            }

            workbook.write(response.getOutputStream());
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        try {
            return DATE_FORMATTER.format(date.toInstant());
        } catch (Exception e) {
            return "";
        }
    }

    private static String safeTrim(String s) {
        return (s == null) ? null : s.trim();
    }

    private static String nullSafe(String s) {
        return (s == null) ? "" : s;
    }
}
