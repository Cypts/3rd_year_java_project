<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Dashboard - Student Management System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <style>
        body {
            background-color: #f8f9fa;
        }
        .sidebar {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            color: white;
        }
        .sidebar .nav-link {
            color: rgba(255, 255, 255, 0.8);
            border-radius: 8px;
            margin: 5px 0;
            padding: 12px 15px;
        }
        .sidebar .nav-link:hover, .sidebar .nav-link.active {
            color: white;
            background: rgba(255, 255, 255, 0.2);
        }
        .main-content {
            padding: 30px;
        }
        .status-card {
            border-radius: 15px;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
            border: none;
        }
        .status-pending {
            background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
        }
        .status-approved {
            background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
        }
        .status-rejected {
            background: linear-gradient(135deg, #ff6b6b 0%, #feca57 100%);
        }
        .status-incomplete {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }
        .profile-card {
            background: white;
            border-radius: 15px;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
            border: none;
        }
    </style>
</head>
<body>
    <div class="container-fluid">
        <div class="row">
            <!-- Sidebar -->
            <div class="col-md-3 col-lg-2 px-0">
                <div class="sidebar p-3">
                    <h4 class="text-center mb-4">
                        <i class="fas fa-graduation-cap"></i> Student Portal
                    </h4>
                    
                    <nav class="nav flex-column">
                        <a class="nav-link active" href="${pageContext.request.contextPath}/student/dashboard">
                            <i class="fas fa-tachometer-alt"></i> Dashboard
                        </a>
                        <a class="nav-link" href="${pageContext.request.contextPath}/student/profile">
                            <i class="fas fa-user"></i> My Profile
                        </a>
                        <a class="nav-link" href="${pageContext.request.contextPath}/student/documents">
                            <i class="fas fa-file-alt"></i> Documents
                        </a>
                        <a class="nav-link" href="${pageContext.request.contextPath}/student/status">
                            <i class="fas fa-info-circle"></i> Application Status
                        </a>
                        <a class="nav-link" href="${pageContext.request.contextPath}/student/notifications">
                            <i class="fas fa-bell"></i> Notifications
                        </a>
                        <hr class="text-white">
                        <a class="nav-link" href="${pageContext.request.contextPath}/logout">
                            <i class="fas fa-sign-out-alt"></i> Logout
                        </a>
                    </nav>
                </div>
            </div>
            
            <!-- Main Content -->
            <div class="col-md-9 col-lg-10">
                <div class="main-content">
                    <h2><i class="fas fa-tachometer-alt"></i> Student Dashboard</h2>
                    <p class="text-muted">Welcome, ${student.fullName}!</p>
                    
                    <c:if test="${not empty success}">
                        <div class="alert alert-success alert-dismissible fade show" role="alert">
                            <i class="fas fa-check-circle"></i> ${success}
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    </c:if>
                    
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <i class="fas fa-exclamation-triangle"></i> ${error}
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    </c:if>
                    
                    <!-- Status Card -->
                    <div class="row mb-4">
                        <div class="col-md-12">
                            <div class="card status-card 
                                <c:choose>
                                    <c:when test="${student.status == 'PENDING'}">status-pending</c:when>
                                    <c:when test="${student.status == 'APPROVED'}">status-approved</c:when>
                                    <c:when test="${student.status == 'REJECTED'}">status-rejected</c:when>
                                    <c:otherwise>status-incomplete</c:otherwise>
                                </c:choose>
                                text-white">
                                <div class="card-body text-center">
                                    <h3 class="card-title">
                                        <i class="fas fa-info-circle"></i> Application Status
                                    </h3>
                                    <h2 class="mb-3">
                                        <c:choose>
                                            <c:when test="${student.status == 'PENDING'}">
                                                <i class="fas fa-clock"></i> PENDING
                                            </c:when>
                                            <c:when test="${student.status == 'APPROVED'}">
                                                <i class="fas fa-check-circle"></i> APPROVED
                                            </c:when>
                                            <c:when test="${student.status == 'REJECTED'}">
                                                <i class="fas fa-times-circle"></i> REJECTED
                                            </c:when>
                                            <c:otherwise>
                                                <i class="fas fa-edit"></i> INCOMPLETE
                                            </c:otherwise>
                                        </c:choose>
                                    </h2>
                                    <p class="card-text">
                                        <c:choose>
                                            <c:when test="${student.status == 'PENDING'}">
                                                Your application is under review. You will be notified once the verification is complete.
                                            </c:when>
                                            <c:when test="${student.status == 'APPROVED'}">
                                                Congratulations! Your application has been approved.
                                                <c:if test="${not empty student.approvedDate}">
                                                    <br>Approved on: ${student.approvedDate}
                                                </c:if>
                                            </c:when>
                                            <c:when test="${student.status == 'REJECTED'}">
                                                Your application has been rejected.
                                                <c:if test="${not empty student.rejectionReason}">
                                                    <br>Reason: ${student.rejectionReason}
                                                </c:if>
                                            </c:when>
                                            <c:otherwise>
                                                Please complete your profile and upload required documents to submit your application.
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Profile Information -->
                    <div class="row">
                        <div class="col-md-6">
                            <div class="card profile-card mb-4">
                                <div class="card-header bg-primary text-white">
                                    <h5 class="mb-0"><i class="fas fa-user"></i> Personal Information</h5>
                                </div>
                                <div class="card-body">
                                    <table class="table table-borderless">
                                        <tr>
                                            <td><strong>Name:</strong></td>
                                            <td>${student.fullName}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>Email:</strong></td>
                                            <td>${student.email}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>Phone:</strong></td>
                                            <td>${student.phone}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>Date of Birth:</strong></td>
                                            <td>${student.dateOfBirth}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>Gender:</strong></td>
                                            <td>${student.gender}</td>
                                        </tr>
                                    </table>
                                </div>
                            </div>
                        </div>
                        
                        <div class="col-md-6">
                            <div class="card profile-card mb-4">
                                <div class="card-header bg-success text-white">
                                    <h5 class="mb-0"><i class="fas fa-book"></i> Academic Information</h5>
                                </div>
                                <div class="card-body">
                                    <table class="table table-borderless">
                                        <tr>
                                            <td><strong>Course:</strong></td>
                                            <td>${student.courseName}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>Enrollment Year:</strong></td>
                                            <td>${student.enrollmentYear}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>Registration Date:</strong></td>
                                            <td>${student.registrationDate}</td>
                                        </tr>
                                        <tr>
                                            <td><strong>Address:</strong></td>
                                            <td>${student.address}, ${student.city}, ${student.state} - ${student.zipCode}</td>
                                        </tr>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Quick Actions -->
                    <div class="row">
                        <div class="col-md-12">
                            <div class="card profile-card">
                                <div class="card-header bg-info text-white">
                                    <h5 class="mb-0"><i class="fas fa-bolt"></i> Quick Actions</h5>
                                </div>
                                <div class="card-body">
                                    <div class="row">
                                        <div class="col-md-3 mb-2">
                                            <a href="${pageContext.request.contextPath}/student/profile" class="btn btn-outline-primary w-100">
                                                <i class="fas fa-edit"></i> Edit Profile
                                            </a>
                                        </div>
                                        <div class="col-md-3 mb-2">
                                            <a href="${pageContext.request.contextPath}/student/documents" class="btn btn-outline-success w-100">
                                                <i class="fas fa-upload"></i> Upload Documents
                                            </a>
                                        </div>
                                        <div class="col-md-3 mb-2">
                                            <a href="${pageContext.request.contextPath}/student/status" class="btn btn-outline-info w-100">
                                                <i class="fas fa-info-circle"></i> View Status
                                            </a>
                                        </div>
                                        <div class="col-md-3 mb-2">
                                            <a href="${pageContext.request.contextPath}/student/notifications" class="btn btn-outline-warning w-100">
                                                <i class="fas fa-bell"></i> Notifications
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Auto-dismiss alerts after 5 seconds
        setTimeout(function() {
            const alerts = document.querySelectorAll('.alert');
            alerts.forEach(function(alert) {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            });
        }, 5000);
    </script>
</body>
</html>