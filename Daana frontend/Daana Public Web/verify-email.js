/**
 * Email Verification JavaScript for Daana.lk
 * Handles OTP verification and email verification flow
 */

// DOM Content Loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeVerification();
});

// Initialize Verification
function initializeVerification() {
    setupVerificationForm();
    setupOtpInputs();
    setupResendButton();
    loadUserEmail();
    
    // Force apply OTP styling after a short delay
    setTimeout(forceOtpStyling, 100);
    
    console.log('✅ Email verification page initialized');
}

// Force OTP Input Styling
function forceOtpStyling() {
    const otpInputs = document.querySelectorAll('.otp-input');
    otpInputs.forEach((input) => {
        input.style.setProperty('color', '#000000', 'important');
        input.style.setProperty('background', 'white', 'important');
        input.style.setProperty('border', '2px solid #e1e5e9', 'important');
        input.style.setProperty('font-size', '1.5rem', 'important');
        input.style.setProperty('font-weight', '600', 'important');
        input.style.setProperty('text-align', 'center', 'important');
        input.style.setProperty('width', '50px', 'important');
        input.style.setProperty('height', '50px', 'important');
    });
    console.log('✅ OTP styling forced applied');
}

// Setup Verification Form
function setupVerificationForm() {
    const verifyForm = document.getElementById('verifyForm');
    if (verifyForm) {
        verifyForm.addEventListener('submit', handleVerification);
    }
}

// Setup OTP Input Fields
function setupOtpInputs() {
    const otpInputs = document.querySelectorAll('.otp-input');
    
    // Force styling for OTP inputs
    otpInputs.forEach((input) => {
        input.style.color = '#000000';
        input.style.background = 'white';
        input.style.border = '2px solid #e1e5e9';
        input.style.fontSize = '1.5rem';
        input.style.fontWeight = '600';
        input.style.textAlign = 'center';
    });
    
    otpInputs.forEach((input, index) => {
        // Handle input
        input.addEventListener('input', (e) => {
            const value = e.target.value;
            
            // Only allow numbers
            if (!/^\d*$/.test(value)) {
                e.target.value = value.replace(/\D/g, '');
                return;
            }
            
            // Move to next input if current is filled
            if (value.length === 1 && index < otpInputs.length - 1) {
                otpInputs[index + 1].focus();
            }
            
            // Update hidden input
            updateOtpCode();
            clearOtpError();
        });
        
        // Handle backspace
        input.addEventListener('keydown', (e) => {
            if (e.key === 'Backspace' && e.target.value === '' && index > 0) {
                otpInputs[index - 1].focus();
            }
        });
        
        // Handle paste
        input.addEventListener('paste', (e) => {
            e.preventDefault();
            const pastedData = e.clipboardData.getData('text').replace(/\D/g, '');
            
            if (pastedData.length === 6) {
                // Fill all inputs with pasted data
                for (let i = 0; i < 6; i++) {
                    otpInputs[i].value = pastedData[i] || '';
                }
                updateOtpCode();
                clearOtpError();
                otpInputs[5].focus();
            }
        });
    });
}

// Setup Resend Button
function setupResendButton() {
    const resendBtn = document.getElementById('resendBtn');
    if (resendBtn) {
        resendBtn.addEventListener('click', handleResendCode);
    }
}

// Load User Email from URL or localStorage
function loadUserEmail() {
    const urlParams = new URLSearchParams(window.location.search);
    const emailFromUrl = urlParams.get('email');
    const emailFromStorage = localStorage.getItem('pendingVerificationEmail');
    const email = emailFromUrl || emailFromStorage;
    
    console.log('Email from URL:', emailFromUrl);
    console.log('Email from localStorage:', emailFromStorage);
    console.log('Selected email:', email);
    
    if (email) {
        document.getElementById('userEmail').textContent = email;
        // Always update localStorage with the current email
        localStorage.setItem('pendingVerificationEmail', email);
        console.log('Email set in localStorage:', email);
    } else {
        // No email found, redirect to signup
        showMessage('No email found for verification. Please sign up first.', 'error');
        setTimeout(() => {
            window.location.href = 'signup.html';
        }, 3000);
    }
}

// Handle Verification Form Submission
async function handleVerification(event) {
    event.preventDefault();
    
    const otpCode = document.getElementById('otpCode').value;
    const email = getCurrentEmail();
    
    // Validate OTP
    if (!validateOtp(otpCode)) {
        return;
    }
    
    try {
        showVerificationLoading(true);
        clearMessage();
        
        console.log('Verifying OTP for email:', email);
        
        const response = await apiService.verifyOtp(email, otpCode);
        
        if (response.success) {
            // Show success message
            showMessage('Email verified successfully! You can now login.', 'success');
            
            // Clear pending verification email
            localStorage.removeItem('pendingVerificationEmail');
            
            // Redirect to login page after 3 seconds
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 3000);
            
        } else {
            showMessage(response.message || 'Verification failed. Please try again.', 'error');
        }
        
    } catch (error) {
        console.error('Verification error:', error);
        
        // Handle API error response format
        if (error.response && error.response.data) {
            const errorData = error.response.data;
            
            // Handle structured error response
            if (errorData.success === false && errorData.message) {
                if (errorData.message.toLowerCase().includes('invalid otp') || 
                    errorData.message.toLowerCase().includes('otp code')) {
                    showOtpError(errorData.message);
                } else {
                    showMessage(errorData.message, 'error');
                }
            } else {
                showMessage('Verification failed. Please try again.', 'error');
            }
        } else if (error.message) {
            // Handle other error types
            if (error.message.includes('network') || error.message.includes('fetch')) {
                showMessage('Network error. Please check your connection and try again.', 'error');
            } else {
                showMessage(error.message, 'error');
            }
        } else {
            showMessage('Verification failed. Please try again later.', 'error');
        }
    } finally {
        showVerificationLoading(false);
    }
}

