package com.studentmanagement.model;

import java.sql.Timestamp;

public class Document {
    private int documentId;
    private int studentId;
    private String documentType;
    private String fileName;
    private String filePath;
    private long fileSize;
    private String mimeType;
    private Timestamp uploadedAt;
    private boolean verified;
    private Integer verifiedBy;
    private Timestamp verifiedAt;
    
    // Additional fields for display
    private String studentName;
    private String verifiedByName;
    
    public Document() {}
    
    // Getters and Setters
    public int getDocumentId() {
        return documentId;
    }
    
    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }
    
    public int getStudentId() {
        return studentId;
    }
    
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }
    
    public String getDocumentType() {
        return documentType;
    }
    
    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public Timestamp getUploadedAt() {
        return uploadedAt;
    }
    
    public void setUploadedAt(Timestamp uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
    
    public boolean isVerified() {
        return verified;
    }
    
    public void setVerified(boolean verified) {
        this.verified = verified;
    }
    
    public Integer getVerifiedBy() {
        return verifiedBy;
    }
    
    public void setVerifiedBy(Integer verifiedBy) {
        this.verifiedBy = verifiedBy;
    }
    
    public Timestamp getVerifiedAt() {
        return verifiedAt;
    }
    
    public void setVerifiedAt(Timestamp verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
    
    public String getStudentName() {
        return studentName;
    }
    
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    
    public String getVerifiedByName() {
        return verifiedByName;
    }
    
    public void setVerifiedByName(String verifiedByName) {
        this.verifiedByName = verifiedByName;
    }
    
    public String getDocumentTypeDisplayName() {
        switch (documentType) {
            case "PHOTO": return "Photograph";
            case "MARKSHEET_10": return "10th Marksheet";
            case "MARKSHEET_12": return "12th Marksheet";
            case "AADHAR": return "Aadhar Card";
            case "TRANSFER_CERTIFICATE": return "Transfer Certificate";
            case "OTHER": return "Other Document";
            default: return documentType;
        }
    }
    
    @Override
    public String toString() {
        return "Document{" +
                "documentId=" + documentId +
                ", documentType='" + documentType + '\'' +
                ", fileName='" + fileName + '\'' +
                ", verified=" + verified +
                '}';
    }
}