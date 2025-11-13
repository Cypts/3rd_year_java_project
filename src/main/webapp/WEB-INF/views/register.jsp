<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Management System - Registration</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <style>
        /* === KEEPING YOUR ORIGINAL STYLING EXACTLY === */
        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px 0;
        }
        .registration-container {
            background: white;
            border-radius: 15px;
            box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
            overflow: hidden;
            max-width: 800px;
            margin: 0 auto;
        }
        .registration-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            text-align: center;
        }
        .registration-form {
            padding: 30px;
        }
        .form-control, .form-select {
            border-radius: 10px;
            border: 1px solid #e0e0e0;
            padding: 12px 15px;
        }
        .form-control:focus, .form-select:focus {
            border-color: #667eea;
            box-shadow: 0 0 0 0.2rem rgba(102, 126, 234, 0.25);
        }
        .btn-register {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border: none;
            border-radius: 10px;
            padding: 12px 30px;
            font-weight: 600;
        }
        .btn-register:hover {
            background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
        }
        .alert {
            border-radius: 10px;
        }
        .section-title {
            color: #667eea;
            font-weight: 600;
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 2px solid #667eea;
        }
        .input-group-text {
            background-color: #f8f9fa;
            border-radius: 10px 0 0 10px;
        }
        .form-control {
            border-radius: 0 10px 10px 0;
        }

        /* small inline error (keeps visual look) */
        .field-error { color: #b00020; font-size: 0.9rem; margin-top: 6px; }
    </style>
</head>
<body>
    <div class="registration-container">
        <div class="registration-header">
            <h3><i class="fas fa-graduation-cap"></i> Student Registration</h3>
            <p class="mb-0">Create your account to get started</p>
        </div>
        
        <div class="registration-form">
            <!-- Server-side summary error -->
            <c:if test="${not empty generalError || not empty error}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    <i class="fas fa-exclamation-triangle"></i>
                    ${generalError != null ? generalError : error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>

            <!-- Server-side success flash -->
            <c:if test="${not empty sessionScope.success}">
                <div class="alert alert-success alert-dismissible fade show" role="alert" id="serverSuccessMsg">
                    <i class="fas fa-check-circle"></i> ${sessionScope.success}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
                <!-- remove flash so it doesn't persist on refresh -->
                <c:remove var="success" scope="session"/>
            </c:if>
            
            <form action="${pageContext.request.contextPath}/register" method="post" id="registrationForm" novalidate>
                <!-- Account Information -->
                <h5 class="section-title"><i class="fas fa-user-lock"></i> Account Information</h5>
                
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="username" class="form-label">Username *</label>
                        <div class="input-group">
                            <span class="input-group-text"><i class="fas fa-user"></i></span>
                            <input type="text" class="form-control" id="username" name="username" 
                                   value="${fn:escapeXml(username)}" required pattern="[a-zA-Z0-9_]{3,20}"
                                   title="Username must be 3-20 characters long and contain only letters, numbers, and underscores">
                        </div>
                        <div id="usernameError" class="field-error" aria-live="polite">${usernameError}</div>
                    </div>
                    
                    <div class="col-md-6 mb-3">
                        <label for="email" class="form-label">Email Address *</label>
                        <div class="input-group">
                            <span class="input-group-text"><i class="fas fa-envelope"></i></span>
                            <input type="email" class="form-control" id="email" name="email" 
                                   value="${fn:escapeXml(email)}" required>
                        </div>
                        <div id="emailError" class="field-error" aria-live="polite">${emailError}</div>
                    </div>
                </div>
                
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="password" class="form-label">Password *</label>
                        <div class="input-group">
                            <span class="input-group-text"><i class="fas fa-lock"></i></span>
                            <input type="password" class="form-control" id="password" name="password" required
                                   pattern="(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}"
                                   title="Password must be at least 8 characters long and contain uppercase, lowercase, digit, and special character">
                            <button class="btn btn-outline-secondary" type="button" id="togglePassword" aria-label="Toggle password visibility">
                                <i class="fas fa-eye"></i>
                            </button>
                        </div>
                        <div id="passwordError" class="field-error" aria-live="polite">${passwordError}</div>
                    </div>
                    
                    <div class="col-md-6 mb-3">
                        <label for="confirmPassword" class="form-label">Confirm Password *</label>
                        <div class="input-group">
                            <span class="input-group-text"><i class="fas fa-lock"></i></span>
                            <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required>
                            <button class="btn btn-outline-secondary" type="button" id="toggleConfirmPassword" aria-label="Toggle confirm password visibility">
                                <i class="fas fa-eye"></i>
                            </button>
                        </div>
                        <div id="confirmPasswordError" class="field-error" aria-live="polite">${confirmPasswordError}</div>
                    </div>
                </div>
                
                <!-- Personal Information -->
                <h5 class="section-title"><i class="fas fa-user"></i> Personal Information</h5>
                
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="firstName" class="form-label">First Name *</label>
                        <input type="text" class="form-control" id="firstName" name="firstName" 
                               value="${fn:escapeXml(firstName)}" required pattern="[a-zA-Z\\s]{2,50}"
                               title="First name must contain only letters and spaces (2-50 characters)">
                        <div id="firstNameError" class="field-error" aria-live="polite">${firstNameError}</div>
                    </div>
                    
                    <div class="col-md-6 mb-3">
                        <label for="lastName" class="form-label">Last Name *</label>
                        <input type="text" class="form-control" id="lastName" name="lastName" 
                               value="${fn:escapeXml(lastName)}" required pattern="[a-zA-Z\\s]{2,50}"
                               title="Last name must contain only letters and spaces (2-50 characters)">
                        <div id="lastNameError" class="field-error" aria-live="polite">${lastNameError}</div>
                    </div>
                </div>
                
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="dateOfBirth" class="form-label">Date of Birth *</label>
                        <input type="date" class="form-control" id="dateOfBirth" name="dateOfBirth" 
                               value="${dateOfBirth}" required max="<%= java.time.LocalDate.now().minusYears(16) %>">
                        <div id="dateOfBirthError" class="field-error" aria-live="polite">${dateOfBirthError}</div>
                    </div>
                    
                    <div class="col-md-6 mb-3">
                        <label for="gender" class="form-label">Gender *</label>
                        <select class="form-select" id="gender" name="gender" required>
                            <option value="">Select Gender</option>
                            <option value="MALE" ${gender == 'MALE' ? 'selected' : ''}>Male</option>
                            <option value="FEMALE" ${gender == 'FEMALE' ? 'selected' : ''}>Female</option>
                            <option value="OTHER" ${gender == 'OTHER' ? 'selected' : ''}>Other</option>
                        </select>
                        <div id="genderError" class="field-error" aria-live="polite">${genderError}</div>
                    </div>
                </div>
                
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="phone" class="form-label">Phone Number *</label>
                        <div class="input-group">
                            <span class="input-group-text"><i class="fas fa-phone"></i></span>
                            <input type="tel" class="form-control" id="phone" name="phone" 
                                   value="${fn:escapeXml(phone)}" required pattern="[0-9]{10}"
                                   title="Please enter a valid 10-digit phone number">
                        </div>
                        <div id="phoneError" class="field-error" aria-live="polite">${phoneError}</div>
                    </div>
                    
                    <div class="col-md-6 mb-3">
                        <label for="zipCode" class="form-label">ZIP Code *</label>
                        <input type="text" class="form-control" id="zipCode" name="zipCode" 
                               value="${fn:escapeXml(zipCode)}" required pattern="[0-9]{6}"
                               title="Please enter a valid 6-digit ZIP code">
                        <div id="zipCodeError" class="field-error" aria-live="polite">${zipCodeError}</div>
                    </div>
                </div>
                
                <div class="mb-3">
                    <label for="address" class="form-label">Address *</label>
                    <textarea class="form-control" id="address" name="address" rows="2" required>${fn:escapeXml(address)}</textarea>
                    <div id="addressError" class="field-error" aria-live="polite">${addressError}</div>
                </div>
                
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="city" class="form-label">City *</label>
                        <input type="text" class="form-control" id="city" name="city" 
                               value="${fn:escapeXml(city)}" required>
                        <div id="cityError" class="field-error" aria-live="polite">${cityError}</div>
                    </div>
                    
                    <div class="col-md-6 mb-3">
                        <label for="state" class="form-label">State *</label>
                        <input type="text" class="form-control" id="state" name="state" 
                               value="${fn:escapeXml(state)}" required>
                        <div id="stateError" class="field-error" aria-live="polite">${stateError}</div>
                    </div>
                </div>
                
                <!-- Academic Information -->
                <h5 class="section-title"><i class="fas fa-book"></i> Academic Information</h5>
                
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="courseId" class="form-label">Course *</label>
                        <select class="form-select" id="courseId" name="courseId" required>
                            <option value="">Select Course</option>
                            <option value="1" ${courseId == '1' ? 'selected' : ''}>B.Tech Computer Science</option>
                            <option value="2" ${courseId == '2' ? 'selected' : ''}>B.Tech Mechanical Engineering</option>
                            <option value="3" ${courseId == '3' ? 'selected' : ''}>B.Tech Electronics & Communication</option>
                            <option value="4" ${courseId == '4' ? 'selected' : ''}>MBA</option>
                            <option value="5" ${courseId == '5' ? 'selected' : ''}>MCA</option>
                        </select>
                        <div id="courseIdError" class="field-error" aria-live="polite">${courseIdError}</div>
                    </div>
                    
                    <div class="col-md-6 mb-3">
                        <label for="enrollmentYear" class="form-label">Enrollment Year *</label>
                        <select class="form-select" id="enrollmentYear" name="enrollmentYear" required>
                            <option value="">Select Year</option>
                            <%-- keep your previous dynamic year generation (scriptlet) for compatibility --%>
                            <c:forEach var="year" begin="<%= java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) - 5 %>" 
                                       end="<%= java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) + 2 %>">
                                <option value="${year}" ${enrollmentYear == year ? 'selected' : ''}>${year}</option>
                            </c:forEach>
                        </select>
                        <div id="enrollmentYearError" class="field-error" aria-live="polite">${enrollmentYearError}</div>
                    </div>
                </div>
                
                <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                    <button type="reset" class="btn btn-secondary me-md-2">
                        <i class="fas fa-undo"></i> Reset
                    </button>
                    <button type="submit" class="btn btn-primary btn-register">
                        <i class="fas fa-user-plus"></i> Register
                    </button>
                </div>
            </form>
            
            <hr class="my-4">
            
            <div class="text-center">
                <p class="mb-0">Already have an account?</p>
                <a href="${pageContext.request.contextPath}/login" class="btn btn-outline-primary">
                    <i class="fas fa-sign-in-alt"></i> Login Here
                </a>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Helper: clear an error text
        function clearError(id) {
            const el = document.getElementById(id);
            if (el) el.textContent = '';
        }
        // Helper: set an error text
        function setError(id, msg) {
            const el = document.getElementById(id);
            if (el) el.textContent = msg;
        }

        // Toggle password visibility (keeps your functionality)
        (function() {
            const togglePassword = document.getElementById('togglePassword');
            const toggleConfirmPassword = document.getElementById('toggleConfirmPassword');

            if (togglePassword) {
                togglePassword.addEventListener('click', function() {
                    const passwordInput = document.getElementById('password');
                    const toggleIcon = this.querySelector('i');

                    if (passwordInput.type === 'password') {
                        passwordInput.type = 'text';
                        toggleIcon.classList.remove('fa-eye');
                        toggleIcon.classList.add('fa-eye-slash');
                    } else {
                        passwordInput.type = 'password';
                        toggleIcon.classList.remove('fa-eye-slash');
                        toggleIcon.classList.add('fa-eye');
                    }
                });
            }

            if (toggleConfirmPassword) {
                toggleConfirmPassword.addEventListener('click', function() {
                    const confirmPasswordInput = document.getElementById('confirmPassword');
                    const toggleIcon = this.querySelector('i');

                    if (confirmPasswordInput.type === 'password') {
                        confirmPasswordInput.type = 'text';
                        toggleIcon.classList.remove('fa-eye');
                        toggleIcon.classList.add('fa-eye-slash');
                    } else {
                        confirmPasswordInput.type = 'password';
                        toggleIcon.classList.remove('fa-eye-slash');
                        toggleIcon.classList.add('fa-eye');
                    }
                });
            }
        })();

        // Client-side validation on submit (fills inline errors)
        document.getElementById('registrationForm').addEventListener('submit', function(e) {
            // Clear previous errors
            ['usernameError','emailError','passwordError','confirmPasswordError','firstNameError','lastNameError',
             'dateOfBirthError','genderError','phoneError','zipCodeError','addressError','cityError','stateError',
             'courseIdError','enrollmentYearError'].forEach(clearError);

            let ok = true;

            const username = document.getElementById('username').value.trim();
            const email = document.getElementById('email').value.trim();
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            const firstName = document.getElementById('firstName').value.trim();
            const lastName = document.getElementById('lastName').value.trim();
            const dobVal = document.getElementById('dateOfBirth').value;
            const gender = document.getElementById('gender').value;
            const phone = document.getElementById('phone').value.trim();
            const zip = document.getElementById('zipCode').value.trim();
            const address = document.getElementById('address').value.trim();
            const city = document.getElementById('city').value.trim();
            const state = document.getElementById('state').value.trim();
            const courseId = document.getElementById('courseId').value;
            const enrollmentYear = document.getElementById('enrollmentYear').value;

            // Basic required checks (mirrors server-side)
            if (!username) { setError('usernameError','Username is required.'); ok = false; }
            if (!email) { setError('emailError','Email is required.'); ok = false; }
            if (!password) { setError('passwordError','Password is required.'); ok = false; }
            if (!confirmPassword) { setError('confirmPasswordError','Please confirm password.'); ok = false; }
            if (!firstName) { setError('firstNameError','First name required.'); ok = false; }
            if (!lastName) { setError('lastNameError','Last name required.'); ok = false; }
            if (!dobVal) { setError('dateOfBirthError','Date of birth required.'); ok = false; }
            if (!gender) { setError('genderError','Please select gender.'); ok = false; }
            if (!phone) { setError('phoneError','Phone required.'); ok = false; }
            if (!zip) { setError('zipCodeError','ZIP code required.'); ok = false; }
            if (!address) { setError('addressError','Address required.'); ok = false; }
            if (!city) { setError('cityError','City required.'); ok = false; }
            if (!state) { setError('stateError','State required.'); ok = false; }
            if (!courseId) { setError('courseIdError','Please select a course.'); ok = false; }
            if (!enrollmentYear) { setError('enrollmentYearError','Please select enrollment year.'); ok = false; }

            // Format checks
            const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (email && !emailRe.test(email)) { setError('emailError','Invalid email format.'); ok = false; }

            const phoneRe = /^\d{10}$/;
            if (phone && !phoneRe.test(phone)) { setError('phoneError','Phone must be 10 digits.'); ok = false; }

            const zipRe = /^\d{6}$/;
            if (zip && !zipRe.test(zip)) { setError('zipCodeError','ZIP must be 6 digits.'); ok = false; }

            if (password && password.length < 8) { setError('passwordError','Password must be at least 8 characters.'); ok = false; }

            if (password && confirmPassword && password !== confirmPassword) {
                setError('confirmPasswordError','Passwords do not match.');
                ok = false;
            }

            // Age check (must be >=16)
            if (dobVal) {
                const dob = new Date(dobVal);
                const today = new Date();
                let age = today.getFullYear() - dob.getFullYear();
                const m = today.getMonth() - dob.getMonth();
                if (m < 0 || (m === 0 && today.getDate() < dob.getDate())) {
                    age--;
                }
                if (isNaN(age) || age < 16) {
                    setError('dateOfBirthError','You must be at least 16 years old to register.');
                    ok = false;
                }
            }

            if (!ok) {
                // show top alert so user notices quickly (bootstrap)
                const topAlert = document.createElement('div');
                topAlert.className = 'alert alert-danger';
                topAlert.textContent = 'Please fix the highlighted errors before submitting the form.';
                const form = document.getElementById('registrationForm');
                form.parentNode.insertBefore(topAlert, form);
                // auto remove after 5s
                setTimeout(() => { topAlert.remove(); }, 5000);
                e.preventDefault();
                return false;
            }

            // else allow submit (server-side validations will still run)
            return true;
        });

        // Auto-dismiss any server alerts after 5 seconds
        setTimeout(function() {
            const alerts = document.querySelectorAll('.alert');
            alerts.forEach(function(alert) {
                try {
                    const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
                    bsAlert.close();
                } catch (err) {
                    // ignore
                }
            });
        }, 5000);
    </script>
</body>
</html>
