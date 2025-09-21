/**
 * Search Programs Page JavaScript
 * Handles program search, filtering, and pagination functionality
 */

// Global variables
let currentPrograms = [];
let filteredPrograms = [];
let currentPage = 0;
let totalPages = 0;
let pageSize = 12;
let categories = [];
let subCategories = [];
let currentFilters = {
    searchText: '',
    categoryId: null,
    subCategoryId: null
};

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    initializeSearchPage();
});

// Initialize search page functionality
async function initializeSearchPage() {
    try {
        setupNavigation();
        setupEventListeners();
        await loadCategories();
        loadFiltersFromURL();
        await loadInitialPrograms();
    } catch (error) {
        console.error('Error initializing search page:', error);
        showError('Failed to initialize search page. Please refresh and try again.');
    }
}

// Setup navigation functionality
function setupNavigation() {
    const hamburger = document.querySelector('.hamburger');
    const navMenu = document.querySelector('.nav-menu');

    if (hamburger && navMenu) {
        hamburger.addEventListener('click', () => {
            hamburger.classList.toggle('active');
            navMenu.classList.toggle('active');
        });

        // Close mobile menu when clicking on a link
        const navLinks = document.querySelectorAll('.nav-link');
        navLinks.forEach(link => {
            link.addEventListener('click', () => {
                hamburger.classList.remove('active');
                navMenu.classList.remove('active');
            });
        });
    }
}

// Setup event listeners
function setupEventListeners() {
    // Search input with debouncing
    const searchInput = document.getElementById('searchText');
    if (searchInput) {
        let searchTimeout;
        searchInput.addEventListener('input', (e) => {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                currentFilters.searchText = e.target.value;
                searchPrograms();
            }, 500);
        });
    }

    // Category filter change
    const categoryFilter = document.getElementById('categoryFilter');
    if (categoryFilter) {
        categoryFilter.addEventListener('change', (e) => {
            currentFilters.categoryId = e.target.value || null;
            updateSubCategories();
            searchPrograms();
        });
    }

    // Sub-category filter change
    const subCategoryFilter = document.getElementById('subCategoryFilter');
    if (subCategoryFilter) {
        subCategoryFilter.addEventListener('change', (e) => {
            currentFilters.subCategoryId = e.target.value || null;
            searchPrograms();
        });
    }

    // Enter key support for search
    document.addEventListener('keypress', (e) => {
        if (e.key === 'Enter' && (e.target.id === 'searchText' || e.target.id === 'categoryFilter' || e.target.id === 'subCategoryFilter')) {
            searchPrograms();
        }
    });
}

// Load categories from API
async function loadCategories() {
    try {
        showLoading('Loading categories...');
        categories = await apiService.getCategories();
        
        if (categories && categories.length > 0) {
            populateCategoryFilter();
        } else {
            // Fallback to static categories
            categories = getStaticCategories();
            populateCategoryFilter();
        }
    } catch (error) {
        console.error('Error loading categories:', error);
        // Use static categories as fallback
        categories = getStaticCategories();
        populateCategoryFilter();
    } finally {
        hideLoading();
    }
}

// Populate category filter dropdown
function populateCategoryFilter() {
    const categoryFilter = document.getElementById('categoryFilter');
    if (!categoryFilter) return;

    // Clear existing options except the first one
    categoryFilter.innerHTML = '<option value="">All Categories</option>';

    categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category.id;
        option.textContent = category.name;
        categoryFilter.appendChild(option);
    });
}

// Update sub-categories based on selected category
function updateSubCategories() {
    const subCategoryFilter = document.getElementById('subCategoryFilter');
    if (!subCategoryFilter) return;

    // Clear existing options except the first one
    subCategoryFilter.innerHTML = '<option value="">All Sub Categories</option>';

    if (currentFilters.categoryId) {
        const selectedCategory = categories.find(cat => cat.id == currentFilters.categoryId);
        if (selectedCategory && selectedCategory.subCategories) {
            selectedCategory.subCategories.forEach(subCategory => {
                const option = document.createElement('option');
                option.value = subCategory.id;
                option.textContent = subCategory.name;
                subCategoryFilter.appendChild(option);
            });
        }
    }
}

// Load filters from URL parameters
function loadFiltersFromURL() {
    const urlParams = new URLSearchParams(window.location.search);
    
    const searchText = urlParams.get('search');
    const categoryId = urlParams.get('category');
    const subCategoryId = urlParams.get('subcategory');
    
    if (searchText) {
        currentFilters.searchText = searchText;
        document.getElementById('searchText').value = searchText;
    }
    
    if (categoryId) {
        currentFilters.categoryId = categoryId;
        document.getElementById('categoryFilter').value = categoryId;
        updateSubCategories();
    }
    
    if (subCategoryId) {
        currentFilters.subCategoryId = subCategoryId;
        document.getElementById('subCategoryFilter').value = subCategoryId;
    }
}

