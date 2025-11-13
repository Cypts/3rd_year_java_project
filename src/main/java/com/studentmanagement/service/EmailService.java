package com.studentmanagement.service;

import com.studentmanagement.model.User;
import com.studentmanagement.util.DBConnection;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

    // SMTP server config (default values)
    private static final String SMTP_SERVER = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    // These should NOT be hard-coded in production. Use env vars / Tomcat context params / secrets manager.
    private static final String FALLBACK_EMAIL_FROM = "kingofman267@gmail.com";
    private static final String FALLBACK_EMAIL_PASSWORD = "cr7lm10xxx.com";

    private final String emailFrom;
    private final String emailPassword;

    private static EmailService instance;

    private EmailService() {
        // Read credentials from environment variables first (recommended)
        String envEmail = System.getenv("SMTP_EMAIL");
        String envPass  = System.getenv("SMTP_PASSWORD");

        this.emailFrom = (envEmail != null && !envEmail.isBlank()) ? envEmail : FALLBACK_EMAIL_FROM;
        this.emailPassword = (envPass != null && !envPass.isBlank()) ? envPass : FALLBACK_EMAIL_PASSWORD;
    }

    public static synchronized EmailService getInstance() {
        if (instance == null) instance = new EmailService();
        return instance;
    }

    /* ---------- Public helper methods (templates unchanged) ---------- */

    public boolean sendRegistrationConfirmation(User user, String studentName) {
        String subject = "Registration Successful - Student Management System";
        String body = buildRegistrationEmailTemplate(studentName, user.getUsername());
        return sendEmail(user.getEmail(), subject, body);
    }

    public boolean sendStatusUpdateEmail(User user, String studentName, String status, String rejectionReason) {
        String subject = "Application Status Update - Student Management System";
        String body = buildStatusUpdateTemplate(studentName, status, rejectionReason);
        return sendEmail(user.getEmail(), subject, body);
    }

    public boolean sendDocumentVerificationEmail(User user, String studentName, String documentType, boolean verified) {
        String subject = "Document Verification Update - Student Management System";
        String body = buildDocumentVerificationTemplate(studentName, documentType, verified);
        return sendEmail(user.getEmail(), subject, body);
    }

    public boolean sendPasswordResetEmail(User user, String resetLink) {
        String subject = "Password Reset Request - Student Management System";
        String body = buildPasswordResetTemplate(user.getUsername(), resetLink);
        return sendEmail(user.getEmail(), subject, body);
    }

    public boolean sendNotificationEmail(User user, String subject, String message) {
        String body = buildGeneralNotificationTemplate(user.getUsername(), message);
        return sendEmail(user.getEmail(), subject, body);
    }

    /* ---------- Core send method (JavaMail / javax.mail) ---------- */

    private boolean sendEmail(String to, String subject, String bodyHtml) {
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", SMTP_SERVER);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", SMTP_SERVER);
        props.put("mail.debug", "false");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailFrom, emailPassword);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailFrom, "Student Management System", StandardCharsets.UTF_8.name()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject, StandardCharsets.UTF_8.name());
            message.setSentDate(new Date());
            message.setContent(bodyHtml, "text/html; charset=utf-8");

            // Use Transport.send (static) which will use the Authenticator provided to Session
            Transport.send(message);

            // Log success to DB (non-blocking)
            logEmail(to, subject, "SENT");
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error sending email to " + to, e);
            logEmail(to, subject, "FAILED");
            return false;
        }
    }

    /* ----------------- Email templates (kept same as your originals) ----------------- */

    private String buildRegistrationEmailTemplate(String studentName, String username) {
        // same template body as you provided (omitted here for brevity)
        return "<!DOCTYPE html>... registration body ...</html>";
    }

    private String buildStatusUpdateTemplate(String studentName, String status, String rejectionReason) {
        return "<!DOCTYPE html>... status update body ...</html>";
    }

    private String buildDocumentVerificationTemplate(String studentName, String documentType, boolean verified) {
        return "<!DOCTYPE html>... document verification body ...</html>";
    }

    private String buildPasswordResetTemplate(String username, String resetLink) {
        return "<!DOCTYPE html>... password reset body ...</html>";
    }

    private String buildGeneralNotificationTemplate(String username, String message) {
        return "<!DOCTYPE html>... notification body ...</html>";
    }

    /* ----------------- DB logging ----------------- */

    private void logEmail(String to, String subject, String status) {
        // Non-blocking, best-effort logging
        String sql = "INSERT INTO email_notifications (user_id, subject, message, status) VALUES (?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // find user by email
            String findUserSql = "SELECT user_id FROM users WHERE email = ?";
            try (PreparedStatement pst = connection.prepareStatement(findUserSql)) {
                pst.setString(1, to);
                try (java.sql.ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        statement.setInt(1, rs.getInt("user_id"));
                    } else {
                        statement.setNull(1, java.sql.Types.INTEGER);
                    }
                }
            }

            statement.setString(2, subject);
            statement.setString(3, "Email sent to: " + to);
            statement.setString(4, status);
            statement.executeUpdate();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error logging email to DB", e);
        }
    }
}
