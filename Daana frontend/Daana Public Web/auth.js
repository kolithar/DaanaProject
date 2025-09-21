/**
 * Authentication JavaScript for Daana.lk
 * Handles login, signup, and authentication state management
 */

// Authentication state
let authState = {
    isAuthenticated: false,
    user: null,
    token: null,
    refreshToken: null
};

// Environment detection
const isDevelopment = window.location.hostname === 'localhost' || 
                     window.location.hostname === '127.0.0.1' ||
                     window.location.hostname.includes('localhost');

// DOM Content Loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeAuth();
});

// Initialize Authentication
function initializeAuth() {
    // Check if user is already logged in
    checkAuthState();
    
    // Setup form event listeners
    setupAuthForms();
    
    // Setup navigation
    setupAuthNavigation();
}

// Check Authentication State
function checkAuthState() {
    const token = localStorage.getItem('daana_access_token');
    const user = localStorage.getItem('daana_user');
    
    if (token && user) {
        try {
            authState.isAuthenticated = true;
            authState.token = token;
            authState.user = JSON.parse(user);
            updateAuthUI();
        } catch (error) {
            console.error('Error parsing user data:', error);
            clearAuthState();
        }
    }
}

// Check if token is expired
function isTokenExpired() {
    const expiresIn = localStorage.getItem('daana_expires_in');
    if (!expiresIn) return true;
    
    const tokenTimestamp = parseInt(expiresIn);
    const currentTimestamp = Math.floor(Date.now() / 1000);
    
    return currentTimestamp >= tokenTimestamp;
}

// Validate current token
function validateToken() {
    const token = localStorage.getItem('daana_access_token');
    if (!token) return false;
    
    if (isTokenExpired()) {
        console.log('Token expired, clearing auth state');
        clearAuthState();
        return false;
    }
    
    return true;
}

// Setup Authentication Forms
function setupAuthForms() {
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
}

// Handle Login Form Submission
async function handleLogin(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const loginData = {
        email: formData.get('email'),
        password: formData.get('password'),
        rememberMe: formData.get('rememberMe') === 'on'
    };
    
    try {
        showLoginLoading(true);
        
        const response = await apiService.login(loginData);
        
        if (response.success !== false) {
            // Store authentication data
            authState.isAuthenticated = true;
            authState.token = response.accessToken;
            authState.refreshToken = response.refreshToken;
            authState.user = {
                email: response.email,
                fullName: response.fullName,
                userType: response.userType,
                userId: response.userId,
                jti: response.jti,
                domain: response.domain
            };
            
            // Save to localStorage
            localStorage.setItem('daana_access_token', response.accessToken);
            localStorage.setItem('daana_refresh_token', response.refreshToken);
            localStorage.setItem('daana_user', JSON.stringify(authState.user));
            localStorage.setItem('daana_token_type', response.tokenType || 'bearer');
            localStorage.setItem('daana_expires_in', response.expiresIn || 86400);
            
            // Update UI
            updateAuthUI();
            
            // Show success message
            showMessage('Login successful! Welcome back, ' + response.fullName, 'success');
            
            // Redirect to home page after a short delay
            setTimeout(() => {
                window.location.href = 'index.html';
            }, 1500);
            
        } else {
            throw new Error(response.message || 'Login failed');
        }
        
    } catch (error) {
        console.error('Login error:', error);
        showMessage(error.message || 'Login failed. Please check your credentials and try again.', 'error');
    } finally {
        showLoginLoading(false);
    }
}

// Show/Hide Login Loading State
function showLoginLoading(show) {
    const submitBtn = document.querySelector('#loginForm button[type="submit"]');
    const btnText = submitBtn.querySelector('.btn-text');
    const btnLoading = submitBtn.querySelector('.btn-loading');
    
    if (show) {
        btnText.style.display = 'none';
        btnLoading.style.display = 'flex';
        submitBtn.disabled = true;
    } else {
        btnText.style.display = 'block';
        btnLoading.style.display = 'none';
        submitBtn.disabled = false;
    }
}

