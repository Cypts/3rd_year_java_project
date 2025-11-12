-- Student Management System Database Schema

CREATE DATABASE IF NOT EXISTS student_management;
USE student_management;

-- Users table (base table for authentication)
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('STUDENT', 'ADMIN') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Courses table
CREATE TABLE IF NOT EXISTS courses (
    course_id INT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(20) UNIQUE NOT NULL,
    course_name VARCHAR(100) NOT NULL,
    description TEXT,
    duration_years INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Students table
CREATE TABLE IF NOT EXISTS students (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    city VARCHAR(50),
    state VARCHAR(50),
    zip_code VARCHAR(10),
    country VARCHAR(50) DEFAULT 'India',
    course_id INT NOT NULL,
    enrollment_year YEAR NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'INCOMPLETE') DEFAULT 'INCOMPLETE',
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_by INT,
    approved_date TIMESTAMP NULL,
    rejection_reason TEXT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(course_id),
    FOREIGN KEY (approved_by) REFERENCES users(user_id)
);

-- Documents table
CREATE TABLE IF NOT EXISTS documents (
    document_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    document_type ENUM('PHOTO', 'MARKSHEET_10', 'MARKSHEET_12', 'AADHAR', 'TRANSFER_CERTIFICATE', 'OTHER') NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified BOOLEAN DEFAULT FALSE,
    verified_by INT,
    verified_at TIMESTAMP NULL,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (verified_by) REFERENCES users(user_id)
);

-- Email notifications table
CREATE TABLE IF NOT EXISTS email_notifications (
    notification_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('SENT', 'FAILED', 'PENDING') DEFAULT 'PENDING',
    error_message TEXT,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Student activity log
CREATE TABLE IF NOT EXISTS activity_log (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    action VARCHAR(100) NOT NULL,
    description TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Insert default admin user (password: admin123)
INSERT INTO users (username, email, password, role) 
VALUES ('admin', 'admin@university.edu', '$2a$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'ADMIN');

-- Insert sample courses
INSERT INTO courses (course_code, course_name, description, duration_years) VALUES
('BTECH-CS', 'B.Tech Computer Science', 'Bachelor of Technology in Computer Science and Engineering', 4),
('BTECH-ME', 'B.Tech Mechanical Engineering', 'Bachelor of Technology in Mechanical Engineering', 4),
('BTECH-EC', 'B.Tech Electronics & Communication', 'Bachelor of Technology in Electronics and Communication Engineering', 4),
('MBA', 'Master of Business Administration', 'Two-year full-time MBA program', 2),
('MCA', 'Master of Computer Applications', 'Three-year postgraduate program in Computer Applications', 3);

-- Create indexes for better performance
CREATE INDEX idx_students_status ON students(status);
CREATE INDEX idx_students_course ON students(course_id);
CREATE INDEX idx_documents_student ON documents(student_id);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_activity_log_user ON activity_log(user_id);
CREATE INDEX idx_activity_log_timestamp ON activity_log(timestamp);