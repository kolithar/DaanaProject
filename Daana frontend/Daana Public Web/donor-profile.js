/**
 * Donor Profile Management
 * Handles profile data loading, updating, and image upload
 */

class DonorProfileManager {
    constructor() {
        this.apiService = window.apiService;
        this.profileData = null;
        this.originalData = null;
        
        this.init();
    }

    async init() {
        // Check authentication
        if (!this.checkAuthentication()) {
            return;
        }

        // Load profile data
        await this.loadProfileData();
        
        
        // Setup event listeners
        this.setupEventListeners();
        
        // Initialize profile dropdown
        this.initializeProfileDropdown();
        
    }

    checkAuthentication() {
        const token = localStorage.getItem('daana_access_token');
        if (!token) {
            this.redirectToLogin();
            return false;
        }
        return true;
    }

    redirectToLogin() {
        window.location.href = 'login.html';
    }

    async loadProfileData() {
        try {
            this.showLoading(true);
            
            const response = await this.apiService.getDonorProfile();
            
            if (response.success && response.body) {
                this.profileData = response.body;
                this.originalData = JSON.parse(JSON.stringify(response.body));
                this.populateProfileForm();
                this.updateProfileDisplay();
            } else {
                throw new Error('Failed to load profile data');
            }
        } catch (error) {
            console.error('Error loading profile:', error);
            this.showError('Failed to load profile data. Please try again.');
        } finally {
            this.showLoading(false);
        }
    }

    populateProfileForm() {
        if (!this.profileData) return;

        // Populate form fields
        document.getElementById('firstName').value = this.profileData.firstName || '';
        document.getElementById('lastName').value = this.profileData.lastName || '';
        document.getElementById('email').value = this.profileData.email || '';
        document.getElementById('gender').value = this.profileData.gender || '';
        document.getElementById('phoneNumber').value = this.profileData.phoneNumber || '';
        document.getElementById('uniqueCustomerId').value = this.profileData.uniqueCustomerId || '';

        // Update profile image
        this.updateProfileImage();
    }

