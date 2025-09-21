/**
 * Charity Registration JavaScript
 * Handles the 3-step charity registration process
 */

class CharityRegistration {
    constructor() {
        this.currentStep = 1;
        this.maxSteps = 3;
        this.registrationData = {};
        this.charityId = null;
        
        this.init();
    }

    init() {
        this.bindEvents();
        this.updateStepDisplay();
        this.setupFileUploads();
        this.setupOtpInputs();
    }

    bindEvents() {
        // Navigation buttons
        document.getElementById('nextBtn').addEventListener('click', () => this.nextStep());
        document.getElementById('prevBtn').addEventListener('click', () => this.prevStep());
        document.getElementById('resendOtpBtn').addEventListener('click', () => this.resendOtp());

        // Form submissions
        document.getElementById('step1Form').addEventListener('submit', (e) => {
            e.preventDefault();
            this.nextStep();
        });

        document.getElementById('step2Form').addEventListener('submit', (e) => {
            e.preventDefault();
            this.nextStep();
        });

        document.getElementById('step3Form').addEventListener('submit', (e) => {
            e.preventDefault();
            this.nextStep();
        });

        // Input validation
        this.setupInputValidation();
        
        // Organization type and document type validation
        this.setupOrganizationTypeValidation();
    }

    setupInputValidation() {
        // Real-time validation for required fields
        const requiredFields = document.querySelectorAll('input[required], select[required], textarea[required]');
        requiredFields.forEach(field => {
            field.addEventListener('blur', () => this.validateField(field));
            field.addEventListener('input', () => this.clearFieldError(field));
        });

        // Email validation
        const emailFields = document.querySelectorAll('input[type="email"]');
        emailFields.forEach(field => {
            field.addEventListener('blur', () => this.validateEmail(field));
        });

        // Phone number validation
        const phoneFields = document.querySelectorAll('input[type="tel"]');
        phoneFields.forEach(field => {
            field.addEventListener('blur', () => this.validatePhone(field));
        });

        // URL validation
        const websiteField = document.getElementById('website');
        if (websiteField) {
            websiteField.addEventListener('blur', () => this.validateUrl(websiteField));
        }
    }

    setupOrganizationTypeValidation() {
        const executionTypeField = document.getElementById('executionType');
        const documentTypeField = document.getElementById('documentType');
        const documentTypeHelp = document.getElementById('documentTypeHelp');

        if (executionTypeField && documentTypeField) {
            executionTypeField.addEventListener('change', () => {
                this.updateDocumentTypeBasedOnOrganizationType();
            });

            // Also validate when document type is manually changed
            documentTypeField.addEventListener('change', () => {
                this.validateDocumentTypeMatch();
            });

            // Initial setup
            this.updateDocumentTypeBasedOnOrganizationType();
        }
    }

    updateDocumentTypeBasedOnOrganizationType() {
        const executionType = document.getElementById('executionType').value;
        const documentTypeField = document.getElementById('documentType');
        const documentTypeHelp = document.getElementById('documentTypeHelp');

        if (!executionType) {
            documentTypeField.value = '';
            documentTypeHelp.textContent = 'Select the appropriate document type based on your organization type';
            return;
        }

        // Clear current selection
        documentTypeField.value = '';

        if (executionType === 'ORGANIZATION') {
            // For organizations, set default to Business Registration Certificate
            documentTypeField.value = 'BUSINESS_REGISTRATION_CERTIFICATE';
            documentTypeHelp.textContent = 'Required: Business Registration Certificate for organizations';
            documentTypeHelp.style.color = '#27ae60';
        } else if (executionType === 'PERSON') {
            // For individuals, set default to ID Card
            documentTypeField.value = 'ID_CARD';
            documentTypeHelp.textContent = 'Required: ID Card for individual persons';
            documentTypeHelp.style.color = '#27ae60';
        } else {
            documentTypeHelp.textContent = 'Select the appropriate document type based on your organization type';
            documentTypeHelp.style.color = '#718096';
        }

        // Clear any existing errors
        this.clearFieldError(documentTypeField);
    }

