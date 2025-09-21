/**
 * Configuration file for Daana.lk
 * Environment-specific settings
 */

const CONFIG = {
    // Environment detection
    isDevelopment: window.location.hostname === 'localhost' || 
                  window.location.hostname === '127.0.0.1' ||
                  window.location.hostname.includes('localhost'),
    
    // API Configuration
    api: {
        // Development URLs
        development: {
            baseURL: 'http://localhost:8080/api/v1',
            publicBaseURL: 'http://localhost:8080/api/v1/public',
            authBaseURL: 'http://localhost:8080/api/v1/auth'
        },
        
        // Production URLs (update these with your production domain)
        production: {
            baseURL: '/api/v1',
            publicBaseURL: '/api/v1/public', 
            authBaseURL: '/api/v1/auth'
        }
    },
    
    // Authentication settings
    auth: {
        tokenKey: 'daana_access_token',
        refreshTokenKey: 'daana_refresh_token',
        userKey: 'daana_user',
        tokenTypeKey: 'daana_token_type',
        expiresInKey: 'daana_expires_in'
    },
    
    // App settings
    app: {
        name: 'Daana.lk',
        version: '1.0.0',
        timeout: 10000
    }
};

// Get current API configuration based on environment
CONFIG.getApiConfig = function() {
    return this.isDevelopment ? this.api.development : this.api.production;
};

// Log current configuration
console.log('🔧 Daana.lk Configuration Loaded');
console.log('Environment:', CONFIG.isDevelopment ? 'Development' : 'Production');
console.log('API Config:', CONFIG.getApiConfig());

// Export for use in other files
if (typeof module !== 'undefined' && module.exports) {
    module.exports = CONFIG;
} else {
    window.CONFIG = CONFIG;
}
