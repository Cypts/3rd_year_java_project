package com.studentmanagement.util;

import java.util.regex.Pattern;

public class ValidationUtil {
    
    // Regular expression patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_]{3,20}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[0-9]{10}$"
    );
    
    private static final Pattern ZIP_CODE_PATTERN = Pattern.compile(
        "^[0-9]{6}$"
    );
    
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[a-zA-Z\\s]{2,50}$"
    );
    
    /**
     * Validate email address
     * @param email Email address
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validate username
     * @param username Username
     * @return true if valid, false otherwise
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }
    
    /**
     * Validate phone number (Indian format)
     * @param phone Phone number
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String cleanPhone = phone.trim().replaceAll("[\\s-]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }
    
    /**
     * Validate ZIP code (Indian PIN code format)
     * @param zipCode ZIP code
     * @return true if valid, false otherwise
     */
    public static boolean isValidZipCode(String zipCode) {
        if (zipCode == null || zipCode.trim().isEmpty()) {
            return false;
        }
        String cleanZip = zipCode.trim().replaceAll("[\\s-]", "");
        return ZIP_CODE_PATTERN.matcher(cleanZip).matches();
    }
    
    /**
     * Validate name (first name, last name)
     * @param name Name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return NAME_PATTERN.matcher(name.trim()).matches();
    }
    
    /**
     * Validate password strength
     * @param password Password to validate
     * @return true if strong, false otherwise
     */
    public static boolean isValidPassword(String password) {
        return PasswordUtil.isPasswordStrong(password);
    }
    
    /**
     * Validate date of birth (student should be at least 16 years old)
     * @param year Birth year
     * @param month Birth month
     * @param day Birth day
     * @return true if valid, false otherwise
     */
    public static boolean isValidDateOfBirth(int year, int month, int day) {
        try {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setLenient(false);
            cal.set(year, month - 1, day);
            
            // Check if date is valid
            cal.getTime();
            
            // Check if student is at least 16 years old
            java.util.Calendar today = java.util.Calendar.getInstance();
            int age = today.get(java.util.Calendar.YEAR) - year;
            
            if (today.get(java.util.Calendar.MONTH) < month - 1) {
                age--;
            } else if (today.get(java.util.Calendar.MONTH) == month - 1 && 
                      today.get(java.util.Calendar.DAY_OF_MONTH) < day) {
                age--;
            }
            
            return age >= 16;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Validate enrollment year
     * @param year Enrollment year
     * @return true if valid, false otherwise
     */
    public static boolean isValidEnrollmentYear(int year) {
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        return year >= currentYear - 5 && year <= currentYear + 2;
    }
    
    /**
     * Validate file upload
     * @param fileName File name
     * @param fileSize File size in bytes
     * @param maxSize Maximum allowed size in bytes
     * @param allowedExtensions Array of allowed file extensions
     * @return true if valid, false otherwise
     */
    public static boolean isValidFileUpload(String fileName, long fileSize, long maxSize, String[] allowedExtensions) {
        if (fileName == null || fileName.trim().isEmpty() || fileSize <= 0) {
            return false;
        }
        
        if (fileSize > maxSize) {
            return false;
        }
        
        String fileExtension = getFileExtension(fileName).toLowerCase();
        for (String allowedExt : allowedExtensions) {
            if (fileExtension.equals(allowedExt.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get file extension
     * @param fileName File name
     * @return File extension
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
    
    /**
     * Sanitize input to prevent XSS
     * @param input Input string
     * @return Sanitized string
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove HTML tags and script content
        String sanitized = input.replaceAll("<script>.*?</script>", "")
                               .replaceAll("<.*?>", "")
                               .replaceAll("&", "&amp;")
                               .replaceAll("<", "&lt;")
                               .replaceAll(">", "&gt;")
                               .replaceAll("&quot;", "&quot;")
                               .replaceAll("'", "&#x27;")
                               .replaceAll("/", "&#x2F;");
        
        return sanitized.trim();
    }
    
    /**
     * Validate that required fields are not empty
     * @param fields Array of field values
     * @return true if all fields are non-empty, false otherwise
     */
    public static boolean areRequiredFieldsFilled(String... fields) {
        for (String field : fields) {
            if (field == null || field.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}