// Load initial programs
async function loadInitialPrograms() {
    try {
        showLoading('Loading programs...');
        await searchPrograms();
    } catch (error) {
        console.error('Error loading initial programs:', error);
        showError('Failed to load programs. Please try again.');
    } finally {
        hideLoading();
    }
}

// Main search function
async function searchPrograms() {
    try {
        showLoadingState();
        
        // Build search parameters
        const searchParams = {
            page: currentPage,
            size: pageSize
        };

        // Add filters if they have values
        if (currentFilters.searchText && currentFilters.searchText.trim()) {
            searchParams.searchText = currentFilters.searchText.trim();
        }
        if (currentFilters.categoryId) {
            searchParams.categoryId = currentFilters.categoryId;
        }
        if (currentFilters.subCategoryId) {
            searchParams.subCategoryId = currentFilters.subCategoryId;
        }

        console.log('Searching with params:', searchParams);

        // Call the filter API
        const response = await apiService.get('/programs/filter', searchParams, 'public');
        
        if (response && response.body) {
            currentPrograms = response.body.content || [];
            totalPages = response.body.totalPages || 0;
            
            displayPrograms(currentPrograms);
            updateResultsCount(response.body.totalElements || 0);
            updatePagination();
        } else {
            throw new Error('Invalid response format');
        }
    } catch (error) {
        console.error('Error searching programs:', error);
        showEmptyState();
        showError('Failed to search programs. Please try again.');
    } finally {
        hideLoadingState();
    }
}

// Display programs in the grid
function displayPrograms(programs) {
    const programsGrid = document.getElementById('programsGrid');
    const emptyState = document.getElementById('emptyState');
    
    if (!programsGrid) return;

    if (programs.length === 0) {
        programsGrid.style.display = 'none';
        emptyState.style.display = 'block';
        return;
    }

    programsGrid.style.display = 'grid';
    emptyState.style.display = 'none';
    programsGrid.innerHTML = '';

    programs.forEach(program => {
        const formattedProgram = apiService.formatProgramForUI(program);
        const programCard = createProgramCard(formattedProgram);
        programsGrid.appendChild(programCard);
    });
}

// Create program card element
function createProgramCard(program) {
    const card = document.createElement('div');
    card.className = 'program-card';
    card.onclick = () => openProgramDetails(program.urlSlug || program.id);
    
    card.innerHTML = `
        <img src="${program.image || 'resources/slider/youtube thumbnail.webp'}" 
             alt="${program.title}" 
             class="program-image"
             loading="lazy">
        <div class="program-content">
            <div class="program-category">${program.category}</div>
            <h3 class="program-title">${program.title}</h3>
            <p class="program-description">${program.description}</p>
            <div class="program-location">
                <i class="fas fa-map-marker-alt"></i>
                <span>${program.location}</span>
            </div>
            <div class="program-progress">
                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${program.progress}%"></div>
                </div>
                <div class="program-stats">
                    <span>Raised: ${program.formattedRaised}</span>
                    <span>Goal: ${program.formattedTarget}</span>
                </div>
            </div>
            <div class="program-percentage">${program.progress}% Funded</div>
            <div class="program-actions">
                <button class="btn btn-primary" onclick="event.stopPropagation(); openProgramDetails('${program.urlSlug || program.id}')">
                    Donate Now
                </button>
                <button class="btn btn-secondary" onclick="event.stopPropagation(); openProgramDetails('${program.urlSlug || program.id}')">
                    View Details
                </button>
            </div>
        </div>
    `;
    
    return card;
}

// Update results count
function updateResultsCount(count) {
    const resultsCount = document.getElementById('resultsCount');
    if (resultsCount) {
        resultsCount.textContent = count;
    }
}

