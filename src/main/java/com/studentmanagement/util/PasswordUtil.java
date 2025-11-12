package com.studentmanagement.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    
    /**
     * Hash a password using BCrypt
     * @param plainTextPassword Plain text password
     * @return Hashed password
     */
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }
    
    /**
     * Verify a password against its hash
     * @param plainTextPassword Plain text password
     * @param hashedPassword Hashed password
     * @return true if passwords match, false otherwise
     */
    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
    
    /**
     * Validate password strength
     * @param password Password to validate
     * @return true if password meets requirements, false otherwise
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        // Check for at least one uppercase, one lowercase, one digit, and one special character
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }
        
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
    
    /**
     * Generate a random password
     * @param length Password length
     * @return Random password
     */
    public static String generateRandomPassword(int length) {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        
        String allChars = upper + lower + digits + special;
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one character from each category
        password.append(upper.charAt((int)(Math.random() * upper.length())));
        password.append(lower.charAt((int)(Math.random() * lower.length())));
        password.append(digits.charAt((int)(Math.random() * digits.length())));
        password.append(special.charAt((int)(Math.random() * special.length())));
        
        // Fill the rest with random characters
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt((int)(Math.random() * allChars.length())));
        }
        
        // Shuffle the characters
        char[] passwordArray = password.toString().toCharArray();
        for (int i = 0; i < passwordArray.length; i++) {
            int j = (int)(Math.random() * passwordArray.length);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }
}