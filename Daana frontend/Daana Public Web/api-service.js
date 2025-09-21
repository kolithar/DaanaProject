/**
 * API Service for Daana.lk
 * Centralized API handling with proper error management and response formatting
 */

class ApiService {
    constructor() {
        // Detect environment and set appropriate base URLs
        this.isDevelopment = window.location.hostname === 'localhost' || 
                           window.location.hostname === '127.0.0.1' ||
                           window.location.hostname.includes('localhost');
        
        if (this.isDevelopment) {
            // Development environment - use localhost
            this.baseURL = 'http://localhost:8080/api/v1';
            this.publicBaseURL = 'http://localhost:8080/api/v1/public';
            this.authBaseURL = 'http://localhost:8080/api/v1/auth';
        } else {
            // Production environment - use relative URLs or your production domain
            this.baseURL = '/api/v1';
            this.publicBaseURL = '/api/v1/public';
            this.authBaseURL = '/api/v1/auth';
        }
        
        this.timeout = 10000; // 10 seconds timeout
        
        console.log('API Service initialized for:', this.isDevelopment ? 'Development' : 'Production');
        console.log('Base URLs:', {
            base: this.baseURL,
            public: this.publicBaseURL,
            auth: this.authBaseURL
        });
    }

    /**
     * Generic HTTP request method
     * @param {string} endpoint - API endpoint
     * @param {Object} options - Fetch options
     * @param {string} baseType - Base URL type ('public', 'auth', or 'default')
     * @returns {Promise} - API response
     */
    async request(endpoint, options = {}, baseType = 'default') {
        let baseURL;
        switch (baseType) {
            case 'public':
                baseURL = this.publicBaseURL;
                break;
            case 'auth':
                baseURL = this.authBaseURL;
                break;
            default:
                baseURL = this.baseURL;
        }
        
        const url = `${baseURL}${endpoint}`;
        const config = {
            timeout: this.timeout,
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        };

        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), this.timeout);
            
            const response = await fetch(url, {
                ...config,
                signal: controller.signal
            });
            
            clearTimeout(timeoutId);

            const data = await response.json();
            
            if (!response.ok) {
                // Create structured error with response data
                const error = new Error(data.message || `HTTP error! status: ${response.status}`);
                error.response = {
                    status: response.status,
                    data: data
                };
                throw error;
            }
            
            // Handle API response structure
            if (data.success === false) {
                throw new Error(data.message || 'API request failed');
            }

            return data;
        } catch (error) {
            console.error(`API Error [${endpoint}]:`, error);
            throw this.handleError(error);
        }
    }

    /**
     * Handle different types of errors
     * @param {Error} error - Error object
     * @returns {Error} - Formatted error
     */
    handleError(error) {
        // If it's already a structured error with response data, preserve it
        if (error.response) {
            return error;
        }
        
        if (error.name === 'AbortError') {
            const timeoutError = new Error('Request timeout. Please check your connection.');
            timeoutError.response = {
                status: 0,
                data: {
                    success: false,
                    message: 'Request timeout. Please check your connection.'
                }
            };
            return timeoutError;
        }
        
        if (error.message.includes('Failed to fetch')) {
            const networkError = new Error('Network error. Please check your connection.');
            networkError.response = {
                status: 0,
                data: {
                    success: false,
                    message: 'Network error. Please check your connection.'
                }
            };
            return networkError;
        }
        
        return error;
    }

    /**
     * GET request
     * @param {string} endpoint - API endpoint
     * @param {Object} params - Query parameters
     * @param {string} baseType - Base URL type ('public', 'auth', or 'default')
     * @returns {Promise} - API response
     */
    async get(endpoint, params = {}, baseType = 'default') {
        const queryString = new URLSearchParams(params).toString();
        const url = queryString ? `${endpoint}?${queryString}` : endpoint;
        
        return this.request(url, {
            method: 'GET'
        }, baseType);
    }

    /**
     * POST request
     * @param {string} endpoint - API endpoint
     * @param {Object} data - Request body data
     * @param {string} baseType - Base URL type ('public', 'auth', or 'default')
     * @returns {Promise} - API response
     */
    async post(endpoint, data = {}, baseType = 'default') {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(data)
        }, baseType);
    }

    /**
     * PUT request
     * @param {string} endpoint - API endpoint
     * @param {Object} data - Request body data
     * @param {string} baseType - Base URL type ('public', 'auth', or 'default')
     * @returns {Promise} - API response
     */
    async put(endpoint, data = {}, baseType = 'default') {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(data)
        }, baseType);
    }

    /**
     * DELETE request
     * @param {string} endpoint - API endpoint
     * @param {string} baseType - Base URL type ('public', 'auth', or 'default')
     * @returns {Promise} - API response
     */
    async delete(endpoint, baseType = 'default') {
        return this.request(endpoint, {
            method: 'DELETE'
        }, baseType);
    }

    // ==================== PROGRAM/DONATION APIs ====================

    /**
     * Get latest donation programs
     * @returns {Promise<Array>} - Array of latest programs
     */
    async getLatestPrograms() {
        try {
            const response = await this.get('/programs/latest', {}, 'public');
            return response.body || [];
        } catch (error) {
            console.error('Error fetching latest programs:', error);
            return [];
        }
    }

    /**
     * Get trending donation programs
     * @returns {Promise<Array>} - Array of trending programs
     */
    async getTrendingPrograms() {
        try {
            const response = await this.get('/programs/trending', {}, 'public');
            return response.body || [];
        } catch (error) {
            console.error('Error fetching trending programs:', error);
            return [];
        }
    }

    /**
     * Get all programs (latest + trending combined)
     * @returns {Promise<Array>} - Combined array of programs
     */
    async getAllPrograms() {
        try {
            const [latestPrograms, trendingPrograms] = await Promise.all([
                this.getLatestPrograms(),
                this.getTrendingPrograms()
            ]);
            
            // Combine and remove duplicates based on programName
            const allPrograms = [...latestPrograms, ...trendingPrograms];
            const uniquePrograms = allPrograms.filter((program, index, self) => 
                index === self.findIndex(p => p.programName === program.programName)
            );
            
            return uniquePrograms;
        } catch (error) {
            console.error('Error fetching all programs:', error);
            return [];
        }
    }

    /**
     * Get program by slug
     * @param {string} slug - Program URL slug
     * @returns {Promise<Object>} - Program details
     */
    async getProgramBySlug(slug) {
        try {
            const response = await this.get(`/programs/${slug}`, {}, 'public');
            return response.body;
        } catch (error) {
            console.error(`Error fetching program ${slug}:`, error);
            return null;
        }
    }

    /**
     * Get all categories with subcategories
     * @returns {Promise<Array>} - Array of categories
     */
    async getCategories() {
        try {
            const response = await this.get('/programs/categories', {}, 'public');
            return response.body || [];
        } catch (error) {
            console.error('Error fetching categories:', error);
            return [];
        }
    }

    // ==================== DONATION APIs ====================

    /**
     * Process donation with file upload support
     * @param {Object} donationData - Donation details
     * @param {File} paymentSlipFile - Payment slip file (optional)
     * @returns {Promise<Object>} - Donation response
     */
    async processDonation(donationData, paymentSlipFile = null) {
        try {
            // Check if user is authenticated
            const token = localStorage.getItem('daana_access_token');
            
            // Create FormData for file upload
            const formData = new FormData();
            // Use programId as campaignId for the API
            formData.append('campaignId', donationData.programId || donationData.campaignId || '1');
            formData.append('actualDonationAmount', donationData.actualDonationAmount.toString());
            formData.append('paymentMethod', donationData.paymentMethod);
            formData.append('comments', donationData.comments || '');
            
            // Add payment slip file if provided
            if (paymentSlipFile) {
                formData.append('paymentSlipUrl', paymentSlipFile);
            }
            
            // Determine endpoint based on authentication
            const endpoint = '/programs/donate';
            const baseType = 'public';
            
            // Prepare request options
            const options = {
                method: 'POST',
                body: formData,
                headers: {}
            };
            
            // Add authorization header if user is authenticated
            if (token) {
                options.headers['Authorization'] = `Bearer ${token}`;
            }
            
            const response = await this.request(endpoint, options, baseType);
            return response;
        } catch (error) {
            console.error('Error processing donation:', error);
            throw error;
        }
    }

    /**
     * Process anonymous donation
     * @param {Object} donationData - Donation details
     * @param {File} paymentSlipFile - Payment slip file (optional)
     * @returns {Promise<Object>} - Donation response
     */
    async processAnonymousDonation(donationData, paymentSlipFile = null) {
        try {
            const formData = new FormData();
            // Use programId as campaignId for the API
            formData.append('campaignId', donationData.programId || donationData.campaignId || '1');
            formData.append('actualDonationAmount', donationData.actualDonationAmount.toString());
            formData.append('paymentMethod', donationData.paymentMethod);
            formData.append('comments', donationData.comments || '');
            
            if (paymentSlipFile) {
                formData.append('paymentSlipUrl', paymentSlipFile);
            }
            
            const response = await this.request('/programs/donate', {
                method: 'POST',
                body: formData,
                headers: {}
            }, 'public');
            
            return response;
        } catch (error) {
            console.error('Error processing anonymous donation:', error);
            throw error;
        }
    }

    /**
     * Process registered donor donation
     * @param {Object} donationData - Donation details
     * @param {File} paymentSlipFile - Payment slip file (optional)
     * @returns {Promise<Object>} - Donation response
     */
    async processRegisteredDonation(donationData, paymentSlipFile = null) {
        try {
            const token = localStorage.getItem('daana_access_token');
            if (!token) {
                throw new Error('Authentication required for registered donor donations');
            }
            
            const formData = new FormData();
            // Use programId as campaignId for the API
            formData.append('campaignId', donationData.programId || donationData.campaignId || '1');
            formData.append('actualDonationAmount', donationData.actualDonationAmount.toString());
            formData.append('paymentMethod', donationData.paymentMethod);
            formData.append('comments', donationData.comments || '');
            
            if (paymentSlipFile) {
                formData.append('paymentSlipUrl', paymentSlipFile);
            }
            
            const response = await this.request('/programs/donate', {
                method: 'POST',
                body: formData,
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            }, 'public');
            
            return response;
        } catch (error) {
            console.error('Error processing registered donation:', error);
            throw error;
        }
    }

    /**
     * Get donation history
     * @param {string} donorEmail - Donor email
     * @returns {Promise<Array>} - Donation history
     */
    async getDonationHistory(donorEmail) {
        try {
            const response = await this.get('/donations/history', { email: donorEmail });
            return response.body || [];
        } catch (error) {
            console.error('Error fetching donation history:', error);
            return [];
        }
    }

    /**
     * Get donor donations with pagination and date filtering
     * @param {number} page - Page number (0-based)
     * @param {number} size - Page size
     * @param {string} startDate - Start date in YYYY/MM/DD format
     * @param {string} endDate - End date in YYYY/MM/DD format
     * @returns {Promise<Object>} - Donation history with pagination
     */
    async getDonorDonations(page = 0, size = 10, startDate = null, endDate = null) {
        try {
            const token = localStorage.getItem('daana_access_token');
            if (!token) {
                throw new Error('Authentication required to fetch donation history');
            }

            const params = { 
                page: page.toString(), 
                size: size.toString() 
            };
            
            // Add date filters if provided
            if (startDate) {
                params.startDate = startDate;
            }
            if (endDate) {
                params.endDate = endDate;
            }
            
            // Build URL with query parameters
            const queryString = new URLSearchParams(params).toString();
            const endpoint = queryString ? `/donor/donations?${queryString}` : '/donor/donations';
            
            const response = await this.request(endpoint, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            }, 'default');
            
            return response;
        } catch (error) {
            console.error('Error fetching donor donations:', error);
            throw error;
        }
    }

    // ==================== AUTHENTICATION APIs ====================

    /**
     * User login
     * @param {Object} loginData - Login credentials
     * @returns {Promise<Object>} - Login response
     */
    async login(loginData) {
        try {
            const response = await this.post('/login', {
                email: loginData.email,
                password: loginData.password
            }, 'auth');
            return response;
        } catch (error) {
            console.error('Error during login:', error);
            throw error;
        }
    }

    /**
     * User signup
     * @param {Object} signupData - Signup data
     * @returns {Promise<Object>} - Signup response
     */
    async signup(signupData) {
        try {
            const response = await this.post('/register', {
                firstName: signupData.firstName,
                lastName: signupData.lastName,
                email: signupData.email,
                password: signupData.password
            }, 'auth');
            return response;
        } catch (error) {
            console.error('Error during signup:', error);
            throw error;
        }
    }

    /**
     * Verify OTP for email verification
     * @param {string} email - User email
     * @param {string} otpCode - OTP code
     * @returns {Promise<Object>} - Verification response
     */
    async verifyOtp(email, otpCode) {
        try {
            const encodedEmail = encodeURIComponent(email);
            const response = await this.request(`/verify-otp?email=${encodedEmail}&otpCode=${otpCode}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            }, 'auth');
            return response;
        } catch (error) {
            console.error('Error during OTP verification:', error);
            throw error;
        }
    }

    /**
     * Resend OTP for email verification
     * @param {string} email - User email
     * @returns {Promise<Object>} - Resend response
     */
    async resendOtp(email) {
        try {
            const encodedEmail = encodeURIComponent(email);
            const response = await this.request(`/resend-otp?email=${encodedEmail}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            }, 'auth');
            return response;
        } catch (error) {
            console.error('Error during OTP resend:', error);
            throw error;
        }
    }

    /**
     * Refresh access token
     * @param {string} refreshToken - Refresh token
     * @returns {Promise<Object>} - Token refresh response
     */
    async refreshToken(refreshToken) {
        try {
            const response = await this.post('/refresh', {
                refreshToken: refreshToken
            }, 'auth');
            return response;
        } catch (error) {
            console.error('Error refreshing token:', error);
            throw error;
        }
    }

    /**
     * Logout user
     * @param {string} token - Access token
     * @returns {Promise<Object>} - Logout response
     */
    async logout(token) {
        try {
            const response = await this.post('/logout', {}, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            }, 'auth');
            return response;
        } catch (error) {
            console.error('Error during logout:', error);
            throw error;
        }
    }

    // ==================== CONTACT & NEWSLETTER APIs ====================

    /**
     * Submit contact form
     * @param {Object} contactData - Contact form data
     * @returns {Promise<Object>} - Contact response
     */
    async submitContact(contactData) {
        try {
            const response = await this.post('/contact', {
                ...contactData,
                timestamp: new Date().toISOString()
            });
            return response;
        } catch (error) {
            console.error('Error submitting contact form:', error);
            throw error;
        }
    }

    /**
     * Subscribe to newsletter
     * @param {string} email - Email address
     * @returns {Promise<Object>} - Subscription response
     */
    async subscribeNewsletter(email) {
        try {
            const response = await this.post('/newsletter', { email });
            return response;
        } catch (error) {
            console.error('Error subscribing to newsletter:', error);
            throw error;
        }
    }

    // ==================== STATISTICS APIs ====================

    /**
     * Get platform statistics
     * @returns {Promise<Object>} - Platform stats
     */
    async getStatistics() {
        try {
            const response = await this.get('/statistics', {}, 'public');
            return response.body || {};
        } catch (error) {
            console.error('Error fetching statistics:', error);
            return {};
        }
    }

    /**
     * Get program statistics
     * @param {string} programSlug - Program slug (optional)
     * @returns {Promise<Object>} - Program stats
     */
    async getProgramStatistics(programSlug = null) {
        try {
            const endpoint = programSlug ? `/statistics/programs/${programSlug}` : '/statistics/programs';
            const response = await this.get(endpoint, {}, 'public');
            return response.body || {};
        } catch (error) {
            console.error('Error fetching program statistics:', error);
            return {};
        }
    }

    // ==================== CHARITY REGISTRATION APIs ====================

    /**
     * Charity registration step 1 - Basic information
     * @param {Object} charityData - Charity basic information
     * @returns {Promise<Object>} - Registration response
     */
    async charityRegisterStep1(charityData) {
        try {
            const response = await this.post('/charity/register/step1', charityData, 'public');
            return response;
        } catch (error) {
            console.error('Error in charity registration step 1:', error);
            throw error;
        }
    }

    /**
     * Charity registration step 2 - Documents and banking
     * @param {FormData} formData - Form data with files and banking info
     * @returns {Promise<Object>} - Registration response
     */
    async charityRegisterStep2(formData) {
        try {
            const response = await this.request('/charity/register/step2', {
                method: 'POST',
                body: formData,
                headers: {} // Let browser set Content-Type for FormData
            }, 'public');
            return response;
        } catch (error) {
            console.error('Error in charity registration step 2:', error);
            throw error;
        }
    }

    /**
     * Charity registration step 3 - Email verification
     * @param {Object} verificationData - OTP verification data
     * @returns {Promise<Object>} - Verification response
     */
    async charityRegisterStep3(verificationData) {
        try {
            const response = await this.post('/charity/register/step3', verificationData, 'public');
            return response;
        } catch (error) {
            console.error('Error in charity registration step 3:', error);
            throw error;
        }
    }

    /**
     * Resend OTP for charity registration
     * @param {string} email - Email address
     * @returns {Promise<Object>} - Resend response
     */
    async charityResendOtp(email) {
        try {
            const encodedEmail = encodeURIComponent(email);
            const response = await this.request(`/charity/register/resend-otp?email=${encodedEmail}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            }, 'public');
            return response;
        } catch (error) {
            console.error('Error resending charity registration OTP:', error);
            throw error;
        }
    }

    // ==================== DONOR PROFILE APIs ====================

    /**
     * Get donor profile
     * @returns {Promise<Object>} - Donor profile data
     */
    async getDonorProfile() {
        try {
            const token = localStorage.getItem('daana_access_token');
            if (!token) {
                throw new Error('Authentication required');
            }

            const response = await this.request('/donor/profile', {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            }, 'default');
            
            return response;
        } catch (error) {
            console.error('Error fetching donor profile:', error);
            throw error;
        }
    }

    /**
     * Update donor profile
     * @param {Object} profileData - Profile data to update
     * @returns {Promise<Object>} - Updated profile data
     */
    async updateDonorProfile(profileData) {
        try {
            const token = localStorage.getItem('daana_access_token');
            if (!token) {
                throw new Error('Authentication required');
            }

            const response = await this.request('/donor/profile', {
                method: 'PUT',
                body: JSON.stringify(profileData),
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            }, 'default');
            
            return response;
        } catch (error) {
            console.error('Error updating donor profile:', error);
            throw error;
        }
    }

    /**
     * Upload donor profile picture
     * @param {File} imageFile - Image file to upload
     * @returns {Promise<Object>} - Upload response with image URL
     */
    async uploadDonorProfilePicture(imageFile) {
        try {
            const token = localStorage.getItem('daana_access_token');
            if (!token) {
                throw new Error('Authentication required');
            }

            const formData = new FormData();
            formData.append('profileImage', imageFile);

            const response = await this.request('/donor/profile/picture', {
                method: 'POST',
                body: formData,
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            }, 'default');
            
            return response;
        } catch (error) {
            console.error('Error uploading profile picture:', error);
            throw error;
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Format currency for display
     * @param {number} amount - Amount to format
     * @param {string} currency - Currency code (default: LKR)
     * @returns {string} - Formatted currency string
     */
    formatCurrency(amount, currency = 'LKR') {
        return new Intl.NumberFormat('en-LK', {
            style: 'currency',
            currency: currency,
            minimumFractionDigits: 0,
            maximumFractionDigits: 0
        }).format(amount);
    }

    /**
     * Calculate progress percentage
     * @param {number} raised - Amount raised
     * @param {number} target - Target amount
     * @returns {number} - Progress percentage
     */
    calculateProgress(raised, target) {
        if (target === 0) return 0;
        return Math.min(Math.round((raised / target) * 100), 100);
    }

    /**
     * Format program data for UI display
     * @param {Object} program - Raw program data from API
     * @returns {Object} - Formatted program data
     */
    formatProgramForUI(program) {
        // Handle both old and new API response formats
        const urlSlug = program.urlSlug || program.urlName;
        const title = program.programTitle || program.title || program.programName;
        const name = program.programName;
        const description = program.programDescription || program.description;
        const category = program.subCategoryName || program.subCategory?.name || program.category?.name;
        const location = program.location || program.programLocation;
        const image = program.programImageUrl || program.programImage;
        const target = program.targetDonationAmount || program.target;
        const raised = program.raised;
        const programId = program.programId; // Keep the original programId for API calls
        
        return {
            id: programId, // Use programId as the main ID
            programId: programId, // Keep programId for donation API
            urlSlug: urlSlug,
            title: title,
            name: name,
            description: description,
            category: category,
            location: location,
            image: image,
            target: target,
            raised: raised,
            progress: this.calculateProgress(raised, target),
            formattedTarget: this.formatCurrency(target),
            formattedRaised: this.formatCurrency(raised)
        };
    }
}

// Create and export singleton instance
const apiService = new ApiService();

// Export for use in other files
if (typeof module !== 'undefined' && module.exports) {
    module.exports = apiService;
} else {
    window.apiService = apiService;
}