// Handle Resend Code
async function handleResendCode() {
    const email = getCurrentEmail();
    
    console.log('Resend - Current email:', email);
    console.log('Resend - Current URL:', window.location.href);
    
    if (!email) {
        showMessage('No email found. Please sign up again.', 'error');
        return;
    }
    
    const resendBtn = document.getElementById('resendBtn');
    
    try {
        resendBtn.disabled = true;
        resendBtn.textContent = 'Sending...';
        
        console.log('Resending verification code to:', email);
        
        const response = await apiService.resendOtp(email);
        
        if (response.success) {
            showMessage('OTP code has been resent to your email address.', 'success');
            
            // Clear OTP inputs
            clearOtpInputs();
            
        } else {
            showMessage(response.message || 'Failed to resend code. Please try again.', 'error');
        }
        
    } catch (error) {
        console.error('Resend error:', error);
        
        // Handle API error response format
        if (error.response && error.response.data) {
            const errorData = error.response.data;
            
            // Handle structured error response
            if (errorData.success === false && errorData.message) {
                if (errorData.message.toLowerCase().includes('user not found')) {
                    showMessage('Email not found. Please sign up again.', 'error');
                } else {
                    showMessage(errorData.message, 'error');
                }
            } else {
                showMessage('Failed to resend code. Please try again.', 'error');
            }
        } else if (error.message) {
            // Handle other error types
            if (error.message.includes('network') || error.message.includes('fetch')) {
                showMessage('Network error. Please check your connection and try again.', 'error');
            } else {
                showMessage(error.message, 'error');
            }
        } else {
            showMessage('Failed to resend code. Please try again later.', 'error');
        }
    } finally {
        resendBtn.disabled = false;
        resendBtn.textContent = 'Resend Code';
    }
}

// Validate OTP
function validateOtp(otpCode) {
    if (!otpCode || otpCode.length !== 6) {
        showOtpError('Please enter a valid 6-digit verification code');
        return false;
    }
    
    if (!/^\d{6}$/.test(otpCode)) {
        showOtpError('Verification code must contain only numbers');
        return false;
    }
    
    return true;
}

// Update OTP Code Hidden Input
function updateOtpCode() {
    const otpInputs = document.querySelectorAll('.otp-input');
    let otpCode = '';
    
    otpInputs.forEach(input => {
        otpCode += input.value;
    });
    
    document.getElementById('otpCode').value = otpCode;
}

// Clear OTP Inputs
function clearOtpInputs() {
    const otpInputs = document.querySelectorAll('.otp-input');
    otpInputs.forEach(input => {
        input.value = '';
    });
    document.getElementById('otpCode').value = '';
    clearOtpError();
}

// Show OTP Error
function showOtpError(message) {
    const otpInputs = document.querySelectorAll('.otp-input');
    const errorElement = document.getElementById('otpCodeError');
    
    // Add error class to all OTP inputs
    otpInputs.forEach(input => {
        input.classList.add('error');
    });
    
    // Show error message
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.style.display = 'block';
    }
}

// Clear OTP Error
function clearOtpError() {
    const otpInputs = document.querySelectorAll('.otp-input');
    const errorElement = document.getElementById('otpCodeError');
    
    // Remove error class from all OTP inputs
    otpInputs.forEach(input => {
        input.classList.remove('error');
    });
    
    // Hide error message
    if (errorElement) {
        errorElement.textContent = '';
        errorElement.style.display = 'none';
    }
}

// Show Verification Loading State
function showVerificationLoading(show) {
    const verifyBtn = document.getElementById('verifyBtn');
    const btnText = verifyBtn.querySelector('.btn-text');
    const btnLoading = verifyBtn.querySelector('.btn-loading');
    
    if (show) {
        verifyBtn.disabled = true;
        btnText.style.display = 'none';
        btnLoading.style.display = 'inline-flex';
    } else {
        verifyBtn.disabled = false;
        btnText.style.display = 'inline';
        btnLoading.style.display = 'none';
    }
}

// Show Message
function showMessage(message, type) {
    const messageElement = document.getElementById('verifyMessage');
    
    if (messageElement) {
        messageElement.textContent = message;
        messageElement.className = `verify-message verify-message-${type}`;
        messageElement.style.display = 'block';
        
        // Auto-remove success messages after 5 seconds
        if (type === 'success') {
            setTimeout(() => {
                messageElement.style.display = 'none';
            }, 5000);
        }
    }
}

// Clear Message
function clearMessage() {
    const messageElement = document.getElementById('verifyMessage');
    if (messageElement) {
        messageElement.style.display = 'none';
        messageElement.textContent = '';
    }
}

// Get Current Email
function getCurrentEmail() {
    const urlParams = new URLSearchParams(window.location.search);
    const emailFromUrl = urlParams.get('email');
    const emailFromStorage = localStorage.getItem('pendingVerificationEmail');
    const email = emailFromUrl || emailFromStorage;
    
    console.log('Getting current email - URL:', emailFromUrl, 'Storage:', emailFromStorage, 'Selected:', email);
    return email;
}

// Export functions for global access
window.clearOtpInputs = clearOtpInputs;
window.showMessage = showMessage;
window.getCurrentEmail = getCurrentEmail;
