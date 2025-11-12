<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error - Student Management System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <style>
        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .error-container {
            background: white;
            border-radius: 15px;
            box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
            padding: 40px;
            text-align: center;
            max-width: 500px;
        }
        .error-icon {
            font-size: 4rem;
            color: #dc3545;
            margin-bottom: 20px;
        }
        .btn-home {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border: none;
            border-radius: 10px;
            padding: 12px 30px;
            font-weight: 600;
        }
    </style>
</head>
<body>
    <div class="error-container">
        <i class="fas fa-exclamation-triangle error-icon"></i>
        <h1 class="display-4 text-danger">Oops!</h1>
        <h3 class="mb-3">Something went wrong</h3>
        
        <c:if test="${not empty error}">
            <div class="alert alert-danger mb-4">
                <strong>Error:</strong> ${error}
            </div>
        </c:if>
        
        <c:if test="${not empty message}">
            <div class="alert alert-info mb-4">
                ${message}
            </div>
        </c:if>
        
        <p class="text-muted mb-4">
            We apologize for the inconvenience. Please try again or contact support if the problem persists.
        </p>
        
        <div class="d-flex justify-content-center gap-2">
            <a href="javascript:history.back()" class="btn btn-outline-secondary">
                <i class="fas fa-arrow-left"></i> Go Back
            </a>
            <a href="${pageContext.request.contextPath}/" class="btn btn-primary btn-home">
                <i class="fas fa-home"></i> Go Home
            </a>
        </div>
        
        <hr class="my-4">
        
        <small class="text-muted">
            <strong>Technical Details:</strong><br>
            Error Code: ${pageContext.errorData.statusCode}<br>
            Request URI: ${pageContext.errorData.requestURI}<br>
            <c:if test="${not empty pageContext.errorData.servletName}">
                Servlet: ${pageContext.errorData.servletName}<br>
            </c:if>
            <c:if test="${not empty pageContext.errorData.throwable}">
                Exception: ${pageContext.errorData.throwable.message}<br>
            </c:if>
            Timestamp: <%= new java.util.Date() %>
        </small>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>