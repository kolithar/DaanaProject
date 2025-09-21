/**
 * Donation History Management
 * Handles donation history data loading, filtering, and display
 */

class DonationHistoryManager {
    constructor() {
        this.apiService = window.apiService;
        this.donationData = null;
        this.currentPage = 0;
        this.pageSize = 10;
        this.filteredDonations = [];
        this.currentStartDate = null;
        this.currentEndDate = null;
        
        this.init();
    }

    async init() {
        // Check authentication
        if (!this.checkAuthentication()) {
            return;
        }

        // Load donation history
        await this.loadDonationHistory();
        
        // Setup event listeners
        this.setupEventListeners();
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

    async loadDonationHistory(page = 0, startDate = null, endDate = null) {
        try {
            this.currentPage = page;
            this.currentStartDate = startDate;
            this.currentEndDate = endDate;
            this.showLoading(true);
            
            // Convert dates to YYYY/MM/DD format if provided
            let formattedStartDate = null;
            let formattedEndDate = null;
            
            if (startDate) {
                formattedStartDate = this.formatDateForAPI(startDate);
            }
            if (endDate) {
                formattedEndDate = this.formatDateForAPI(endDate);
            }
            
            const response = await this.apiService.getDonorDonations(
                page, 
                this.pageSize, 
                formattedStartDate, 
                formattedEndDate
            );
            
            if (response.success && response.body) {
                this.donationData = response.body;
                this.filteredDonations = response.body.content || [];
                this.updateDonationStats();
                this.displayDonations();
                this.updatePagination();
            } else {
                throw new Error('Failed to load donation history');
            }
        } catch (error) {
            console.error('Error loading donation history:', error);
            this.showDonationError('Failed to load donation history. Please try again.');
        } finally {
            this.showLoading(false);
        }
    }

    formatDateForAPI(dateString) {
        // Convert YYYY-MM-DD to YYYY/MM/DD format
        if (dateString) {
            return dateString.replace(/-/g, '/');
        }
        return null;
    }

    updateDonationStats() {
        if (!this.donationData || !this.donationData.content) return;

        const donations = this.donationData.content;
        const totalDonations = donations.length;
        const totalAmount = donations.reduce((sum, donation) => sum + donation.actualDonationAmount, 0);
        const uniqueCampaigns = new Set(donations.map(donation => donation.campaignId)).size;
        
        // Get last donation date
        let lastDonationDate = 'No donations';
        if (donations.length > 0) {
            const sortedDonations = [...donations].sort((a, b) => new Date(b.created) - new Date(a.created));
            const lastDonation = sortedDonations[0];
            const date = new Date(lastDonation.created);
            lastDonationDate = date.toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'short',
                day: 'numeric'
            });
        }

