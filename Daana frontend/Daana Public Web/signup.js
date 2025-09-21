/**
 * Signup JavaScript for Daana.lk
 * Handles signup form validation and API integration
 */

// DOM Content Loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeSignup();
});

// Initialize Signup
function initializeSignup() {
    setupSignupForm();
    setupFormValidation();
    console.log('✅ Signup page initialized');
}

// Setup Signup Form
function setupSignupForm() {
    const signupForm = document.getElementById('signupForm');
    if (signupForm) {
        signupForm.addEventListener('submit', handleSignup);
    }
}

// Setup Form Validation
function setupFormValidation() {
    // Real-time validation
    const inputs = document.querySelectorAll('#signupForm input[required]');
    inputs.forEach(input => {
        input.addEventListener('blur', () => validateField(input));
        input.addEventListener('input', () => clearFieldError(input));
    });
    
    // Password confirmation validation
    const confirmPassword = document.getElementById('confirmPassword');
    if (confirmPassword) {
        confirmPassword.addEventListener('input', validatePasswordMatch);
    }
}

// Handle Signup Form Submission
async function handleSignup(event) {
    event.preventDefault();
    
    const form = event.target;
    const formData = new FormData(form);
    
    // Validate all fields
    if (!validateForm(form)) {
        return;
    }
    
    // Prepare signup data
    const signupData = {
        firstName: formData.get('firstName').trim(),
        lastName: formData.get('lastName').trim(),
        email: formData.get('email').trim().toLowerCase(),
        password: formData.get('password')
    };
    
    try {
        showSignupLoading(true);
        clearAllErrors();
        
        console.log('Submitting signup data:', { ...signupData, password: '[HIDDEN]' });
        
        const response = await apiService.signup(signupData);
        
        if (response.success) {
            // Store email for verification
            const email = signupData.email;
            localStorage.setItem('pendingVerificationEmail', email);
            
            // Show success message
            showMessage('Account created successfully! Please check your email for verification.', 'success');
            
            // Clear form
            form.reset();
            
            // Redirect to verification page after 3 seconds
            setTimeout(() => {
                window.location.href = `verify-email.html?email=${encodeURIComponent(email)}`;
            }, 3000);
            
        } else {
            showMessage(response.message || 'Signup failed. Please try again.', 'error');
        }
        
    } catch (error) {
        console.error('Signup error:', error);
        
        // Handle API error response format
        if (error.response && error.response.data) {
            const errorData = error.response.data;
            
            // Handle structured error response
            if (errorData.success === false && errorData.message) {
                // Check for specific error types
                if (errorData.message.toLowerCase().includes('email') && 
                    errorData.message.toLowerCase().includes('already registered')) {
                    showFieldError('email', errorData.message);
                } else if (errorData.message.toLowerCase().includes('validation')) {
                    showMessage(errorData.message, 'error');
                } else {
                    showMessage(errorData.message, 'error');
                }
            } else {
                showMessage('Signup failed. Please try again.', 'error');
            }
        } else if (error.message) {
            // Handle other error types
            if (error.message.includes('email')) {
                showFieldError('email', 'This email is already registered. Please use a different email or try logging in.');
            } else if (error.message.includes('validation')) {
                showMessage('Please check your input and try again.', 'error');
            } else if (error.message.includes('network') || error.message.includes('fetch')) {
                showMessage('Network error. Please check your connection and try again.', 'error');
            } else {
                showMessage(error.message, 'error');
            }
        } else {
            showMessage('Signup failed. Please try again later.', 'error');
        }
    } finally {
        showSignupLoading(false);
    }
}

// Validate Form
function validateForm(form) {
    let isValid = true;
    const inputs = form.querySelectorAll('input[required]');
    
    inputs.forEach(input => {
        if (!validateField(input)) {
            isValid = false;
        }
    });
    
    // Validate password match
    if (!validatePasswordMatch()) {
        isValid = false;
    }
    
    // Validate terms agreement
    const agreeTerms = document.getElementById('agreeTerms');
    if (!agreeTerms.checked) {
        showFieldError('agreeTerms', 'You must agree to the Terms of Service and Privacy Policy');
        isValid = false;
    }
    
    return isValid;
}

// Validate Individual Field
function validateField(input) {
    const value = input.value.trim();
    const fieldName = input.name;
    let isValid = true;
    let errorMessage = '';
    
    // Required field validation
    if (!value) {
        errorMessage = `${getFieldLabel(fieldName)} is required`;
        isValid = false;
    } else {
        // Field-specific validation
        switch (fieldName) {
            case 'firstName':
            case 'lastName':
                if (value.length < 2) {
                    errorMessage = `${getFieldLabel(fieldName)} must be at least 2 characters`;
                    isValid = false;
                } else if (!/^[a-zA-Z\s]+$/.test(value)) {
                    errorMessage = `${getFieldLabel(fieldName)} can only contain letters and spaces`;
                    isValid = false;
                }
                break;
                
            case 'email':
                if (!isValidEmail(value)) {
                    errorMessage = 'Please enter a valid email address';
                    isValid = false;
                }
                break;
                
            case 'password':
                if (value.length < 8) {
                    errorMessage = 'Password must be at least 8 characters long';
                    isValid = false;
                } else if (!isStrongPassword(value)) {
                    errorMessage = 'Password must contain at least one letter and one number';
                    isValid = false;
                }
                break;
        }
    }
    
    if (isValid) {
        clearFieldError(input);
    } else {
        showFieldError(fieldName, errorMessage);
    }
    
    return isValid;
}