    setupFileUploads() {
        // Document file upload
        const documentFileInput = document.getElementById('documentFile');
        const documentFilePreview = document.getElementById('documentFilePreview');
        
        documentFileInput.addEventListener('change', (e) => {
            this.handleFileUpload(e, documentFilePreview, 'documentFile');
        });

        // Logo file upload
        const logoFileInput = document.getElementById('logoFile');
        const logoFilePreview = document.getElementById('logoFilePreview');
        
        logoFileInput.addEventListener('change', (e) => {
            this.handleFileUpload(e, logoFilePreview, 'logoFile');
        });
    }

    setupOtpInputs() {
        const otpInputs = document.querySelectorAll('.otp-input');
        
        otpInputs.forEach((input, index) => {
            input.addEventListener('input', (e) => {
                const value = e.target.value;
                
                // Only allow numbers
                if (!/^\d*$/.test(value)) {
                    e.target.value = value.replace(/\D/g, '');
                    return;
                }
                
                // Move to next input
                if (value && index < otpInputs.length - 1) {
                    otpInputs[index + 1].focus();
                }
            });
            
            input.addEventListener('keydown', (e) => {
                // Move to previous input on backspace
                if (e.key === 'Backspace' && !e.target.value && index > 0) {
                    otpInputs[index - 1].focus();
                }
            });
            
            input.addEventListener('paste', (e) => {
                e.preventDefault();
                const pastedData = e.clipboardData.getData('text').replace(/\D/g, '');
                if (pastedData.length === 6) {
                    pastedData.split('').forEach((digit, i) => {
                        if (otpInputs[i]) {
                            otpInputs[i].value = digit;
                        }
                    });
                    otpInputs[5].focus();
                }
            });
        });
    }

    handleFileUpload(event, previewElement, fieldName) {
        const file = event.target.files[0];
        if (!file) return;

        // Validate file type
        const allowedTypes = {
            documentFile: ['.pdf', '.jpg', '.jpeg', '.png'],
            logoFile: ['.jpg', '.jpeg', '.png', '.webp']
        };

        const fileExtension = '.' + file.name.split('.').pop().toLowerCase();
        if (!allowedTypes[fieldName].includes(fileExtension)) {
            this.showFieldError(event.target, `Please select a valid file type: ${allowedTypes[fieldName].join(', ')}`);
            return;
        }

        // Validate file size (5MB max)
        const maxSize = 5 * 1024 * 1024; // 5MB
        if (file.size > maxSize) {
            this.showFieldError(event.target, 'File size must be less than 5MB');
            return;
        }

        // Show file preview
        previewElement.textContent = `Selected: ${file.name} (${this.formatFileSize(file.size)})`;
        previewElement.classList.add('show');
        this.clearFieldError(event.target);

        // Store file for later upload
        this.registrationData[fieldName] = file;
    }

    formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    async nextStep() {
        if (this.currentStep === 1) {
            if (!await this.validateStep1()) return;
            await this.submitStep1();
        } else if (this.currentStep === 2) {
            if (!await this.validateStep2()) return;
            await this.submitStep2();
        } else if (this.currentStep === 3) {
            if (!await this.validateStep3()) return;
            await this.submitStep3();
        }
    }

    prevStep() {
        if (this.currentStep > 1) {
            this.currentStep--;
            this.updateStepDisplay();
        }
    }

    updateStepDisplay() {
        // Update step indicators
        for (let i = 1; i <= this.maxSteps; i++) {
            const indicator = document.getElementById(`step${i}-indicator`);
            const stepContent = document.getElementById(`step${i}`);
            
            if (i < this.currentStep) {
                indicator.classList.remove('active', 'pending');
                indicator.classList.add('completed');
                stepContent.classList.remove('active');
            } else if (i === this.currentStep) {
                indicator.classList.remove('completed', 'pending');
                indicator.classList.add('active');
                stepContent.classList.remove('active');
                stepContent.classList.add('active');
            } else {
                indicator.classList.remove('active', 'completed');
                indicator.classList.add('pending');
                stepContent.classList.remove('active');
            }
        }

        // Update navigation buttons
        const prevBtn = document.getElementById('prevBtn');
        const nextBtn = document.getElementById('nextBtn');
        
        prevBtn.style.display = this.currentStep > 1 ? 'flex' : 'none';
        
        if (this.currentStep === this.maxSteps) {
            nextBtn.innerHTML = `
                <span class="btn-text">Complete Registration</span>
                <span class="btn-loading">
                    <i class="fas fa-spinner fa-spin"></i>
                    Verifying...
                </span>
            `;
        } else {
            nextBtn.innerHTML = `
                <span class="btn-text">Next Step</span>
                <span class="btn-loading">
                    <i class="fas fa-spinner fa-spin"></i>
                    Processing...
                </span>
            `;
        }
    }