        document.getElementById('totalDonations').textContent = totalDonations;
        document.getElementById('totalAmount').textContent = `Rs. ${totalAmount.toLocaleString()}`;
        document.getElementById('campaignsSupported').textContent = uniqueCampaigns;
        document.getElementById('lastDonation').textContent = lastDonationDate;
    }

    displayDonations() {
        const donationList = document.getElementById('donationList');
        
        if (!this.filteredDonations || this.filteredDonations.length === 0) {
            donationList.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-heart"></i>
                    <h3>No donations found</h3>
                    <p>You haven't made any donations yet, or they don't match your current filters.</p>
                    <button class="btn btn-primary" onclick="window.location.href='index.html#donate'">
                        <i class="fas fa-heart"></i> Make Your First Donation
                    </button>
                </div>
            `;
            return;
        }

        const donationsHTML = this.filteredDonations.map(donation => this.createDonationCard(donation)).join('');
        donationList.innerHTML = donationsHTML;
    }

    createDonationCard(donation) {
        const statusClass = donation.status.toLowerCase();
        const statusIcon = this.getStatusIcon(donation.status);
        const paymentIcon = donation.paymentMethod === 'CARD' ? 'fas fa-credit-card' : 'fas fa-university';
        
        return `
            <div class="donation-card">
                <div class="donation-header">
                    <div class="donation-status">
                        <i class="${statusIcon}"></i>
                        <span class="status-${statusClass}">${donation.status}</span>
                    </div>
                    <div class="donation-date">
                        ${donation.donationDateFormatted}
                    </div>
                </div>
                
                <div class="donation-content">
                    <div class="campaign-info">
                        <img src="${donation.campaignImage}" alt="${donation.campaignName}" class="campaign-image" onerror="this.src='resources/logo/Danna-logo.webp'">
                        <div class="campaign-details">
                            <h4>${donation.campaignTitle}</h4>
                            <p class="charity-name">
                                <img src="${donation.charityLogo}" alt="${donation.charityName}" class="charity-logo" onerror="this.style.display='none'">
                                ${donation.charityName}
                            </p>
                            <p class="category">${donation.categoryName} • ${donation.subCategoryName}</p>
                        </div>
                    </div>
                    
                    <div class="donation-amounts">
                        <div class="amount-item">
                            <label>Donation Amount:</label>
                            <span class="amount">Rs. ${donation.actualDonationAmount.toLocaleString()}</span>
                        </div>
                        <div class="amount-item">
                            <label>Service Charge:</label>
                            <span class="service-charge">Rs. ${donation.serviceCharge.toLocaleString()}</span>
                        </div>
                        <div class="amount-item total">
                            <label>Net Amount:</label>
                            <span class="net-amount">Rs. ${donation.netDonationAmount.toLocaleString()}</span>
                        </div>
                    </div>
                    
                    <div class="donation-meta">
                        <div class="payment-info">
                            <i class="${paymentIcon}"></i>
                            <span>${donation.paymentMethod.replace('_', ' ')}</span>
                            <span class="reference">Ref: ${donation.paymentReferenceNumber}</span>
                        </div>
                        ${donation.isAnonymousDonation ? '<span class="anonymous-badge"><i class="fas fa-user-secret"></i> Anonymous</span>' : ''}
                    </div>
                    
                    ${donation.comments ? `
                        <div class="donation-comments">
                            <i class="fas fa-comment"></i>
                            <p>"${donation.comments}"</p>
                        </div>
                    ` : ''}
                    
                    ${donation.paymentSlipUrl ? `
                        <div class="donation-actions">
                            <a href="${donation.paymentSlipUrl}" target="_blank" class="btn btn-outline btn-sm">
                                <i class="fas fa-file-pdf"></i> View Receipt
                            </a>
                        </div>
                    ` : ''}
                </div>
            </div>
        `;
    }

    getStatusIcon(status) {
        switch (status) {
            case 'ACTIVE':
                return 'fas fa-check-circle';
            case 'PENDING':
                return 'fas fa-clock';
            case 'COMPLETED':
                return 'fas fa-check-double';
            default:
                return 'fas fa-question-circle';
        }
    }

    updatePagination() {
        if (!this.donationData) return;

        const pagination = document.getElementById('donationPagination');
        const prevBtn = document.getElementById('prevPageBtn');
        const nextBtn = document.getElementById('nextPageBtn');
        const pageInfo = document.getElementById('pageInfo');

        if (this.donationData.totalPages <= 1) {
            pagination.style.display = 'none';
            return;
        }

        pagination.style.display = 'flex';
        
        const currentPage = this.donationData.number + 1;
        const totalPages = this.donationData.totalPages;
        
        pageInfo.textContent = `Page ${currentPage} of ${totalPages}`;
        
        prevBtn.disabled = this.donationData.first;
        nextBtn.disabled = this.donationData.last;
    }

    filterDonations() {
        // This function is kept for compatibility but no longer performs filtering
        // Date filtering is handled by applyDateFilters() which reloads data from server
        if (!this.donationData || !this.donationData.content) return;
        
        this.filteredDonations = [...this.donationData.content];
        this.displayDonations();
    }

    clearFilters() {
        // Clear date filters
        document.getElementById('startDate').value = '';
        document.getElementById('endDate').value = '';
        
        // Reset date tracking
        this.currentStartDate = null;
        this.currentEndDate = null;
        
        // Reload data without date filters
        this.loadDonationHistory(0, null, null);
    }

    setQuickDateRange(range) {
        const today = new Date();
        let startDate = null;
        let endDate = null;

        switch (range) {
            case 'today':
                startDate = today.toISOString().split('T')[0];
                endDate = today.toISOString().split('T')[0];
                break;
            case 'week':
                const weekStart = new Date(today);
                weekStart.setDate(today.getDate() - today.getDay());
                startDate = weekStart.toISOString().split('T')[0];
                endDate = today.toISOString().split('T')[0];
                break;
            case 'month':
                const monthStart = new Date(today.getFullYear(), today.getMonth(), 1);
                startDate = monthStart.toISOString().split('T')[0];
                endDate = today.toISOString().split('T')[0];
                break;
            case 'year':
                const yearStart = new Date(today.getFullYear(), 0, 1);
                startDate = yearStart.toISOString().split('T')[0];
                endDate = today.toISOString().split('T')[0];
                break;
        }

        // Update date inputs
        document.getElementById('startDate').value = startDate;
        document.getElementById('endDate').value = endDate;

        // Apply the date filter
        this.applyDateFilters();
    }

    applyDateFilters() {
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;

        // Validate date range
        if (startDate && endDate && startDate > endDate) {
            alert('Start date cannot be later than end date');
            document.getElementById('endDate').value = '';
            return;
        }

        // Show loading indicator
        this.showDateFilterIndicator(startDate || endDate);

        // Reload data with new date filters
        this.loadDonationHistory(0, startDate || null, endDate || null);
    }

    showDateFilterIndicator(hasDateFilter) {
        const filterSection = document.querySelector('.filter-section');
        if (hasDateFilter) {
            filterSection.classList.add('date-filter-active');
        } else {
            filterSection.classList.remove('date-filter-active');
        }
    }

    showDonationError(message) {
        const donationList = document.getElementById('donationList');
        donationList.innerHTML = `
            <div class="error-state">
                <i class="fas fa-exclamation-triangle"></i>
                <h3>Error Loading Donations</h3>
                <p>${message}</p>
                <button class="btn btn-primary" onclick="window.donationHistoryManager.loadDonationHistory()">
                    <i class="fas fa-refresh"></i> Try Again
                </button>
            </div>
        `;
    }

    showLoading(show, message = 'Loading donation history...') {
        const overlay = document.getElementById('loadingOverlay');
        const loadingText = overlay.querySelector('p');
        
        loadingText.textContent = message;
        overlay.style.display = show ? 'flex' : 'none';
    }

    setupEventListeners() {
        // Add any additional event listeners if needed
    }
}

// Global functions for HTML event handlers
function filterDonations() {
    if (window.donationHistoryManager) {
        window.donationHistoryManager.filterDonations();
    }
}

function clearFilters() {
    if (window.donationHistoryManager) {
        window.donationHistoryManager.clearFilters();
    }
}

function applyDateFilters() {
    if (window.donationHistoryManager) {
        window.donationHistoryManager.applyDateFilters();
    }
}

function setQuickDateRange(range) {
    if (window.donationHistoryManager) {
        window.donationHistoryManager.setQuickDateRange(range);
    }
}

function loadDonationPage(direction) {
    if (window.donationHistoryManager) {
        const currentPage = window.donationHistoryManager.currentPage;
        const totalPages = window.donationHistoryManager.donationData?.totalPages || 0;
        
        let newPage = currentPage;
        if (direction === 'next' && currentPage < totalPages - 1) {
            newPage = currentPage + 1;
        } else if (direction === 'prev' && currentPage > 0) {
            newPage = currentPage - 1;
        }
        
        if (newPage !== currentPage) {
            window.donationHistoryManager.loadDonationHistory(
                newPage, 
                window.donationHistoryManager.currentStartDate, 
                window.donationHistoryManager.currentEndDate
            );
        }
    }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.donationHistoryManager = new DonationHistoryManager();
});