// Update pagination
function updatePagination() {
    const pagination = document.getElementById('pagination');
    if (!pagination) return;

    if (totalPages <= 1) {
        pagination.style.display = 'none';
        return;
    }

    pagination.style.display = 'flex';
    pagination.innerHTML = '';

    // Previous button
    const prevBtn = document.createElement('button');
    prevBtn.className = 'pagination-btn';
    prevBtn.innerHTML = '<i class="fas fa-chevron-left"></i> Previous';
    prevBtn.disabled = currentPage === 0;
    prevBtn.onclick = () => goToPage(currentPage - 1);
    pagination.appendChild(prevBtn);

    // Page numbers
    const startPage = Math.max(0, currentPage - 2);
    const endPage = Math.min(totalPages - 1, currentPage + 2);

    if (startPage > 0) {
        const firstBtn = createPageButton(0);
        pagination.appendChild(firstBtn);
        
        if (startPage > 1) {
            const ellipsis = document.createElement('span');
            ellipsis.textContent = '...';
            ellipsis.style.padding = '0.75rem';
            pagination.appendChild(ellipsis);
        }
    }

    for (let i = startPage; i <= endPage; i++) {
        const pageBtn = createPageButton(i);
        pagination.appendChild(pageBtn);
    }

    if (endPage < totalPages - 1) {
        if (endPage < totalPages - 2) {
            const ellipsis = document.createElement('span');
            ellipsis.textContent = '...';
            ellipsis.style.padding = '0.75rem';
            pagination.appendChild(ellipsis);
        }
        
        const lastBtn = createPageButton(totalPages - 1);
        pagination.appendChild(lastBtn);
    }

    // Next button
    const nextBtn = document.createElement('button');
    nextBtn.className = 'pagination-btn';
    nextBtn.innerHTML = 'Next <i class="fas fa-chevron-right"></i>';
    nextBtn.disabled = currentPage >= totalPages - 1;
    nextBtn.onclick = () => goToPage(currentPage + 1);
    pagination.appendChild(nextBtn);
}

// Create page button
function createPageButton(pageNumber) {
    const button = document.createElement('button');
    button.className = `pagination-btn ${pageNumber === currentPage ? 'active' : ''}`;
    button.textContent = pageNumber + 1;
    button.onclick = () => goToPage(pageNumber);
    return button;
}

// Go to specific page
function goToPage(page) {
    if (page >= 0 && page < totalPages && page !== currentPage) {
        currentPage = page;
        searchPrograms();
        
        // Scroll to top of results
        const resultsSection = document.querySelector('.results-section');
        if (resultsSection) {
            resultsSection.scrollIntoView({ behavior: 'smooth' });
        }
    }
}

// Sort programs
function sortPrograms() {
    const sortBy = document.getElementById('sortBy').value;
    
    if (currentPrograms.length === 0) return;

    let sortedPrograms = [...currentPrograms];

    switch (sortBy) {
        case 'created':
            // API already returns sorted by created date
            break;
        case 'raised':
            sortedPrograms.sort((a, b) => b.raised - a.raised);
            break;
        case 'target':
            sortedPrograms.sort((a, b) => b.targetDonationAmount - a.targetDonationAmount);
            break;
        case 'progress':
            sortedPrograms.sort((a, b) => {
                const progressA = apiService.calculateProgress(a.raised, a.targetDonationAmount);
                const progressB = apiService.calculateProgress(b.raised, b.targetDonationAmount);
                return progressB - progressA;
            });
            break;
    }

    displayPrograms(sortedPrograms);
}

// Clear all filters
function clearFilters() {
    currentFilters = {
        searchText: '',
        categoryId: null,
        subCategoryId: null
    };

    // Reset form inputs
    document.getElementById('searchText').value = '';
    document.getElementById('categoryFilter').value = '';
    document.getElementById('subCategoryFilter').value = '';
    document.getElementById('subCategoryFilter').innerHTML = '<option value="">All Sub Categories</option>';

    // Reset pagination
    currentPage = 0;

    // Search with cleared filters
    searchPrograms();
}

// Donate to specific program
function donateToProgram(programSlug) {
    // Store the selected program for donation
    sessionStorage.setItem('selectedProgram', programSlug);
    
    // Redirect to main page donation section
    window.location.href = 'index.html#donate';
}

// Open program details page
function openProgramDetails(programSlug) {
    if (!programSlug) {
        showError('Program not found');
        return;
    }
    
    // Navigate to program details page with slug as query parameter
    window.location.href = `program.html?slug=${encodeURIComponent(programSlug)}`;
}

// Open program modal (deprecated - keeping for backward compatibility)
async function openProgramModal(programSlug) {
    try {
        const modal = document.getElementById('programModal');
        const program = await apiService.getProgramBySlug(programSlug);
        
        if (program) {
            const formattedProgram = apiService.formatProgramForUI(program);
            populateModal(formattedProgram);
            modal.style.display = 'block';
            document.body.style.overflow = 'hidden';
        } else {
            showError('Program details not found');
        }
    } catch (error) {
        console.error('Error opening program modal:', error);
        showError('Unable to load program details');
    }
}

// Populate modal with program data
function populateModal(program) {
    document.getElementById('modalImage').src = program.image || 'resources/slider/youtube thumbnail.webp';
    document.getElementById('modalImage').alt = program.title;
    document.getElementById('modalCategory').textContent = program.category;
    document.getElementById('modalTitle').textContent = program.title;
    document.getElementById('modalDescription').textContent = program.description;
    document.getElementById('modalLocation').textContent = program.location;
    document.getElementById('modalRaised').textContent = `Raised: ${program.formattedRaised}`;
    document.getElementById('modalTarget').textContent = `Goal: ${program.formattedTarget}`;
    document.getElementById('modalPercentage').textContent = `${program.progress}% Funded`;
    document.getElementById('modalProgressFill').style.width = `${program.progress}%`;
    
    // Set up donate button
    const donateBtn = document.getElementById('modalDonateBtn');
    donateBtn.onclick = () => {
        closeModal();
        donateToProgram(program.id);
    };
}

