<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - Student Management System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <style>
        body {
            background-color: #f8f9fa;
        }
        .sidebar {
            background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
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
        .stats-card {
            border-radius: 15px;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
            border: none;
            transition: transform 0.3s ease;
        }
        .stats-card:hover {
            transform: translateY(-5px);
        }
        .stats-card .card-body {
            padding: 25px;
        }
        .stats-card .display-4 {
            font-weight: 700;
        }
        .stats-icon {
            font-size: 2.5rem;
            opacity: 0.8;
        }
        .chart-container {
            background: white;
            border-radius: 15px;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
            padding: 25px;
            margin-bottom: 25px;
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
                        <i class="fas fa-user-shield"></i> Admin Portal
                    </h4>
                    
                    <nav class="nav flex-column">
                        <a class="nav-link active" href="${pageContext.request.contextPath}/admin/dashboard">
                            <i class="fas fa-tachometer-alt"></i> Dashboard
                        </a>
                        <a class="nav-link" href="${pageContext.request.contextPath}/admin/students">
                            <i class="fas fa-users"></i> Manage Students
                        </a>
                        <a class="nav-link" href="${pageContext.request.contextPath}/admin/pending">
                            <i class="fas fa-clock"></i> Pending Applications
                        </a>
                        <a class="nav-link" href="${pageContext.request.contextPath}/admin/documents">
                            <i class="fas fa-file-alt"></i> Document Verification
                        </a>
                        <a class="nav-link" href="${pageContext.request.contextPath}/admin/courses">
                            <i class="fas fa-book"></i> Course Management
                        </a>
                        <a class="nav-link" href="${pageContext.request.contextPath}/admin/reports">
                            <i class="fas fa-chart-bar"></i> Reports & Analytics
                        </a>
                        <a class="nav-link" href="${pageContext.request.contextPath}/admin/settings">
                            <i class="fas fa-cog"></i> Settings
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
                    <h2><i class="fas fa-tachometer-alt"></i> Admin Dashboard</h2>
                    <p class="text-muted">Welcome to the administration panel</p>
                    
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
                    
                    <!-- Statistics Cards -->
                    <div class="row mb-4">
                        <div class="col-md-3 mb-3">
                            <div class="card stats-card bg-primary text-white">
                                <div class="card-body">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <div>
                                            <h5 class="card-title">Total Students</h5>
                                            <h2 class="display-4">${totalStudents}</h2>
                                        </div>
                                        <div>
                                            <i class="fas fa-users stats-icon"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="col-md-3 mb-3">
                            <div class="card stats-card bg-warning text-white">
                                <div class="card-body">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <div>
                                            <h5 class="card-title">Pending</h5>
                                            <h2 class="display-4">${pendingStudents}</h2>
                                        </div>
                                        <div>
                                            <i class="fas fa-clock stats-icon"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="col-md-3 mb-3">
                            <div class="card stats-card bg-success text-white">
                                <div class="card-body">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <div>
                                            <h5 class="card-title">Approved</h5>
                                            <h2 class="display-4">${approvedStudents}</h2>
                                        </div>
                                        <div>
                                            <i class="fas fa-check-circle stats-icon"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="col-md-3 mb-3">
                            <div class="card stats-card bg-danger text-white">
                                <div class="card-body">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <div>
                                            <h5 class="card-title">Rejected</h5>
                                            <h2 class="display-4">${rejectedStudents}</h2>
                                        </div>
                                        <div>
                                            <i class="fas fa-times-circle stats-icon"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-4 mb-3">
                            <div class="card stats-card bg-info text-white">
                                <div class="card-body">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <div>
                                            <h5 class="card-title">Incomplete</h5>
                                            <h2 class="display-4">${incompleteStudents}</h2>
                                        </div>
                                        <div>
                                            <i class="fas fa-edit stats-icon"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="col-md-4 mb-3">
                            <div class="card stats-card bg-secondary text-white">
                                <div class="card-body">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <div>
                                            <h5 class="card-title">Total Admins</h5>
                                            <h2 class="display-4">${totalAdmins}</h2>
                                        </div>
                                        <div>
                                            <i class="fas fa-user-shield stats-icon"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="col-md-4 mb-3">
                            <div class="card stats-card bg-dark text-white">
                                <div class="card-body">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <div>
                                            <h5 class="card-title">Courses</h5>
                                            <h2 class="display-4">${totalCourses}</h2>
                                        </div>
                                        <div>
                                            <i class="fas fa-book stats-icon"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Quick Actions -->
                    <div class="row">
                        <div class="col-md-12">
                            <div class="chart-container">
                                <h5><i class="fas fa-bolt"></i> Quick Actions</h5>
                                <div class="row mt-3">
                                    <div class="col-md-3 mb-2">
                                        <a href="${pageContext.request.contextPath}/admin/students" class="btn btn-primary w-100">
                                            <i class="fas fa-users"></i> View All Students
                                        </a>
                                    </div>
                                    <div class="col-md-3 mb-2">
                                        <a href="${pageContext.request.contextPath}/admin/pending" class="btn btn-warning w-100">
                                            <i class="fas fa-clock"></i> Review Applications (${pendingStudents})
                                        </a>
                                    </div>
                                    <div class="col-md-3 mb-2">
                                        <a href="${pageContext.request.contextPath}/admin/documents" class="btn btn-info w-100">
                                            <i class="fas fa-file-alt"></i> Verify Documents
                                        </a>
                                    </div>
                                    <div class="col-md-3 mb-2">
                                        <a href="${pageContext.request.contextPath}/admin/reports" class="btn btn-success w-100">
                                            <i class="fas fa-chart-bar"></i> Generate Reports
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Recent Activity -->
                    <div class="row">
                        <div class="col-md-12">
                            <div class="chart-container">
                                <h5><i class="fas fa-history"></i> Recent Activity</h5>
                                <div class="text-center text-muted py-4">
                                    <i class="fas fa-info-circle fa-2x mb-2"></i>
                                    <p>Recent activity feed will be displayed here.</p>
                                    <small>Activity logging system to be implemented.</small>
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