package com.studentmanagement.model;

import java.sql.Timestamp;

public class Course {
    private int courseId;
    private String courseCode;
    private String courseName;
    private String description;
    private int durationYears;
    private boolean isActive;
    private Timestamp createdAt;
    
    public Course() {}
    
    public Course(int courseId, String courseCode, String courseName, String description, int durationYears) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.description = description;
        this.durationYears = durationYears;
        this.isActive = true;
    }
    
    // Getters and Setters
    public int getCourseId() {
        return courseId;
    }
    
    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }
    
    public String getCourseCode() {
        return courseCode;
    }
    
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }
    
    public String getCourseName() {
        return courseName;
    }
    
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getDurationYears() {
        return durationYears;
    }
    
    public void setDurationYears(int durationYears) {
        this.durationYears = durationYears;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return courseCode + " - " + courseName;
    }
}