// Close modal
function closeModal() {
    const modal = document.getElementById('programModal');
    modal.style.display = 'none';
    document.body.style.overflow = 'auto';
}

// Show loading state
function showLoadingState() {
    const loadingState = document.getElementById('loadingState');
    const programsGrid = document.getElementById('programsGrid');
    const emptyState = document.getElementById('emptyState');
    
    if (loadingState) loadingState.style.display = 'block';
    if (programsGrid) programsGrid.style.display = 'none';
    if (emptyState) emptyState.style.display = 'none';
}

// Hide loading state
function hideLoadingState() {
    const loadingState = document.getElementById('loadingState');
    if (loadingState) loadingState.style.display = 'none';
}

// Show empty state
function showEmptyState() {
    const emptyState = document.getElementById('emptyState');
    const programsGrid = document.getElementById('programsGrid');
    
    if (emptyState) emptyState.style.display = 'block';
    if (programsGrid) programsGrid.style.display = 'none';
}

// Show loading message
function showLoading(message = 'Loading...') {
    // Create loading overlay
    const overlay = document.createElement('div');
    overlay.id = 'loadingOverlay';
    overlay.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.7);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 10000;
        color: white;
        font-size: 1.2rem;
    `;
    overlay.innerHTML = `
        <div style="text-align: center;">
            <div style="width: 40px; height: 40px; border: 4px solid #f3f3f3; border-top: 4px solid #f51a2d; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 1rem;"></div>
            ${message}
        </div>
    `;
    
    document.body.appendChild(overlay);
}

// Hide loading message
function hideLoading() {
    const overlay = document.getElementById('loadingOverlay');
    if (overlay) {
        overlay.remove();
    }
}

// Show success message
function showSuccess(message) {
    showMessage(message, 'success');
}

// Show error message
function showError(message) {
    showMessage(message, 'error');
}

// Show message
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

// Static categories fallback
function getStaticCategories() {
    return [
        {
            id: 1,
            name: 'Health & Medical',
            subCategories: [
                { id: 1, name: 'Mental Health' },
                { id: 2, name: 'Physical Health' },
                { id: 3, name: 'Emergency Medical' },
                { id: 4, name: 'Elderly Care' },
                { id: 5, name: 'Disability Support' }
            ]
        },
        {
            id: 2,
            name: 'Education',
            subCategories: [
                { id: 6, name: 'School Education' },
                { id: 7, name: 'Higher Education' },
                { id: 8, name: 'Adult Education' },
                { id: 9, name: 'Vocational Training' },
                { id: 10, name: 'Early Childhood' }
            ]
        },
        {
            id: 3,
            name: 'Environment',
            subCategories: [
                { id: 11, name: 'Climate Change' },
                { id: 12, name: 'Wildlife Conservation' },
                { id: 13, name: 'Marine Conservation' },
                { id: 14, name: 'Renewable Energy' },
                { id: 15, name: 'Waste Management' }
            ]
        },
        {
            id: 4,
            name: 'Disaster Relief',
            subCategories: [
                { id: 16, name: 'Natural Disasters' },
                { id: 17, name: 'Emergency Response' },
                { id: 18, name: 'Disaster Recovery' },
                { id: 19, name: 'Preparedness Training' }
            ]
        },
        {
            id: 5,
            name: 'Community Development',
            subCategories: [
                { id: 20, name: 'Infrastructure' },
                { id: 21, name: 'Social Services' },
                { id: 22, name: 'Economic Development' },
                { id: 23, name: 'Cultural Preservation' }
            ]
        }
    ];
}

// Setup modal event listeners
document.addEventListener('DOMContentLoaded', function() {
    const modal = document.getElementById('programModal');
    const closeBtn = document.querySelector('.close');
    
    if (closeBtn) {
        closeBtn.addEventListener('click', closeModal);
    }
    
    if (modal) {
        window.addEventListener('click', (e) => {
            if (e.target === modal) {
                closeModal();
            }
        });
        
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && modal.style.display === 'block') {
                closeModal();
            }
        });
    }
});

// Make functions globally available
window.searchPrograms = searchPrograms;
window.clearFilters = clearFilters;
window.sortPrograms = sortPrograms;
window.donateToProgram = donateToProgram;
window.openProgramDetails = openProgramDetails;
window.openProgramModal = openProgramModal;
window.closeModal = closeModal;