// Toggle Password Visibility
function togglePassword() {
    const passwordInput = document.getElementById('password');
    const toggleIcon = document.getElementById('passwordToggleIcon');
    
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

// Update Authentication UI
function updateAuthUI() {
    const navAuth = document.querySelector('.nav-auth');
    
    console.log('updateAuthUI called');
    console.log('Auth State:', authState);
    console.log('Nav Auth Element:', navAuth);
    
    if (authState.isAuthenticated && authState.user && navAuth) {
        console.log('✅ Showing user profile');
        // Get user initials for avatar
        const initials = getInitials(authState.user.fullName || authState.user.email);
        
        navAuth.innerHTML = `
            <div class="user-profile" onclick="toggleUserMenu()">
                <div class="user-avatar">${initials}</div>
                <div class="user-details">
                    <span class="user-name">${authState.user.fullName || authState.user.email}</span>
                    <span class="user-type">${authState.user.userType || 'User'}</span>
                </div>
            </div>
            <div class="user-menu" id="userMenu" style="display: none;">
                <div class="user-info">
                    <span class="user-name">${authState.user.fullName || authState.user.email}</span>
                    <span class="user-type">${authState.user.userType || 'User'}</span>
                </div>
                <div class="user-actions">
                    <a href="donor-profile.html" class="btn btn-outline">Profile Manage</a>
                    <a href="donation-history.html" class="btn btn-outline">Donation History</a>
                    <button onclick="logout()" class="btn btn-primary">Logout</button>
                </div>
            </div>
        `;
        console.log('✅ User profile HTML set');
    } else if (navAuth) {
        console.log('❌ Showing login/signup buttons');
        // Show login/signup buttons when not authenticated
        navAuth.innerHTML = `
            <a href="login.html" class="btn btn-outline">Login</a>
            <a href="signup.html" class="btn btn-primary">Sign Up</a>
        `;
    } else {
        console.log('❌ Nav auth element not found');
    }
}

// Get user initials for avatar
function getInitials(name) {
    if (!name) return 'U';
    
    const words = name.trim().split(' ');
    if (words.length === 1) {
        return words[0].charAt(0).toUpperCase();
    } else {
        return (words[0].charAt(0) + words[words.length - 1].charAt(0)).toUpperCase();
    }
}

// Toggle user menu
function toggleUserMenu() {
    const userMenu = document.getElementById('userMenu');
    if (userMenu) {
        userMenu.style.display = userMenu.style.display === 'none' ? 'block' : 'none';
    }
}

// Close user menu when clicking outside
document.addEventListener('click', function(event) {
    const userProfile = document.querySelector('.user-profile');
    const userMenu = document.getElementById('userMenu');
    
    if (userProfile && userMenu && !userProfile.contains(event.target) && !userMenu.contains(event.target)) {
        userMenu.style.display = 'none';
    }
});

// Setup Authentication Navigation
function setupAuthNavigation() {
    const hamburger = document.querySelector('.hamburger');
    const navMenu = document.querySelector('.nav-menu');
    const navLinks = document.querySelectorAll('.nav-link');

    if (hamburger && navMenu) {
        // Mobile menu toggle
        hamburger.addEventListener('click', () => {
            hamburger.classList.toggle('active');
            navMenu.classList.toggle('active');
        });

        // Close mobile menu when clicking on a link
        navLinks.forEach(link => {
            link.addEventListener('click', () => {
                hamburger.classList.remove('active');
                navMenu.classList.remove('active');
            });
        });
    }
}

// Logout Function
async function logout() {
    try {
        // Try to call logout API if we have a token
        const token = authState.token;
        if (token) {
            await apiService.logout(token);
        }
    } catch (error) {
        console.error('Error calling logout API:', error);
        // Continue with local logout even if API call fails
    }
    
    // Clear authentication state
    clearAuthState();
    
    // Clear all localStorage data
    localStorage.removeItem('daana_access_token');
    localStorage.removeItem('daana_refresh_token');
    localStorage.removeItem('daana_user');
    localStorage.removeItem('daana_token_type');
    localStorage.removeItem('daana_expires_in');
    
    // Update UI
    updateAuthUI();
    
    // Show message
    showMessage('You have been logged out successfully.', 'success');
    
    // Redirect to home page
    setTimeout(() => {
        window.location.href = 'index.html';
    }, 1000);
}

// Clear Authentication State
function clearAuthState() {
    authState.isAuthenticated = false;
    authState.user = null;
    authState.token = null;
    authState.refreshToken = null;
}

// Show Message Function
function showMessage(message, type) {
    const messageDiv = document.createElement('div');
    messageDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 1rem 2rem;
        border-radius: 8px;
        color: white;
        font-weight: 500;
        z-index: 10001;
        max-width: 400px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        transform: translateX(100%);
        transition: transform 0.3s ease;
    `;
    
    messageDiv.style.background = type === 'success' ? '#28a745' : '#dc3545';
    messageDiv.textContent = message;
    
    document.body.appendChild(messageDiv);
    
    // Animate in
    setTimeout(() => {
        messageDiv.style.transform = 'translateX(0)';
    }, 100);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        messageDiv.style.transform = 'translateX(100%)';
        setTimeout(() => {
            if (messageDiv.parentNode) {
                messageDiv.parentNode.removeChild(messageDiv);
            }
        }, 300);
    }, 5000);
}

// Test function to verify localStorage data
function testStorage() {
    console.log('=== AUTHENTICATION STORAGE TEST ===');
    console.log('Environment:', isDevelopment ? '🔧 Development' : '🚀 Production');
    console.log('Current URL:', window.location.href);
    console.log('Access Token:', localStorage.getItem('daana_access_token') ? '✅ Saved' : '❌ Not found');
    console.log('Refresh Token:', localStorage.getItem('daana_refresh_token') ? '✅ Saved' : '❌ Not found');
    console.log('User Data:', localStorage.getItem('daana_user') ? '✅ Saved' : '❌ Not found');
    console.log('Token Type:', localStorage.getItem('daana_token_type') || 'Not set');
    console.log('Expires In:', localStorage.getItem('daana_expires_in') || 'Not set');
    
    const userData = localStorage.getItem('daana_user');
    if (userData) {
        try {
            const user = JSON.parse(userData);
            console.log('User Details:', user);
        } catch (error) {
            console.error('Error parsing user data:', error);
        }
    }
    console.log('=====================================');
}

// Function to check environment and API configuration
function checkEnvironment() {
    console.log('=== ENVIRONMENT CHECK ===');
    console.log('Current Hostname:', window.location.hostname);
    console.log('Environment:', isDevelopment ? 'Development' : 'Production');
    console.log('API Service Available:', typeof apiService !== 'undefined' ? '✅ Yes' : '❌ No');
    
    if (typeof apiService !== 'undefined') {
        console.log('API Base URLs:', {
            base: apiService.baseURL,
            public: apiService.publicBaseURL,
            auth: apiService.authBaseURL
        });
    }
    console.log('========================');
}

// Debug function to check authentication state
function debugAuth() {
    console.log('=== AUTH DEBUG ===');
    console.log('Auth State:', authState);
    console.log('Is Authenticated:', authState.isAuthenticated);
    console.log('User:', authState.user);
    
    const navAuth = document.querySelector('.nav-auth');
    console.log('Nav Auth Element:', navAuth);
    console.log('Nav Auth HTML:', navAuth ? navAuth.innerHTML : 'Not found');
    
    const token = localStorage.getItem('daana_access_token');
    const user = localStorage.getItem('daana_user');
    console.log('Stored Token:', token ? 'Found' : 'Not found');
    console.log('Stored User:', user ? 'Found' : 'Not found');
    
    if (user) {
        try {
            const userData = JSON.parse(user);
            console.log('Parsed User Data:', userData);
        } catch (error) {
            console.error('Error parsing user:', error);
        }
    }
    console.log('================');
}

// Force update UI function
function forceUpdateUI() {
    console.log('Forcing UI update...');
    updateAuthUI();
    console.log('UI update completed');
}

// Force show user profile (for testing)
function forceShowProfile() {
    const navAuth = document.querySelector('.nav-auth');
    if (navAuth) {
        const userProfile = navAuth.querySelector('.user-profile');
        if (userProfile) {
            userProfile.style.display = 'flex';
            userProfile.style.visibility = 'visible';
            userProfile.style.opacity = '1';
            console.log('✅ User profile forced to show');
        } else {
            console.log('❌ User profile element not found');
        }
    } else {
        console.log('❌ Nav auth element not found');
    }
}

// Utility functions for token management
function getAuthToken() {
    if (validateToken()) {
        return localStorage.getItem('daana_access_token');
    }
    return null;
}

function testLocalStorage() {
    console.log('=== Testing localStorage ===');
    console.log('Access Token:', localStorage.getItem('daana_access_token') ? '✅ Saved' : '❌ Not found');
    console.log('Refresh Token:', localStorage.getItem('daana_refresh_token') ? '✅ Saved' : '❌ Not found');
    console.log('User Data:', localStorage.getItem('daana_user') ? '✅ Saved' : '❌ Not found');
    console.log('Token Type:', localStorage.getItem('daana_token_type') || 'Not set');
    console.log('Expires In:', localStorage.getItem('daana_expires_in') || 'Not set');
    console.log('Token Valid:', validateToken() ? '✅ Valid' : '❌ Invalid/Expired');
    
    const userData = localStorage.getItem('daana_user');
    if (userData) {
        console.log('User Details:', JSON.parse(userData));
    }
    
    // Show token info (first 20 chars for security)
    const token = localStorage.getItem('daana_access_token');
    if (token) {
        console.log('Token Preview:', token.substring(0, 20) + '...');
    }
}

// Test donation history API with current token
function testDonationHistoryAPI() {
    const token = getAuthToken();
    if (!token) {
        console.log('❌ No valid token found. Please login first.');
        return;
    }
    
    console.log('🔍 Testing Donation History API...');
    console.log('Using token:', token.substring(0, 20) + '...');
    
    // Test API call
    fetch('http://localhost:8080/api/v1/donor/donations?startDate=2024/01/01&endDate=2025/12/01&page=0&size=10', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        console.log('API Response Status:', response.status);
        if (response.ok) {
            return response.json();
        } else {
            throw new Error(`API Error: ${response.status} ${response.statusText}`);
        }
    })
    .then(data => {
        console.log('✅ API Success:', data);
        if (data.success && data.body && data.body.content) {
            console.log(`Found ${data.body.content.length} donations`);
            console.log('Sample donation:', data.body.content[0]);
        }
    })
    .catch(error => {
        console.error('❌ API Error:', error);
    });
}

// Export functions for global access
window.togglePassword = togglePassword;
window.logout = logout;
window.toggleUserMenu = toggleUserMenu;
window.testStorage = testStorage;
window.checkEnvironment = checkEnvironment;
window.debugAuth = debugAuth;
window.forceUpdateUI = forceUpdateUI;
window.forceShowProfile = forceShowProfile;
window.getAuthToken = getAuthToken;
window.testLocalStorage = testLocalStorage;
window.validateToken = validateToken;
window.testDonationHistoryAPI = testDonationHistoryAPI;