    async validateStep1() {
        const requiredFields = [
            'name', 'email', 'password', 'executionType', 'mobileNumber',
            'description', 'nicNumberOrRegistrationNumber', 'contactPersonName',
            'contactPersonMobile', 'contactPersonEmail'
        ];

        let isValid = true;

        for (const fieldName of requiredFields) {
            const field = document.getElementById(fieldName);
            if (!this.validateField(field)) {
                isValid = false;
            }
        }

        // Additional validations
        if (!this.validateEmail(document.getElementById('email'))) isValid = false;
        if (!this.validatePhone(document.getElementById('mobileNumber'))) isValid = false;
        if (!this.validatePhone(document.getElementById('contactPersonMobile'))) isValid = false;
        if (!this.validateEmail(document.getElementById('contactPersonEmail'))) isValid = false;
        
        const websiteField = document.getElementById('website');
        if (websiteField.value && !this.validateUrl(websiteField)) isValid = false;

        return isValid;
    }

    async validateStep2() {
        const requiredFields = [
            'documentType', 'documentFile', 'bankName', 'branchName',
            'accountHolderName', 'swiftCode', 'accountNumber'
        ];

        let isValid = true;

        for (const fieldName of requiredFields) {
            const field = document.getElementById(fieldName);
            if (!this.validateField(field)) {
                isValid = false;
            }
        }

        // Validate document type matches organization type
        if (!this.validateDocumentTypeMatch()) {
            isValid = false;
        }

        return isValid;
    }

    validateDocumentTypeMatch() {
        const executionType = document.getElementById('executionType').value;
        const documentType = document.getElementById('documentType').value;
        const documentTypeField = document.getElementById('documentType');

        if (executionType === 'ORGANIZATION' && documentType !== 'BUSINESS_REGISTRATION_CERTIFICATE') {
            this.showFieldError(documentTypeField, 'Organizations must provide a Business Registration Certificate');
            return false;
        }

        if (executionType === 'PERSON' && documentType !== 'ID_CARD') {
            this.showFieldError(documentTypeField, 'Individual persons must provide an ID Card');
            return false;
        }

        this.clearFieldError(documentTypeField);
        return true;
    }

    async validateStep3() {
        const otpInputs = document.querySelectorAll('.otp-input');
        const otpCode = Array.from(otpInputs).map(input => input.value).join('');
        
        if (otpCode.length !== 6) {
            this.showFieldError(otpInputs[0], 'Please enter the complete 6-digit OTP code');
            return false;
        }

        return true;
    }

    validateField(field) {
        if (!field.value.trim()) {
            this.showFieldError(field, 'This field is required');
            return false;
        }
        this.clearFieldError(field);
        return true;
    }