    updateProfileDisplay() {
        if (!this.profileData) return;

        // Update header information
        document.getElementById('profileName').textContent = this.profileData.fullName || this.profileData.displayName || 'User';
        document.getElementById('profileEmail').textContent = this.profileData.email || '';

        // Update account status
        const accountStatus = document.getElementById('accountStatus');
        const accountStatusText = document.getElementById('accountStatusText');
        
        if (this.profileData.accountVerifyStatus) {
            accountStatus.innerHTML = '<i class="fas fa-check-circle"></i> Account Verified';
            accountStatus.className = 'status-badge verified';
            accountStatusText.textContent = 'Verified';
        } else {
            accountStatus.innerHTML = '<i class="fas fa-clock"></i> Pending Verification';
            accountStatus.className = 'status-badge pending';
            accountStatusText.textContent = 'Pending Verification';
        }

        // Update dates
        if (this.profileData.created) {
            const createdDate = new Date(this.profileData.created);
            document.getElementById('memberSince').textContent = createdDate.toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            });
        }

        if (this.profileData.updated) {
            const updatedDate = new Date(this.profileData.updated);
            document.getElementById('lastUpdated').textContent = updatedDate.toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            });
        }

        // Update billing address
        const billingAddress = this.profileData.billingAddress || 'Not provided';
        document.getElementById('billingAddress').textContent = billingAddress;
    }

    updateProfileImage() {
        const profileImage = document.getElementById('profileImage');
        const avatarPlaceholder = document.getElementById('avatarPlaceholder');
        
        if (this.profileData.profileImageUrl) {
            profileImage.src = this.profileData.profileImageUrl;
            profileImage.style.display = 'block';
            avatarPlaceholder.style.display = 'none';
        } else {
            profileImage.style.display = 'none';
            avatarPlaceholder.style.display = 'flex';
        }
    }

    setupEventListeners() {
        // Profile form submission
        const profileForm = document.getElementById('profileForm');
        profileForm.addEventListener('submit', (e) => {
            e.preventDefault();
            this.updateProfile();
        });

        // Form validation
        const inputs = profileForm.querySelectorAll('input, select');
        inputs.forEach(input => {
            input.addEventListener('blur', () => this.validateField(input));
            input.addEventListener('input', () => this.clearFieldError(input));
        });
    }

    initializeProfileDropdown() {
        // Update profile dropdown with current user data
        if (this.profileData) {
            const navProfileName = document.getElementById('navProfileName');
            const dropdownProfileName = document.getElementById('dropdownProfileName');
            const dropdownProfileEmail = document.getElementById('dropdownProfileEmail');
            
            // Check if dropdown elements exist before updating
            if (navProfileName && dropdownProfileName && dropdownProfileEmail) {
                if (this.profileData.fullName || this.profileData.displayName) {
                    const displayName = this.profileData.fullName || this.profileData.displayName;
                    navProfileName.textContent = displayName;
                    dropdownProfileName.textContent = displayName;
                }
                
                if (this.profileData.email) {
                    dropdownProfileEmail.textContent = this.profileData.email;
                }
                
                // Update profile images
                this.updateDropdownProfileImages(this.profileData.profileImageUrl);
            } else {
                console.log('Profile dropdown elements not found on this page');
            }
        }
    }

    updateDropdownProfileImages(profileImageUrl) {
        const navProfileImage = document.getElementById('navProfileImage');
        const navAvatarPlaceholder = document.getElementById('navAvatarPlaceholder');
        const dropdownProfileImage = document.getElementById('dropdownProfileImage');
        const dropdownAvatarPlaceholder = document.getElementById('dropdownAvatarPlaceholder');
        
        // Check if elements exist before updating
        if (!navProfileImage || !navAvatarPlaceholder || !dropdownProfileImage || !dropdownAvatarPlaceholder) {
            console.log('Profile image elements not found on this page');
            return;
        }
        
        if (profileImageUrl) {
            navProfileImage.src = profileImageUrl;
            navProfileImage.style.display = 'block';
            navAvatarPlaceholder.style.display = 'none';
            
            dropdownProfileImage.src = profileImageUrl;
            dropdownProfileImage.style.display = 'block';
            dropdownAvatarPlaceholder.style.display = 'none';
        } else {
            navProfileImage.style.display = 'none';
            navAvatarPlaceholder.style.display = 'flex';
            
            dropdownProfileImage.style.display = 'none';
            dropdownAvatarPlaceholder.style.display = 'flex';
        }
    }

    async updateProfile() {
        try {
            const updateBtn = document.getElementById('updateProfileBtn');
            const btnText = updateBtn.querySelector('.btn-text');
            const btnLoading = updateBtn.querySelector('.btn-loading');
            
            // Show loading state
            btnText.style.display = 'none';
            btnLoading.style.display = 'flex';
            updateBtn.disabled = true;

            // Get form data
            const formData = {
                firstName: document.getElementById('firstName').value.trim(),
                lastName: document.getElementById('lastName').value.trim(),
                gender: document.getElementById('gender').value
            };

            // Validate form
            if (!this.validateForm(formData)) {
                throw new Error('Please fill in all required fields');
            }

            // Make API call
            const response = await this.apiService.updateDonorProfile(formData);

            if (response.success && response.body) {
                this.profileData = response.body;
                this.originalData = JSON.parse(JSON.stringify(response.body));
                this.updateProfileDisplay();
                this.showSuccess('Profile updated successfully!');
            } else {
                throw new Error(response.message || 'Failed to update profile');
            }

        } catch (error) {
            console.error('Error updating profile:', error);
            this.showError(error.message || 'Failed to update profile. Please try again.');
        } finally {
            // Reset button state
            const updateBtn = document.getElementById('updateProfileBtn');
            const btnText = updateBtn.querySelector('.btn-text');
            const btnLoading = updateBtn.querySelector('.btn-loading');
            
            btnText.style.display = 'flex';
            btnLoading.style.display = 'none';
            updateBtn.disabled = false;
        }
    }

    async handleProfileImageUpload(event) {
        const file = event.target.files[0];
        if (!file) return;

        // Validate file
        if (!this.validateImageFile(file)) {
            return;
        }

        try {
            this.showLoading(true, 'Uploading profile picture...');

            // Make API call for image upload
            const response = await this.apiService.uploadDonorProfilePicture(file);

            if (response.success && response.body) {
                // Update profile data with new image URL
                this.profileData.profileImageUrl = response.body;
                this.updateProfileImage();
                this.updateDropdownProfileImages(response.body);
                this.showSuccess('Profile picture updated successfully!');
            } else {
                throw new Error(response.message || 'Failed to upload profile picture');
            }

        } catch (error) {
            console.error('Error uploading profile picture:', error);
            this.showError(error.message || 'Failed to upload profile picture. Please try again.');
        } finally {
            this.showLoading(false);
            // Reset file input
            event.target.value = '';
        }
    }

    validateImageFile(file) {
        // Check file type
        const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
        if (!allowedTypes.includes(file.type)) {
            this.showError('Please select a valid image file (JPEG, PNG, or WebP)');
            return false;
        }

        // Check file size (5MB limit)
        const maxSize = 5 * 1024 * 1024; // 5MB
        if (file.size > maxSize) {
            this.showError('Image file size must be less than 5MB');
            return false;
        }

        return true;
    }

    validateForm(data) {
        let isValid = true;

        // Required fields
        if (!data.firstName.trim()) {
            this.showFieldError('firstName', 'First name is required');
            isValid = false;
        }

        if (!data.lastName.trim()) {
            this.showFieldError('lastName', 'Last name is required');
            isValid = false;
        }

        if (!data.gender) {
            this.showFieldError('gender', 'Please select your gender');
            isValid = false;
        }

        return isValid;
    }

    validateField(field) {
        const value = field.value.trim();
        const fieldName = field.name;

        // Clear previous errors
        this.clearFieldError(field);

        // Validate based on field type
        switch (fieldName) {
            case 'firstName':
            case 'lastName':
                if (!value) {
                    this.showFieldError(fieldName, 'This field is required');
                    return false;
                }
                if (value.length < 2) {
                    this.showFieldError(fieldName, 'Must be at least 2 characters');
                    return false;
                }
                break;
            case 'gender':
                if (!value) {
                    this.showFieldError(fieldName, 'Please select your gender');
                    return false;
                }
                break;
        }

        return true;
    }

    showFieldError(fieldName, message) {
        const field = document.getElementById(fieldName);
        const formGroup = field.closest('.form-group');
        
        // Remove existing error
        const existingError = formGroup.querySelector('.field-error');
        if (existingError) {
            existingError.remove();
        }

        // Add error message
        const errorDiv = document.createElement('div');
        errorDiv.className = 'field-error';
        errorDiv.textContent = message;
        formGroup.appendChild(errorDiv);

        // Add error class to field
        field.classList.add('error');
    }

    clearFieldError(field) {
        const formGroup = field.closest('.form-group');
        const errorDiv = formGroup.querySelector('.field-error');
        
        if (errorDiv) {
            errorDiv.remove();
        }
        
        field.classList.remove('error');
    }

    resetForm() {
        if (this.originalData) {
            this.profileData = JSON.parse(JSON.stringify(this.originalData));
            this.populateProfileForm();
            this.updateProfileDisplay();
        }
    }

    showLoading(show, message = 'Loading...') {
        const overlay = document.getElementById('loadingOverlay');
        const loadingText = overlay.querySelector('p');
        
        loadingText.textContent = message;
        overlay.style.display = show ? 'flex' : 'none';
    }

    showSuccess(message) {
        const modal = document.getElementById('successModal');
        const messageElement = document.getElementById('successMessage');
        
        messageElement.textContent = message;
        modal.style.display = 'flex';
    }

    showError(message) {
        const modal = document.getElementById('errorModal');
        const messageElement = document.getElementById('errorMessage');
        
        messageElement.textContent = message;
        modal.style.display = 'flex';
    }

}

// Global functions for HTML event handlers
function handleProfileImageUpload(event) {
    if (window.donorProfileManager) {
        window.donorProfileManager.handleProfileImageUpload(event);
    }
}

function resetForm() {
    if (window.donorProfileManager) {
        window.donorProfileManager.resetForm();
    }
}

function closeSuccessModal() {
    document.getElementById('successModal').style.display = 'none';
}

function closeErrorModal() {
    document.getElementById('errorModal').style.display = 'none';
}

function logout() {
    // Clear authentication data
    localStorage.removeItem('daana_access_token');
    localStorage.removeItem('daana_refresh_token');
    localStorage.removeItem('daana_user_data');
    
    // Redirect to login
    window.location.href = 'login.html';
}


// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.donorProfileManager = new DonorProfileManager();
});

// Close modals when clicking outside
window.addEventListener('click', (event) => {
    const successModal = document.getElementById('successModal');
    const errorModal = document.getElementById('errorModal');
    
    if (event.target === successModal) {
        closeSuccessModal();
    }
    
    if (event.target === errorModal) {
        closeErrorModal();
    }
});
