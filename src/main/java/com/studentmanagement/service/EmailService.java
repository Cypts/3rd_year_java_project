package com.studentmanagement.service;

import com.studentmanagement.model.User;
import com.sun.mail.smtp.SMTPTransport;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

public class EmailService {
    
    private static final String SMTP_SERVER = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_FROM = "your-email@gmail.com";
    private static final String EMAIL_PASSWORD = "your-app-password";
    
    private static EmailService instance;
    
    private EmailService() {}
    
    public static synchronized EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }
    
    /**
     * Send registration confirmation email
     * @param user User object
     * @param studentName Student name
     * @return true if successful, false otherwise
     */
    public boolean sendRegistrationConfirmation(User user, String studentName) {
        String subject = "Registration Successful - Student Management System";
        String body = buildRegistrationEmailTemplate(studentName, user.getUsername());
        
        return sendEmail(user.getEmail(), subject, body);
    }
    
    /**
     * Send application status update email
     * @param user User object
     * @param studentName Student name
     * @param status New status
     * @param rejectionReason Rejection reason (if applicable)
     * @return true if successful, false otherwise
     */
    public boolean sendStatusUpdateEmail(User user, String studentName, String status, String rejectionReason) {
        String subject = "Application Status Update - Student Management System";
        String body = buildStatusUpdateTemplate(studentName, status, rejectionReason);
        
        return sendEmail(user.getEmail(), subject, body);
    }
    
    /**
     * Send document verification email
     * @param user User object
     * @param studentName Student name
     * @param documentType Document type
     * @param verified Verification status
     * @return true if successful, false otherwise
     */
    public boolean sendDocumentVerificationEmail(User user, String studentName, String documentType, boolean verified) {
        String subject = "Document Verification Update - Student Management System";
        String body = buildDocumentVerificationTemplate(studentName, documentType, verified);
        
        return sendEmail(user.getEmail(), subject, body);
    }
    
    /**
     * Send password reset email
     * @param user User object
     * @param resetLink Password reset link
     * @return true if successful, false otherwise
     */
    public boolean sendPasswordResetEmail(User user, String resetLink) {
        String subject = "Password Reset Request - Student Management System";
        String body = buildPasswordResetTemplate(user.getUsername(), resetLink);
        
        return sendEmail(user.getEmail(), subject, body);
    }
    
    /**
     * Send general notification email
     * @param user User object
     * @param subject Email subject
     * @param message Email message
     * @return true if successful, false otherwise
     */
    public boolean sendNotificationEmail(User user, String subject, String message) {
        String body = buildGeneralNotificationTemplate(user.getUsername(), message);
        
        return sendEmail(user.getEmail(), subject, body);
    }
    
    /**
     * Send email using SMTP
     * @param to Recipient email
     * @param subject Email subject
     * @param body Email body
     * @return true if successful, false otherwise
     */
    private boolean sendEmail(String to, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_SERVER);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", SMTP_SERVER);
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
            }
        });
        
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM, "Student Management System"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(body, "text/html; charset=utf-8");
            message.setSentDate(new Date());
            
            SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
            transport.connect(SMTP_SERVER, EMAIL_FROM, EMAIL_PASSWORD);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            
            // Log successful email
            logEmail(to, subject, "SENT");
            return true;
            
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
            
            // Log failed email
            logEmail(to, subject, "FAILED");
            return false;
        }
    }
    
    /**
     * Build registration confirmation email template
     * @param studentName Student name
     * @param username Username
     * @return HTML email body
     */
    private String buildRegistrationEmailTemplate(String studentName, String username) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }"
                + ".container { max-width: 600px; margin: 0 auto; padding: 20px; }"
                + ".header { background-color: #667eea; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }"
                + ".content { background-color: #f9f9f9; padding: 20px; border-radius: 0 0 10px 10px; }"
                + ".footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='container'>"
                + "<div class='header'>"
                + "<h2>Registration Successful!</h2>"
                + "</div>"
                + "<div class='content'>"
                + "<p>Dear " + studentName + ",</p>"
                + "<p>Welcome to our Student Management System! Your registration has been completed successfully.</p>"
                + "<p><strong>Username:</strong> " + username + "</p>"
                + "<p>Please complete your profile and upload the required documents to submit your application.</p>"
                + "<p>If you have any questions, please don't hesitate to contact us.</p>"
                + "<p>Best regards,<br>Student Management System Team</p>"
                + "</div>"
                + "<div class='footer'>"
                + "<p>This is an automated email. Please do not reply to this email.</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }
    
    /**
     * Build status update email template
     * @param studentName Student name
     * @param status New status
     * @param rejectionReason Rejection reason (if applicable)
     * @return HTML email body
     */
    private String buildStatusUpdateTemplate(String studentName, String status, String rejectionReason) {
        String statusMessage = "";
        String statusColor = "";
        
        switch (status) {
            case "PENDING":
                statusMessage = "Your application is now under review. We will notify you once the verification is complete.";
                statusColor = "#ffc107";
                break;
            case "APPROVED":
                statusMessage = "Congratulations! Your application has been approved.";
                statusColor = "#28a745";
                break;
            case "REJECTED":
                statusMessage = "Your application has been rejected.";
                if (rejectionReason != null && !rejectionReason.trim().isEmpty()) {
                    statusMessage += "<br><strong>Reason:</strong> " + rejectionReason;
                }
                statusColor = "#dc3545";
                break;
        }
        
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }"
                + ".container { max-width: 600px; margin: 0 auto; padding: 20px; }"
                + ".header { background-color: " + statusColor + "; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }"
                + ".content { background-color: #f9f9f9; padding: 20px; border-radius: 0 0 10px 10px; }"
                + ".footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='container'>"
                + "<div class='header'>"
                + "<h2>Application Status Update</h2>"
                + "</div>"
                + "<div class='content'>"
                + "<p>Dear " + studentName + ",</p>"
                + "<p>We would like to inform you that your application status has been updated.</p>"
                + "<p><strong>New Status:</strong> " + status + "</p>"
                + "<p>" + statusMessage + "</p>"
                + "<p>If you have any questions, please don't hesitate to contact us.</p>"
                + "<p>Best regards,<br>Student Management System Team</p>"
                + "</div>"
                + "<div class='footer'>"
                + "<p>This is an automated email. Please do not reply to this email.</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }
    
    /**
     * Build document verification email template
     * @param studentName Student name
     * @param documentType Document type
     * @param verified Verification status
     * @return HTML email body
     */
    private String buildDocumentVerificationTemplate(String studentName, String documentType, boolean verified) {
        String status = verified ? "verified" : "rejected";
        String statusColor = verified ? "#28a745" : "#dc3545";
        String message = verified ? 
            "Your " + documentType + " has been successfully verified." :
            "Your " + documentType + " could not be verified. Please upload a valid document.";
        
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }"
                + ".container { max-width: 600px; margin: 0 auto; padding: 20px; }"
                + ".header { background-color: " + statusColor + "; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }"
                + ".content { background-color: #f9f9f9; padding: 20px; border-radius: 0 0 10px 10px; }"
                + ".footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='container'>"
                + "<div class='header'>"
                + "<h2>Document Verification Update</h2>"
                + "</div>"
                + "<div class='content'>"
                + "<p>Dear " + studentName + ",</p>"
                + "<p>We would like to inform you that your document has been " + status + ".</p>"
                + "<p><strong>Document Type:</strong> " + documentType + "</p>"
                + "<p>" + message + "</p>"
                + "<p>Best regards,<br>Student Management System Team</p>"
                + "</div>"
                + "<div class='footer'>"
                + "<p>This is an automated email. Please do not reply to this email.</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }
    
    /**
     * Build password reset email template
     * @param username Username
     * @param resetLink Password reset link
     * @return HTML email body
     */
    private String buildPasswordResetTemplate(String username, String resetLink) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }"
                + ".container { max-width: 600px; margin: 0 auto; padding: 20px; }"
                + ".header { background-color: #667eea; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }"
                + ".content { background-color: #f9f9f9; padding: 20px; border-radius: 0 0 10px 10px; }"
                + ".reset-button { display: inline-block; background-color: #667eea; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; margin: 10px 0; }"
                + ".footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='container'>"
                + "<div class='header'>"
                + "<h2>Password Reset Request</h2>"
                + "</div>"
                + "<div class='content'>"
                + "<p>Dear " + username + ",</p>"
                + "<p>We received a request to reset your password. Click the button below to reset your password:</p>"
                + "<p style='text-align: center;'><a href='" + resetLink + "' class='reset-button'>Reset Password</a></p>"
                + "<p>If you didn't request this, please ignore this email. The link will expire in 24 hours.</p>"
                + "<p>Best regards,<br>Student Management System Team</p>"
                + "</div>"
                + "<div class='footer'>"
                + "<p>This is an automated email. Please do not reply to this email.</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }
    
    /**
     * Build general notification email template
     * @param username Username
     * @param message Notification message
     * @return HTML email body
     */
    private String buildGeneralNotificationTemplate(String username, String message) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }"
                + ".container { max-width: 600px; margin: 0 auto; padding: 20px; }"
                + ".header { background-color: #667eea; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }"
                + ".content { background-color: #f9f9f9; padding: 20px; border-radius: 0 0 10px 10px; }"
                + ".footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='container'>"
                + "<div class='header'>"
                + "<h2>Notification</h2>"
                + "</div>"
                + "<div class='content'>"
                + "<p>Dear " + username + ",</p>"
                + "<p>" + message + "</p>"
                + "<p>Best regards,<br>Student Management System Team</p>"
                + "</div>"
                + "<div class='footer'>"
                + "<p>This is an automated email. Please do not reply to this email.</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }
    
    /**
     * Log email activity
     * @param to Recipient email
     * @param subject Email subject
     * @param status Email status (SENT/FAILED)
     */
    private void logEmail(String to, String subject, String status) {
        try {
            String sql = "INSERT INTO email_notifications (user_id, subject, message, status) VALUES (?, ?, ?, ?)";
            
            try (java.sql.Connection connection = com.studentmanagement.util.DBConnection.getConnection();
                 java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
                
                // Find user by email
                String findUserSql = "SELECT user_id FROM users WHERE email = ?";
                try (java.sql.PreparedStatement findStatement = connection.prepareStatement(findUserSql)) {
                    findStatement.setString(1, to);
                    java.sql.ResultSet resultSet = findStatement.executeQuery();
                    
                    if (resultSet.next()) {
                        statement.setInt(1, resultSet.getInt("user_id"));
                    } else {
                        statement.setNull(1, java.sql.Types.INTEGER);
                    }
                }
                
                statement.setString(2, subject);
                statement.setString(3, "Email sent to: " + to);
                statement.setString(4, status);
                
                statement.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("Error logging email: " + e.getMessage());
        }
    }
}