    validateEmail(field) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (field.value && !emailRegex.test(field.value)) {
            this.showFieldError(field, 'Please enter a valid email address');
            return false;
        }
        this.clearFieldError(field);
        return true;
    }

    validatePhone(field) {
        const phoneRegex = /^[0-9]{9,10}$/;
        if (field.value && !phoneRegex.test(field.value.replace(/\D/g, ''))) {
            this.showFieldError(field, 'Please enter a valid phone number');
            return false;
        }
        this.clearFieldError(field);
        return true;
    }

    validateUrl(field) {
        try {
            new URL(field.value);
            this.clearFieldError(field);
            return true;
        } catch {
            this.showFieldError(field, 'Please enter a valid URL');
            return false;
        }
    }

    showFieldError(field, message) {
        field.classList.add('error');
        const errorElement = document.getElementById(field.name + 'Error') || 
                           document.getElementById(field.id + 'Error');
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.classList.add('show');
        }
    }

    clearFieldError(field) {
        field.classList.remove('error');
        const errorElement = document.getElementById(field.name + 'Error') || 
                           document.getElementById(field.id + 'Error');
        if (errorElement) {
            errorElement.classList.remove('show');
        }
    }

    async submitStep1() {
        const nextBtn = document.getElementById('nextBtn');
        nextBtn.classList.add('loading');
        nextBtn.disabled = true;

        try {
            // Collect form data
            const formData = {
                name: document.getElementById('name').value,
                email: document.getElementById('email').value,
                password: document.getElementById('password').value,
                executionType: document.getElementById('executionType').value,
                website: document.getElementById('website').value || null,
                description: document.getElementById('description').value,
                mobileNumber: parseInt(document.getElementById('mobileNumber').value.replace(/\D/g, '')),
                nicNumberOrRegistrationNumber: document.getElementById('nicNumberOrRegistrationNumber').value,
                contactPersonName: document.getElementById('contactPersonName').value,
                contactPersonMobile: parseInt(document.getElementById('contactPersonMobile').value.replace(/\D/g, '')),
                contactPersonEmail: document.getElementById('contactPersonEmail').value
            };

            // Make API call
            const response = await this.callCharityStep1API(formData);
            
            if (response.success) {
                this.charityId = response.body.id;
                this.registrationData = { ...this.registrationData, ...formData };
                this.currentStep++;
                this.updateStepDisplay();
                this.showSuccessMessage('Registration step 1 completed. Please check your email for OTP code.');
            } else {
                throw new Error(response.message || 'Step 1 registration failed');
            }
        } catch (error) {
            console.error('Step 1 submission error:', error);
            this.showErrorMessage(error.message || 'Failed to submit step 1. Please try again.');
        } finally {
            nextBtn.classList.remove('loading');
            nextBtn.disabled = false;
        }
    }

    async submitStep2() {
        const nextBtn = document.getElementById('nextBtn');
        nextBtn.classList.add('loading');
        nextBtn.disabled = true;

        try {
            // Create FormData for file upload
            const formData = new FormData();
            formData.append('id', this.charityId);
            formData.append('documentType', document.getElementById('documentType').value);
            formData.append('bankName', document.getElementById('bankName').value);
            formData.append('branchName', document.getElementById('branchName').value);
            formData.append('accountHolderName', document.getElementById('accountHolderName').value);
            formData.append('swiftCode', document.getElementById('swiftCode').value);
            formData.append('accountNumber', document.getElementById('accountNumber').value);

            // Add files if they exist
            if (this.registrationData.documentFile) {
                formData.append('documentFile', this.registrationData.documentFile);
            }
            if (this.registrationData.logoFile) {
                formData.append('logoFile', this.registrationData.logoFile);
            }

            // Make API call
            const response = await this.callCharityStep2API(formData);
            
            if (response.success) {
                this.currentStep++;
                this.updateStepDisplay();
                this.showSuccessMessage('Documents uploaded successfully. Please proceed to email verification.');
            } else {
                throw new Error(response.message || 'Step 2 registration failed');
            }
        } catch (error) {
            console.error('Step 2 submission error:', error);
            this.showErrorMessage(error.message || 'Failed to submit step 2. Please try again.');
        } finally {
            nextBtn.classList.remove('loading');
            nextBtn.disabled = false;
        }
    }

    async submitStep3() {
        const nextBtn = document.getElementById('nextBtn');
        nextBtn.classList.add('loading');
        nextBtn.disabled = true;

        try {
            // Get OTP code
            const otpInputs = document.querySelectorAll('.otp-input');
            const otpCode = Array.from(otpInputs).map(input => input.value).join('');

            const formData = {
                id: this.charityId,
                otpCode: otpCode
            };

            // Make API call
            const response = await this.callCharityStep3API(formData);
            
            if (response.success) {
                this.showSuccessMessage('Your request is under review and we will get back to you soon.');
                this.showCompletionMessage();
            } else {
                throw new Error(response.message || 'Step 3 verification failed');
            }
        } catch (error) {
            console.error('Step 3 submission error:', error);
            this.showErrorMessage(error.message || 'Failed to verify OTP. Please try again.');
        } finally {
            nextBtn.classList.remove('loading');
            nextBtn.disabled = false;
        }
    }

    async resendOtp() {
        const resendBtn = document.getElementById('resendOtpBtn');
        resendBtn.disabled = true;
        resendBtn.textContent = 'Sending...';

        try {
            // Make API call to resend OTP
            const response = await this.callResendOtpAPI(this.registrationData.email);
            
            if (response.success) {
                this.showSuccessMessage('OTP code has been resent to your email.');
            } else {
                throw new Error(response.message || 'Failed to resend OTP');
            }
        } catch (error) {
            console.error('Resend OTP error:', error);
            this.showErrorMessage(error.message || 'Failed to resend OTP. Please try again.');
        } finally {
            resendBtn.disabled = false;
            resendBtn.textContent = 'Resend OTP';
        }
    }

    // API Methods using the centralized API service
    async callCharityStep1API(data) {
        try {
            return await apiService.charityRegisterStep1(data);
        } catch (error) {
            throw error;
        }
    }

    async callCharityStep2API(formData) {
        try {
            return await apiService.charityRegisterStep2(formData);
        } catch (error) {
            throw error;
        }
    }

    async callCharityStep3API(data) {
        try {
            return await apiService.charityRegisterStep3(data);
        } catch (error) {
            throw error;
        }
    }

    async callResendOtpAPI(email) {
        try {
            return await apiService.charityResendOtp(email);
        } catch (error) {
            throw error;
        }
    }

    showSuccessMessage(message) {
        const successElement = document.getElementById('successMessage');
        const successText = document.getElementById('successText');
        successText.textContent = message;
        successElement.classList.add('show');
        
        // Hide after 5 seconds
        setTimeout(() => {
            successElement.classList.remove('show');
        }, 5000);
    }

    showErrorMessage(message) {
        // Create a temporary error message element
        const errorElement = document.createElement('div');
        errorElement.className = 'error-message show';
        errorElement.style.cssText = `
            background: #fed7d7;
            color: #c53030;
            padding: 1rem;
            border-radius: 10px;
            margin-bottom: 1rem;
            text-align: center;
            font-weight: 500;
        `;
        errorElement.textContent = message;
        
        const formContainer = document.querySelector('.form-container');
        formContainer.insertBefore(errorElement, formContainer.firstChild);
        
        // Hide after 5 seconds
        setTimeout(() => {
            errorElement.remove();
        }, 5000);
    }

    showCompletionMessage() {
        // Hide the form and show completion message
        const formContainer = document.querySelector('.form-container');
        formContainer.innerHTML = `
            <div style="text-align: center; padding: 3rem 0;">
                <div style="width: 100px; height: 100px; background: #27ae60; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 2rem; color: white; font-size: 3rem;">
                    <i class="fas fa-check"></i>
                </div>
                <h2 style="color: #1a202c; margin-bottom: 1rem; font-size: 2rem;">Registration Complete!</h2>
                <p style="color: #718096; margin-bottom: 2rem; font-size: 1.1rem; line-height: 1.6;">
                    Thank you for registering with Daana.lk. Your charity organization registration is now under review.<br>
                    We will get back to you soon with the approval status.
                </p>
                <div style="display: flex; gap: 1rem; justify-content: center; flex-wrap: wrap;">
                    <a href="index.html" class="btn btn-primary">
                        <i class="fas fa-home"></i>
                        Go to Home
                    </a>
                    <a href="login.html" class="btn btn-secondary">
                        <i class="fas fa-sign-in-alt"></i>
                        Login
                    </a>
                </div>
            </div>
        `;
    }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new CharityRegistration();
});

// Mobile menu toggle (reuse from existing code)
document.addEventListener('DOMContentLoaded', () => {
    const hamburger = document.querySelector('.hamburger');
    const navMenu = document.querySelector('.nav-menu');

    if (hamburger && navMenu) {
        hamburger.addEventListener('click', () => {
            hamburger.classList.toggle('active');
            navMenu.classList.toggle('active');
        });

        // Close menu when clicking on a link
        document.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', () => {
                hamburger.classList.remove('active');
                navMenu.classList.remove('active');
            });
        });
    }
});