// Validate Password Match
function validatePasswordMatch() {
    const password = document.getElementById('password');
    const confirmPassword = document.getElementById('confirmPassword');
    
    if (password && confirmPassword) {
        if (confirmPassword.value && password.value !== confirmPassword.value) {
            showFieldError('confirmPassword', 'Passwords do not match');
            return false;
        } else {
            clearFieldError(confirmPassword);
            return true;
        }
    }
    return true;
}

// Utility Functions
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

function isStrongPassword(password) {
    // At least 8 characters, one letter, one number
    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,}$/;
    return passwordRegex.test(password);
}

function getFieldLabel(fieldName) {
    const labels = {
        firstName: 'First Name',
        lastName: 'Last Name',
        email: 'Email',
        password: 'Password',
        confirmPassword: 'Confirm Password'
    };
    return labels[fieldName] || fieldName;
}

// Show Field Error
function showFieldError(fieldName, message) {
    const input = document.getElementById(fieldName);
    const errorElement = document.getElementById(fieldName + 'Error');
    
    if (input) {
        input.classList.add('error');
    }
    
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.style.display = 'block';
    }
}

// Clear Field Error
function clearFieldError(input) {
    const fieldName = input.name || input.id;
    const errorElement = document.getElementById(fieldName + 'Error');
    
    if (input) {
        input.classList.remove('error');
    }
    
    if (errorElement) {
        errorElement.textContent = '';
        errorElement.style.display = 'none';
    }
}

// Clear All Errors
function clearAllErrors() {
    const errorElements = document.querySelectorAll('.error-message');
    const errorInputs = document.querySelectorAll('.error');
    
    errorElements.forEach(element => {
        element.textContent = '';
        element.style.display = 'none';
    });
    
    errorInputs.forEach(input => {
        input.classList.remove('error');
    });
}

// Show Signup Loading State
function showSignupLoading(show) {
    const signupBtn = document.getElementById('signupBtn');
    const btnText = signupBtn.querySelector('.btn-text');
    const btnLoading = signupBtn.querySelector('.btn-loading');
    
    if (show) {
        signupBtn.disabled = true;
        btnText.style.display = 'none';
        btnLoading.style.display = 'inline-flex';
    } else {
        signupBtn.disabled = false;
        btnText.style.display = 'inline';
        btnLoading.style.display = 'none';
    }
}

// Show Message
function showMessage(message, type) {
    // Remove existing messages
    const existingMessages = document.querySelectorAll('.auth-message');
    existingMessages.forEach(msg => msg.remove());
    
    // Create new message
    const messageDiv = document.createElement('div');
    messageDiv.className = `auth-message auth-message-${type}`;
    messageDiv.innerHTML = `
        <div class="message-content">
            <i class="fas ${type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'}"></i>
            <span>${message}</span>
        </div>
    `;
    
    // Insert after form header
    const authHeader = document.querySelector('.auth-header');
    if (authHeader) {
        authHeader.insertAdjacentElement('afterend', messageDiv);
    }
    
    // Auto-remove success messages after 5 seconds
    if (type === 'success') {
        setTimeout(() => {
            messageDiv.remove();
        }, 5000);
    }
}

// Toggle Password Visibility
function togglePassword(fieldId) {
    const passwordInput = document.getElementById(fieldId);
    const toggleIcon = document.getElementById(fieldId + 'ToggleIcon');
    
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        toggleIcon.classList.remove('fa-eye');
        toggleIcon.classList.add('fa-eye-slash');
    } else {
        passwordInput.type = 'password';
        toggleIcon.classList.remove('fa-eye-slash');
        toggleIcon.classList.add('fa-eye');
    }
}

// Test function to demonstrate error handling
function testErrorHandling() {
    console.log('Testing error handling...');
    
    // Simulate the error response format you provided
    const mockError = {
        response: {
            status: 400,
            data: {
                success: false,
                message: "Email is already registered and verified. Please login instead.",
                body: null
            }
        }
    };
    
    // Test the error handling logic
    if (mockError.response && mockError.response.data) {
        const errorData = mockError.response.data;
        
        if (errorData.success === false && errorData.message) {
            if (errorData.message.toLowerCase().includes('email') && 
                errorData.message.toLowerCase().includes('already registered')) {
                console.log('✅ Would show field error for email:', errorData.message);
                showFieldError('email', errorData.message);
            } else {
                console.log('✅ Would show general error:', errorData.message);
                showMessage(errorData.message, 'error');
            }
        }
    }
}

// Export functions for global access
window.togglePassword = togglePassword;
window.testErrorHandling = testErrorHandling;
