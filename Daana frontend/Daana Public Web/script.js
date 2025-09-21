// API Configuration - Now handled by api-service.js

// Global Variables
let currentSlide = 0;
let slides = [];
let dots = [];

// DOM Content Loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

// Fallback initialization in case DOMContentLoaded already fired
if (document.readyState === 'loading') {
    // Still loading, wait for DOMContentLoaded
} else {
    // DOM already loaded, initialize immediately
    initializeApp();
}

// Initialize Application
function initializeApp() {
    // Initialize slider elements
    slides = document.querySelectorAll('.slide');
    dots = document.querySelectorAll('.dot');
    
    console.log('Slider elements found:', {
        slides: slides.length,
        dots: dots.length
    });
    
    setupNavigation();
    setupSlider();
    setupForms();
    setupModalEvents();
    setupDonationModalEvents();
    setupCarouselTouchSupport();
    loadCategories();
    loadCauses();
    loadStatistics();
    setupScrollAnimations();
    setupCounterAnimations();
    updateDonateButtonHandlers();
    setupProfileDropdown();
}

// Navigation Setup
function setupNavigation() {
    const hamburger = document.querySelector('.hamburger');
    const navMenu = document.querySelector('.nav-menu');
    const navLinks = document.querySelectorAll('.nav-link');

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

    // Smooth scrolling for navigation links
    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const targetId = link.getAttribute('href');
            const targetSection = document.querySelector(targetId);
            
            if (targetSection) {
                const offsetTop = targetSection.offsetTop - 80; // Account for fixed navbar
                window.scrollTo({
                    top: offsetTop,
                    behavior: 'smooth'
                });
            }
        });
    });

    // Navbar background on scroll
    window.addEventListener('scroll', () => {
        const navbar = document.querySelector('.navbar');
        if (window.scrollY > 100) {
            navbar.style.background = 'rgba(255, 255, 255, 0.98)';
        } else {
            navbar.style.background = 'rgba(255, 255, 255, 0.95)';
        }
    });
}

// Slider Setup
function setupSlider() {
    // Auto-slide functionality
    setInterval(() => {
        changeSlide(1);
    }, 5000);

    // Touch/swipe support for mobile
    let startX = 0;
    let endX = 0;

    const slider = document.querySelector('.hero-slider');
    
    if (slider) {
        slider.addEventListener('touchstart', (e) => {
            startX = e.touches[0].clientX;
        });

        slider.addEventListener('touchend', (e) => {
            endX = e.changedTouches[0].clientX;
            handleSwipe();
        });

        function handleSwipe() {
            const threshold = 50;
            const diff = startX - endX;

            if (Math.abs(diff) > threshold) {
                if (diff > 0) {
                    changeSlide(1); // Swipe left - next slide
                } else {
                    changeSlide(-1); // Swipe right - previous slide
                }
            }
        }
    }
}

// Slider Functions - Define immediately for global access
function changeSlide(direction) {
    if (slides.length === 0) return;
    
    slides[currentSlide].classList.remove('active');
    dots[currentSlide].classList.remove('active');

    currentSlide += direction;

    if (currentSlide >= slides.length) {
        currentSlide = 0;
    } else if (currentSlide < 0) {
        currentSlide = slides.length - 1;
    }

    slides[currentSlide].classList.add('active');
    dots[currentSlide].classList.add('active');
}

function goToSlide(n) {
    if (slides.length === 0) return;
    
    slides[currentSlide].classList.remove('active');
    dots[currentSlide].classList.remove('active');

    currentSlide = n - 1;
    slides[currentSlide].classList.add('active');
    dots[currentSlide].classList.add('active');
}

// Make functions globally available immediately
window.changeSlide = changeSlide;
window.currentSlide = goToSlide;

// Debug: Log that functions are available
console.log('Slider functions loaded:', {
    changeSlide: typeof window.changeSlide,
    currentSlide: typeof window.currentSlide
});

// Form Setup
function setupForms() {
    setupDonationForm();
    setupContactForm();
    setupNewsletterForm();
}

// Donation Form
function setupDonationForm() {
    const donationForm = document.getElementById('donationForm');
    const amountButtons = document.querySelectorAll('.amount-btn');
    const amountInput = document.getElementById('amount');

    // Check if elements exist before setting up event listeners
    if (!amountInput || amountButtons.length === 0) {
        console.log('Donation form elements not found on this page');
        return;
    }

    // Amount button selection
    amountButtons.forEach(button => {
        button.addEventListener('click', () => {
            amountButtons.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');
            amountInput.value = button.dataset.amount;
        });
    });

    // Custom amount input
    amountInput.addEventListener('input', () => {
        amountButtons.forEach(btn => btn.classList.remove('active'));
    });

    // Form submission - now opens donation modal
    if (donationForm) {
        donationForm.addEventListener('submit', async (e) => {
            e.preventDefault();
        
        const formData = new FormData(donationForm);
        const cause = formData.get('cause');
        const amount = parseFloat(formData.get('amount'));
        
        if (!cause) {
            showError('Please select a cause to donate to');
            return;
        }
        
        if (!amount || amount < 100) {
            showError('Please enter a valid donation amount (minimum LKR 100)');
            return;
        }
        
        // Create program object for donation modal
        const program = {
            programId: cause,
            id: cause,
            title: cause === 'general' ? 'General Fund' : cause,
            amount: amount
        };
        
        // Open donation modal
        openDonationModal(program);
        });
    }
}

// Contact Form
function setupContactForm() {
    const contactForm = document.getElementById('contactForm');
    
    if (!contactForm) {
        console.log('Contact form not found on this page');
        return;
    }

    contactForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const formData = new FormData(contactForm);
        const contactData = {
            name: formData.get('name'),
            email: formData.get('email'),
            subject: formData.get('subject'),
            message: formData.get('message')
        };

        try {
            showLoading('Sending message...');
            
            const result = await apiService.submitContact(contactData);
            showSuccess('Thank you for your message! We will get back to you soon.');
            contactForm.reset();
        } catch (error) {
            console.error('Contact error:', error);
            showError('Sorry, there was an error sending your message. Please try again.');
        } finally {
            hideLoading();
        }
    });
}

// Newsletter Form
function setupNewsletterForm() {
    const newsletterForm = document.querySelector('.newsletter-form');
    
    if (!newsletterForm) {
        console.log('Newsletter form not found on this page');
        return;
    }

    newsletterForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const email = newsletterForm.querySelector('input[type="email"]').value;
        
        try {
            const result = await apiService.subscribeNewsletter(email);
            showSuccess('Thank you for subscribing to our newsletter!');
            newsletterForm.reset();
        } catch (error) {
            console.error('Newsletter error:', error);
            showError('Sorry, there was an error subscribing. Please try again.');
        }
    });
}

// Carousel state management
let carouselStates = {
    trending: { currentIndex: 0, totalItems: 0 },
    latest: { currentIndex: 0, totalItems: 0 }
};

// Show loading state for causes
function showLoadingState() {
    const trendingTrack = document.getElementById('trendingCausesTrack');
    const latestTrack = document.getElementById('latestCausesTrack');
    
    const loadingHTML = `
        <div class="cause-card loading">
            <div class="loading-skeleton"></div>
        </div>
        <div class="cause-card loading">
            <div class="loading-skeleton"></div>
        </div>
        <div class="cause-card loading">
            <div class="loading-skeleton"></div>
        </div>
    `;
    
    if (trendingTrack) trendingTrack.innerHTML = loadingHTML;
    if (latestTrack) latestTrack.innerHTML = loadingHTML;
}

// Load Categories from API
async function loadCategories() {
    const categoriesGrid = document.getElementById('categoriesGrid');
    
    try {
        // Show loading state
        showCategoriesLoadingState();
        
        // Fetch categories from API
        const categories = await apiService.getCategories();
        
        if (categories && categories.length > 0) {
            displayCategories(categories);
        } else {
            // Fallback to static categories if API returns empty
            displayStaticCategories();
        }
    } catch (error) {
        console.error('Error loading categories:', error);
        displayStaticCategories();
    }
}

// Show loading state for categories
function showCategoriesLoadingState() {
    const categoriesTrack = document.getElementById('categoriesTrack');
    if (!categoriesTrack) return;
    
    categoriesTrack.innerHTML = `
        <div class="category-card loading">
            <div class="loading-skeleton"></div>
        </div>
        <div class="category-card loading">
            <div class="loading-skeleton"></div>
        </div>
        <div class="category-card loading">
            <div class="loading-skeleton"></div>
        </div>
        <div class="category-card loading">
            <div class="loading-skeleton"></div>
        </div>
    `;
}

// Display Categories
function displayCategories(categories) {
    const categoriesTrack = document.getElementById('categoriesTrack');
    if (!categoriesTrack) return;
    
    categoriesTrack.innerHTML = '';

    categories.forEach((category, index) => {
        const categoryCard = createCategoryCard(category, index === 0);
        categoriesTrack.appendChild(categoryCard);
    });
    
    // Set first category as active by default
    if (categories.length > 0) {
        updateSelectedCategory(categories[0]);
    }
    
    // Initialize category carousel state
    window.categoryCarouselState = {
        currentIndex: 0,
        totalItems: categories.length
    };
    
    updateCategoryNavButtons();
}

// Create Category Card
function createCategoryCard(category, isActive = false) {
    const card = document.createElement('div');
    card.className = `category-card ${isActive ? 'active' : ''}`;
    
    // Get icon class based on category name
    const iconClass = getCategoryIconClass(category.name);
    const icon = getCategoryIcon(category.name);
    
    // Truncate description for card display
    const truncatedDescription = truncateText(category.description, 80);
    
    card.innerHTML = `
        <div class="icon ${iconClass}">
            <i class="${icon}"></i>
        </div>
        <div class="name">${category.name}</div>
        <div class="count">${category.subCategories ? category.subCategories.length : 0} subcategories</div>
        <div class="description">${truncatedDescription}</div>
    `;
    
    // Add click event to select category
    card.addEventListener('click', () => {
        // Remove active class from all cards
        document.querySelectorAll('.category-card').forEach(el => el.classList.remove('active'));
        // Add active class to clicked card
        card.classList.add('active');
        // Update selected category
        updateSelectedCategory(category);
    });
    
    return card;
}

// Get icon class based on category name
function getCategoryIconClass(categoryName) {
    const name = categoryName.toLowerCase();
    if (name.includes('health') || name.includes('medical')) return 'health';
    if (name.includes('education') || name.includes('school')) return 'education';
    if (name.includes('environment') || name.includes('climate')) return 'environment';
    if (name.includes('disaster') || name.includes('emergency')) return 'disaster';
    if (name.includes('children') || name.includes('youth')) return 'children';
    if (name.includes('animal') || name.includes('wildlife')) return 'animals';
    if (name.includes('community') || name.includes('social')) return 'community';
    if (name.includes('technology') || name.includes('digital')) return 'technology';
    return 'health'; // default
}

// Get icon based on category name
function getCategoryIcon(categoryName) {
    const name = categoryName.toLowerCase();
    if (name.includes('health') || name.includes('medical')) return 'fas fa-medkit';
    if (name.includes('education') || name.includes('school')) return 'fas fa-graduation-cap';
    if (name.includes('environment') || name.includes('climate')) return 'fas fa-leaf';
    if (name.includes('disaster') || name.includes('emergency')) return 'fas fa-exclamation-triangle';
    if (name.includes('children') || name.includes('youth')) return 'fas fa-child';
    if (name.includes('animal') || name.includes('wildlife')) return 'fas fa-paw';
    if (name.includes('community') || name.includes('social')) return 'fas fa-users';
    if (name.includes('technology') || name.includes('digital')) return 'fas fa-laptop';
    return 'fas fa-heart'; // default
}

// Update selected category display
function updateSelectedCategory(category) {
    const nameElement = document.getElementById('selectedCategoryName');
    const descriptionElement = document.getElementById('selectedCategoryDescription');
    const imageElement = document.getElementById('selectedCategoryImage');
    const subcategoriesElement = document.getElementById('subcategoriesGrid');
    const exploreBtn = document.querySelector('.explore-category-btn');
    
    if (nameElement) {
        nameElement.textContent = category.name;
    }
    
    if (descriptionElement) {
        descriptionElement.textContent = category.description;
    }
    
    if (imageElement) {
        imageElement.src = category.imageUrl || 'resources/slider/youtube thumbnail.webp';
        imageElement.alt = category.name;
    }
    
    if (subcategoriesElement && category.subCategories) {
        subcategoriesElement.innerHTML = '';
        category.subCategories.forEach(subCategory => {
            const subcategoryItem = document.createElement('div');
            subcategoryItem.className = 'subcategory-item';
            subcategoryItem.innerHTML = `
                <div class="name">${subCategory.name}</div>
                <div class="description">${subCategory.description || 'Click to explore programs'}</div>
            `;
            
            // Add click event to filter by subcategory
            subcategoryItem.addEventListener('click', () => {
                filterProgramsBySubcategory(category.name, subCategory.name);
            });
            
            subcategoriesElement.appendChild(subcategoryItem);
        });
    }
    
    if (exploreBtn) {
        exploreBtn.style.display = 'flex';
        exploreBtn.onclick = () => filterProgramsByCategory(category);
    }
}

// Category carousel navigation
function scrollCategories(direction) {
    const state = window.categoryCarouselState;
    const track = document.getElementById('categoriesTrack');
    
    if (!track || !state || state.totalItems === 0) return;
    
    const cardWidth = 320 + 24; // card width + gap
    const visibleCards = Math.floor(track.parentElement.offsetWidth / cardWidth);
    const maxIndex = Math.max(0, state.totalItems - visibleCards);
    
    state.currentIndex += direction;
    state.currentIndex = Math.max(0, Math.min(state.currentIndex, maxIndex));
    
    const translateX = -state.currentIndex * cardWidth;
    track.style.transform = `translateX(${translateX}px)`;
    
    updateCategoryNavButtons();
}

// Update category navigation button states
function updateCategoryNavButtons() {
    const state = window.categoryCarouselState;
    const track = document.getElementById('categoriesTrack');
    
    if (!track || !state) return;
    
    const cardWidth = 320 + 24; // card width + gap
    const visibleCards = Math.floor(track.parentElement.offsetWidth / cardWidth);
    const maxIndex = Math.max(0, state.totalItems - visibleCards);
    
    const prevBtn = document.querySelector('.category-nav-btn.prev');
    const nextBtn = document.querySelector('.category-nav-btn.next');
    
    if (prevBtn) {
        prevBtn.disabled = state.currentIndex === 0;
    }
    if (nextBtn) {
        nextBtn.disabled = state.currentIndex >= maxIndex || state.totalItems <= visibleCards;
    }
}

// Filter programs by category
function filterProgramsByCategory(category) {
    // Scroll to causes section
    const causesSection = document.getElementById('causes');
    causesSection.scrollIntoView({ behavior: 'smooth' });
    
    // Show message
    setTimeout(() => {
        showSuccess(`Filtering programs by ${category.name} category. This feature will be implemented soon!`);
    }, 1000);
}

// Filter programs by subcategory
function filterProgramsBySubcategory(categoryName, subcategoryName) {
    // Scroll to causes section
    const causesSection = document.getElementById('causes');
    causesSection.scrollIntoView({ behavior: 'smooth' });
    
    // Show message
    setTimeout(() => {
        showSuccess(`Filtering programs by ${subcategoryName} in ${categoryName}. This feature will be implemented soon!`);
    }, 1000);
}

// Static Categories Fallback
function displayStaticCategories() {
    const staticCategories = [
        {
            id: 1,
            name: 'Health',
            description: 'Health and medical related programs including mental health, physical health, and emergency medical care',
            imageUrl: 'resources/slider/youtube thumbnail-1.webp',
            subCategories: [
                { name: 'Mental Health' },
                { name: 'Physical Health' },
                { name: 'Emergency Medical' },
                { name: 'Elderly Care' },
                { name: 'Disability Support' }
            ]
        },
        {
            id: 2,
            name: 'Education',
            description: 'Educational programs and scholarships for all age groups',
            imageUrl: 'resources/slider/youtube thumbnail-2.webp',
            subCategories: [
                { name: 'School Education' },
                { name: 'Higher Education' },
                { name: 'Adult Education' },
                { name: 'Vocational Training' },
                { name: 'Early Childhood' }
            ]
        },
        {
            id: 3,
            name: 'Environment',
            description: 'Environmental protection, conservation, and climate action programs',
            imageUrl: 'resources/slider/youtube thumbnail.webp',
            subCategories: [
                { name: 'Climate Change' },
                { name: 'Wildlife Conservation' },
                { name: 'Marine Conservation' },
                { name: 'Renewable Energy' },
                { name: 'Waste Management' }
            ]
        },
        {
            id: 4,
            name: 'Disaster Relief',
            description: 'Emergency response and disaster recovery services',
            imageUrl: 'resources/slider/youtube thumbnail-1.webp',
            subCategories: [
                { name: 'Natural Disasters' },
                { name: 'Emergency Response' },
                { name: 'Disaster Recovery' },
                { name: 'Preparedness Training' }
            ]
        }
    ];
    
    displayCategories(staticCategories);
}

// Load Statistics from API
async function loadStatistics() {
    try {
        const stats = await apiService.getStatistics();
        
        if (stats && Object.keys(stats).length > 0) {
            updateStatistics(stats);
        } else {
            // Use default statistics if API doesn't return data
            console.log('Using default statistics');
        }
    } catch (error) {
        console.error('Error loading statistics:', error);
        // Continue with default statistics
    }
}

// Update statistics display
function updateStatistics(stats) {
    const statNumbers = document.querySelectorAll('.stat-number');
    
    if (statNumbers.length >= 4) {
        // Update with real data if available
        if (stats.livesImpacted) {
            statNumbers[0].setAttribute('data-target', stats.livesImpacted);
        }
        if (stats.activeProjects) {
            statNumbers[1].setAttribute('data-target', stats.activeProjects);
        }
        if (stats.communitiesServed) {
            statNumbers[2].setAttribute('data-target', stats.communitiesServed);
        }
        if (stats.successRate) {
            statNumbers[3].setAttribute('data-target', stats.successRate);
        }
    }
}

// Load Causes from API
async function loadCauses() {
    try {
        // Show loading state
        showLoadingState();
        
        // Fetch trending and latest programs separately
        const [trendingPrograms, latestPrograms] = await Promise.all([
            apiService.getTrendingPrograms(),
            apiService.getLatestPrograms()
        ]);
        
        if (trendingPrograms.length > 0 || latestPrograms.length > 0) {
            displayTrendingCauses(trendingPrograms);
            displayLatestCauses(latestPrograms);
            
            // Combine all programs for donation form options
            const allPrograms = [...trendingPrograms, ...latestPrograms];
            updateDonationFormOptions(allPrograms);
        } else {
            // Fallback to static causes if API returns empty
            displayStaticCauses();
        }
    } catch (error) {
        console.error('Error loading causes:', error);
        displayStaticCauses();
    }
}

// Display Trending Causes
function displayTrendingCauses(programs) {
    const trendingTrack = document.getElementById('trendingCausesTrack');
    if (!trendingTrack) return;
    
    trendingTrack.innerHTML = '';
    carouselStates.trending.totalItems = programs.length;
    carouselStates.trending.currentIndex = 0;

    programs.forEach(program => {
        const formattedProgram = apiService.formatProgramForUI(program);
        const causeCard = createCauseCard(formattedProgram);
        trendingTrack.appendChild(causeCard);
    });
    
    updateCarouselButtons('trending');
    updateDonateButtonHandlers();
}

// Display Latest Causes
function displayLatestCauses(programs) {
    const latestTrack = document.getElementById('latestCausesTrack');
    if (!latestTrack) return;
    
    latestTrack.innerHTML = '';
    carouselStates.latest.totalItems = programs.length;
    carouselStates.latest.currentIndex = 0;

    programs.forEach(program => {
        const formattedProgram = apiService.formatProgramForUI(program);
        const causeCard = createCauseCard(formattedProgram);
        latestTrack.appendChild(causeCard);
    });
    
    updateCarouselButtons('latest');
    updateDonateButtonHandlers();
}

// Truncate text function
function truncateText(text, maxLength = 120) {
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength).trim() + '...';
}

// Create Cause Card
function createCauseCard(program) {
    const card = document.createElement('div');
    card.className = 'cause-card';
    card.dataset.programId = program.id;
    card.onclick = () => openProgramDetails(program.urlSlug || program.id);
    
    // Truncate description for card display
    const truncatedDescription = truncateText(program.description, 120);
    
    card.innerHTML = `
        <img src="${program.image || 'resources/slider/youtube thumbnail.webp'}" alt="${program.title}" loading="lazy" onclick="event.stopPropagation(); openProgramDetails('${program.urlSlug || program.id}')" style="cursor: pointer;">
        <div class="cause-content">
            <div class="cause-category">${program.category}</div>
            <h3 class="cause-title" onclick="event.stopPropagation(); openProgramDetails('${program.urlSlug || program.id}')" style="cursor: pointer;">${program.title}</h3>
            <p class="cause-description">${truncatedDescription}</p>
            <div class="cause-location">
                <i class="fas fa-map-marker-alt"></i>
                <span>${program.location}</span>
            </div>
            <div class="cause-progress">
                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${program.progress}%"></div>
                </div>
                <div class="cause-stats">
                    <div class="stat-item">
                        <span class="stat-label">Raised:</span>
                        <span class="stat-value">${program.formattedRaised}</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-label">Goal:</span>
                        <span class="stat-value">${program.formattedTarget}</span>
                    </div>
                </div>
            </div>
            <div class="cause-amount">${program.progress}% Funded</div>
            <div class="cause-actions">
                <button class="btn btn-primary donate-btn" data-program-id="${program.programId}" data-program-title="${program.title}">Donate Now</button>
                <button class="btn btn-secondary" onclick="event.stopPropagation(); openProgramDetails('${program.urlSlug || program.id}')">View Details</button>
            </div>
        </div>
    `;
    
    return card;
}

// Static Causes Fallback
function displayStaticCauses() {
    const staticTrendingCauses = [
        {
            programId: 1,
            urlSlug: 'flood-relief-fund',
            programTitle: 'Flood Relief Fund',
            programDescription: 'Providing immediate relief and long-term recovery support for communities affected by recent floods.',
            subCategoryName: 'Natural Disasters',
            location: 'Flood-affected areas',
            programImageUrl: 'resources/slider/youtube thumbnail.webp',
            targetDonationAmount: 1000000.00,
            raised: 300000.00
        },
        {
            programId: 2,
            urlSlug: 'wildlife-conservation',
            programTitle: 'Wildlife Conservation Project',
            programDescription: 'Conservation efforts to protect endangered species and their habitats in Sri Lanka.',
            subCategoryName: 'Wildlife Conservation',
            location: 'National parks and wildlife reserves',
            programImageUrl: 'resources/slider/youtube thumbnail.webp',
            targetDonationAmount: 900000.00,
            raised: 220000.00
        }
    ];

    const staticLatestCauses = [
        {
            programId: 3,
            urlSlug: 'mental-health-support',
            programTitle: 'Mental Health Support Program',
            programDescription: 'A comprehensive program to provide mental health support and counseling services to rural communities.',
            subCategoryName: 'Mental Health',
            location: 'Rural areas across Sri Lanka',
            programImageUrl: 'resources/slider/youtube thumbnail-2.webp',
            targetDonationAmount: 500000.00,
            raised: 125000.00
        },
        {
            programId: 4,
            urlSlug: 'emergency-medical-fund',
            programTitle: 'Emergency Medical Fund',
            programDescription: 'Providing emergency medical treatment and surgery support for patients who cannot afford healthcare.',
            subCategoryName: 'Emergency Medical',
            location: 'Hospitals nationwide',
            programImageUrl: 'resources/slider/youtube thumbnail-1.webp',
            targetDonationAmount: 750000.00,
            raised: 180000.00
        }
    ];
    
    displayTrendingCauses(staticTrendingCauses);
    displayLatestCauses(staticLatestCauses);
    
    // Update donation form with static data
    const allPrograms = [...staticTrendingCauses, ...staticLatestCauses];
    updateDonationFormOptions(allPrograms);
    
    // Update donate button handlers for static causes
    updateDonateButtonHandlers();
}

// Update donation form options dynamically
function updateDonationFormOptions(programs) {
    const causeSelect = document.getElementById('cause');
    
    // Clear existing options except the first one
    causeSelect.innerHTML = '<option value="">Choose a cause</option>';
    
    // Add program options
    programs.forEach(program => {
        const option = document.createElement('option');
        option.value = program.programId || program.urlSlug;
        option.textContent = program.programTitle;
        causeSelect.appendChild(option);
    });
    
    // Add general fund option
    const generalOption = document.createElement('option');
    generalOption.value = 'general';
    generalOption.textContent = 'General Fund';
    causeSelect.appendChild(generalOption);
}

// Modal Functions
let currentPrograms = []; // Store loaded programs for modal access

// Open program modal
async function openProgramModal(programSlug) {
    try {
        const modal = document.getElementById('programModal');
        const program = await apiService.getProgramBySlug(programSlug);
        
        if (program) {
            const formattedProgram = apiService.formatProgramForUI(program);
            populateModal(formattedProgram);
            modal.style.display = 'block';
            document.body.style.overflow = 'hidden'; // Prevent background scrolling
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
    // Show full description in modal (no truncation)
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
        donateToCause(program.id);
    };
}

// Close modal
function closeModal() {
    const modal = document.getElementById('programModal');
    modal.style.display = 'none';
    document.body.style.overflow = 'auto'; // Restore scrolling
}

// Donate to Specific Cause
function donateToCause(causeId) {
    const causeSelect = document.getElementById('cause');
    causeSelect.value = causeId;
    
    // Scroll to donation form
    const donateSection = document.getElementById('donate');
    donateSection.scrollIntoView({ behavior: 'smooth' });
}

// Scroll Animations
function setupScrollAnimations() {
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('fade-in-up');
            }
        });
    }, observerOptions);

    // Observe elements for animation
    const animateElements = document.querySelectorAll('.stat-item, .cause-card, .testimonial-card, .about-content, .donate-content');
    animateElements.forEach(el => observer.observe(el));
}

// Counter Animations
function setupCounterAnimations() {
    const counters = document.querySelectorAll('.stat-number');
    
    const counterObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                animateCounter(entry.target);
                counterObserver.unobserve(entry.target);
            }
        });
    }, { threshold: 0.5 });

    counters.forEach(counter => counterObserver.observe(counter));
}

function animateCounter(element) {
    const target = parseInt(element.dataset.target);
    const duration = 2000;
    const increment = target / (duration / 16);
    let current = 0;

    const timer = setInterval(() => {
        current += increment;
        if (current >= target) {
            current = target;
            clearInterval(timer);
        }
        element.textContent = Math.floor(current);
    }, 16);
}

// Utility Functions
function scrollToDonate() {
    document.getElementById('donate').scrollIntoView({ behavior: 'smooth' });
}

function scrollToCauses() {
    document.getElementById('causes').scrollIntoView({ behavior: 'smooth' });
}

// Make utility functions globally available
window.scrollToDonate = scrollToDonate;
window.scrollToCauses = scrollToCauses;

// Loading and Message Functions
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
    
    // Add spin animation
    const style = document.createElement('style');
    style.textContent = `
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    `;
    document.head.appendChild(style);
    
    document.body.appendChild(overlay);
}

function hideLoading() {
    const overlay = document.getElementById('loadingOverlay');
    if (overlay) {
        overlay.remove();
    }
}

function showSuccess(message) {
    showMessage(message, 'success');
}

function showError(message) {
    showMessage(message, 'error');
}

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

// API Helper Functions
class ApiClient {
    static async get(endpoint) {
        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            return await response.json();
        } catch (error) {
            console.error('API GET Error:', error);
            throw error;
        }
    }

    static async post(endpoint, data) {
        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data)
            });
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            return await response.json();
        } catch (error) {
            console.error('API POST Error:', error);
            throw error;
        }
    }
}

// Setup modal event listeners
function setupModalEvents() {
    const modal = document.getElementById('programModal');
    const closeBtn = document.querySelector('.close');
    
    // Close modal when clicking X
    closeBtn.addEventListener('click', closeModal);
    
    // Close modal when clicking outside
    window.addEventListener('click', (e) => {
        if (e.target === modal) {
            closeModal();
        }
    });
    
    // Close modal with Escape key
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && modal.style.display === 'block') {
            closeModal();
        }
    });
}

// Setup donation modal event listeners
function setupDonationModalEvents() {
    const donationModal = document.getElementById('donationModal');
    const successModal = document.getElementById('donationSuccessModal');
    
    // Close donation modal when clicking outside
    window.addEventListener('click', (e) => {
        if (e.target === donationModal) {
            closeDonationModal();
        }
        if (e.target === successModal) {
            closeDonationSuccessModal();
        }
    });
    
    // Close modals with Escape key
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            if (donationModal.style.display === 'block') {
                closeDonationModal();
            }
            if (successModal.style.display === 'block') {
                closeDonationSuccessModal();
            }
        }
    });
}

// Carousel scroll function
function scrollCarousel(type, direction) {
    const state = carouselStates[type];
    const track = document.getElementById(`${type}CausesTrack`);
    
    if (!track || state.totalItems === 0) return;
    
    const cardWidth = 320 + 24; // card width + gap
    const visibleCards = Math.floor(track.parentElement.offsetWidth / cardWidth);
    const maxIndex = Math.max(0, state.totalItems - visibleCards);
    
    state.currentIndex += direction;
    state.currentIndex = Math.max(0, Math.min(state.currentIndex, maxIndex));
    
    const translateX = -state.currentIndex * cardWidth;
    track.style.transform = `translateX(${translateX}px)`;
    
    updateCarouselButtons(type);
}

// Update carousel button states
function updateCarouselButtons(type) {
    const state = carouselStates[type];
    const track = document.getElementById(`${type}CausesTrack`);
    
    if (!track) return;
    
    const cardWidth = 320 + 24; // card width + gap
    const visibleCards = Math.floor(track.parentElement.offsetWidth / cardWidth);
    const maxIndex = Math.max(0, state.totalItems - visibleCards);
    
    const prevBtn = document.querySelector(`[onclick="scrollCarousel('${type}', -1)"]`);
    const nextBtn = document.querySelector(`[onclick="scrollCarousel('${type}', 1)"]`);
    
    if (prevBtn) {
        prevBtn.disabled = state.currentIndex === 0;
    }
    if (nextBtn) {
        nextBtn.disabled = state.currentIndex >= maxIndex || state.totalItems <= visibleCards;
    }
}

// View All Programs Function
function viewAllPrograms() {
    // This could redirect to a dedicated programs page or show all programs in a modal
    // For now, let's scroll to the causes section and show a message
    const causesSection = document.getElementById('causes');
    causesSection.scrollIntoView({ behavior: 'smooth' });
    
    // Show a message that all programs are displayed
    setTimeout(() => {
        showSuccess('All available programs are displayed above. Use the carousel to browse through all causes.');
    }, 1000);
}

// Add touch/swipe support for carousels
function setupCarouselTouchSupport() {
    const carousels = document.querySelectorAll('.causes-carousel');
    
    carousels.forEach(carousel => {
        let startX = 0;
        let endX = 0;
        let isDragging = false;
        
        carousel.addEventListener('touchstart', (e) => {
            startX = e.touches[0].clientX;
            isDragging = true;
        });
        
        carousel.addEventListener('touchmove', (e) => {
            if (!isDragging) return;
            e.preventDefault();
        });
        
        carousel.addEventListener('touchend', (e) => {
            if (!isDragging) return;
            endX = e.changedTouches[0].clientX;
            isDragging = false;
            
            const diff = startX - endX;
            const threshold = 50;
            
            if (Math.abs(diff) > threshold) {
                const type = carousel.id.includes('trending') ? 'trending' : 'latest';
                const direction = diff > 0 ? 1 : -1;
                scrollCarousel(type, direction);
            }
        });
    });
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

// ==================== DONATION MODAL FUNCTIONS ====================

// Global variables for donation modal
let currentDonationProgram = null;
let selectedDonorType = null;

// Open donation modal
function openDonationModal(program = null) {
    currentDonationProgram = program;
    const modal = document.getElementById('donationModal');
    const programTitle = document.getElementById('donationModalProgramTitle');
    
    // Debug logging
    console.log('Opening donation modal for program:', program);
    console.log('Program ID will be used as campaignId:', program?.programId || program?.id);
    
    if (program) {
        programTitle.textContent = `Supporting: ${program.title || program.programTitle || 'This Cause'}`;
    } else {
        programTitle.textContent = 'Supporting a great cause';
    }
    
    // Reset modal state
    resetDonationModal();
    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';
}

// Close donation modal
function closeDonationModal() {
    const modal = document.getElementById('donationModal');
    modal.style.display = 'none';
    document.body.style.overflow = 'auto';
    resetDonationModal();
}

// Reset donation modal to initial state
function resetDonationModal() {
    selectedDonorType = null;
    
    // Show donor type selection
    document.getElementById('donorTypeSelection').style.display = 'block';
    document.getElementById('anonymousDonationForm').style.display = 'none';
    document.getElementById('registeredDonorSection').style.display = 'none';
    
    // Remove selected class from donor type cards
    document.querySelectorAll('.donor-type-card').forEach(card => {
        card.classList.remove('selected');
    });
    
    // Reset form
    const form = document.getElementById('anonymousDonationFormElement');
    if (form) {
        form.reset();
    }
    
    // Reset amount buttons
    document.querySelectorAll('.amount-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // Hide payment slip section
    document.getElementById('paymentSlipSection').style.display = 'none';
    
    // Reset summary
    updateDonationSummary(0);
}

// Select donor type
function selectDonorType(type) {
    selectedDonorType = type;
    
    // Update UI
    document.querySelectorAll('.donor-type-card').forEach(card => {
        card.classList.remove('selected');
    });
    
    // Add selected class to clicked card
    event.currentTarget.classList.add('selected');
    
    if (type === 'anonymous') {
        // Check if user is already logged in
        const token = localStorage.getItem('daana_access_token');
        if (token) {
            // User is logged in, ask if they want to donate as registered user
            if (confirm('You are already logged in. Would you like to donate as a registered user to track your donations?')) {
                selectDonorType('registered');
                return;
            }
        }
        
        // Show anonymous donation form
        document.getElementById('donorTypeSelection').style.display = 'none';
        document.getElementById('anonymousDonationForm').style.display = 'block';
        document.getElementById('registeredDonorSection').style.display = 'none';
        
        // Setup anonymous donation form
        setupAnonymousDonationForm();
    } else if (type === 'registered') {
        // Check if user is logged in
        const token = localStorage.getItem('daana_access_token');
        if (token) {
            // User is logged in, show donation form
            document.getElementById('donorTypeSelection').style.display = 'none';
            document.getElementById('anonymousDonationForm').style.display = 'block';
            document.getElementById('registeredDonorSection').style.display = 'none';
            
            // Setup registered donation form
            setupRegisteredDonationForm();
        } else {
            // User not logged in, show login prompt
            document.getElementById('donorTypeSelection').style.display = 'none';
            document.getElementById('anonymousDonationForm').style.display = 'none';
            document.getElementById('registeredDonorSection').style.display = 'block';
        }
    }
}

// Back to donor type selection
function backToDonorTypeSelection() {
    document.getElementById('donorTypeSelection').style.display = 'block';
    document.getElementById('anonymousDonationForm').style.display = 'none';
    document.getElementById('registeredDonorSection').style.display = 'none';
    
    // Remove selected class
    document.querySelectorAll('.donor-type-card').forEach(card => {
        card.classList.remove('selected');
    });
    
    selectedDonorType = null;
}

// Setup anonymous donation form
function setupAnonymousDonationForm() {
    const form = document.getElementById('anonymousDonationFormElement');
    const amountButtons = document.querySelectorAll('#anonymousDonationForm .amount-btn');
    const amountInput = document.getElementById('anonymousAmount');
    const paymentMethodSelect = document.getElementById('paymentMethod');
    
    // Amount button selection
    amountButtons.forEach(button => {
        button.addEventListener('click', () => {
            amountButtons.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');
            amountInput.value = button.dataset.amount;
            updateDonationSummary(parseFloat(button.dataset.amount));
        });
    });
    
    // Custom amount input
    amountInput.addEventListener('input', () => {
        amountButtons.forEach(btn => btn.classList.remove('active'));
        const amount = parseFloat(amountInput.value) || 0;
        updateDonationSummary(amount);
    });
    
    // Payment method change
    paymentMethodSelect.addEventListener('change', () => {
        const paymentSlipSection = document.getElementById('paymentSlipSection');
        if (paymentMethodSelect.value === 'BANK_TRANSFER') {
            paymentSlipSection.style.display = 'block';
        } else {
            paymentSlipSection.style.display = 'none';
        }
    });
    
    // Form submission
    form.addEventListener('submit', handleAnonymousDonation);
}

// Setup registered donation form
function setupRegisteredDonationForm() {
    const form = document.getElementById('anonymousDonationFormElement');
    const amountButtons = document.querySelectorAll('#anonymousDonationForm .amount-btn');
    const amountInput = document.getElementById('anonymousAmount');
    const paymentMethodSelect = document.getElementById('paymentMethod');
    
    // Amount button selection
    amountButtons.forEach(button => {
        button.addEventListener('click', () => {
            amountButtons.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');
            amountInput.value = button.dataset.amount;
            updateDonationSummary(parseFloat(button.dataset.amount));
        });
    });
    
    // Custom amount input
    amountInput.addEventListener('input', () => {
        amountButtons.forEach(btn => btn.classList.remove('active'));
        const amount = parseFloat(amountInput.value) || 0;
        updateDonationSummary(amount);
    });
    
    // Payment method change
    paymentMethodSelect.addEventListener('change', () => {
        const paymentSlipSection = document.getElementById('paymentSlipSection');
        if (paymentMethodSelect.value === 'BANK_TRANSFER') {
            paymentSlipSection.style.display = 'block';
        } else {
            paymentSlipSection.style.display = 'none';
        }
    });
    
    // Form submission
    form.addEventListener('submit', handleRegisteredDonation);
}

// Handle anonymous donation
async function handleAnonymousDonation(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const amount = parseFloat(formData.get('amount'));
    const paymentMethod = formData.get('paymentMethod');
    const comments = formData.get('comments');
    const paymentSlipFile = document.getElementById('paymentSlip').files[0];
    
    if (amount < 100) {
        showError('Minimum donation amount is LKR 100');
        return;
    }
    
    const donationData = {
        programId: currentDonationProgram?.programId || currentDonationProgram?.id || '1',
        actualDonationAmount: amount,
        paymentMethod: paymentMethod,
        comments: comments
    };
    
    // Debug logging
    console.log('Processing anonymous donation with data:', donationData);
    console.log('Payment slip file:', paymentSlipFile);
    
    try {
        showDonationLoading(true);
        
        const response = await apiService.processAnonymousDonation(donationData, paymentSlipFile);
        
        if (response.success) {
            closeDonationModal();
            showDonationSuccess(response.body);
        } else {
            throw new Error(response.message || 'Donation failed');
        }
    } catch (error) {
        console.error('Anonymous donation error:', error);
        showError(error.message || 'Sorry, there was an error processing your donation. Please try again.');
    } finally {
        showDonationLoading(false);
    }
}

// Handle registered donation
async function handleRegisteredDonation(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const amount = parseFloat(formData.get('amount'));
    const paymentMethod = formData.get('paymentMethod');
    const comments = formData.get('comments');
    const paymentSlipFile = document.getElementById('paymentSlip').files[0];
    
    if (amount < 100) {
        showError('Minimum donation amount is LKR 100');
        return;
    }
    
    const donationData = {
        programId: currentDonationProgram?.programId || currentDonationProgram?.id || '1',
        actualDonationAmount: amount,
        paymentMethod: paymentMethod,
        comments: comments
    };
    
    try {
        showDonationLoading(true);
        
        const response = await apiService.processRegisteredDonation(donationData, paymentSlipFile);
        
        if (response.success) {
            closeDonationModal();
            showDonationSuccess(response.body);
        } else {
            throw new Error(response.message || 'Donation failed');
        }
    } catch (error) {
        console.error('Registered donation error:', error);
        showError(error.message || 'Sorry, there was an error processing your donation. Please try again.');
    } finally {
        showDonationLoading(false);
    }
}

// Update donation summary
function updateDonationSummary(amount) {
    const serviceChargeRate = 0.025; // 2.5%
    const serviceCharge = amount * serviceChargeRate;
    const total = amount + serviceCharge;
    
    document.getElementById('summaryAmount').textContent = `LKR ${amount.toLocaleString()}`;
    document.getElementById('summaryServiceCharge').textContent = `LKR ${serviceCharge.toLocaleString()}`;
    document.getElementById('summaryTotal').textContent = `LKR ${total.toLocaleString()}`;
}

// Handle file upload
function handleFileUpload(input) {
    const file = input.files[0];
    if (file) {
        // Validate file type
        const allowedTypes = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png'];
        if (!allowedTypes.includes(file.type)) {
            showError('Please upload a PDF, JPG, or PNG file');
            input.value = '';
            return;
        }
        
        // Validate file size (5MB max)
        const maxSize = 5 * 1024 * 1024; // 5MB
        if (file.size > maxSize) {
            showError('File size must be less than 5MB');
            input.value = '';
            return;
        }
        
        console.log('File selected:', file.name, file.size, file.type);
    }
}

// Show donation loading state
function showDonationLoading(show) {
    const submitBtn = document.querySelector('#anonymousDonationFormElement button[type="submit"]');
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

// Show donation success modal
function showDonationSuccess(donationData) {
    const modal = document.getElementById('donationSuccessModal');
    
    // Populate success modal with donation details
    document.getElementById('donationId').textContent = donationData.donationId || '-';
    document.getElementById('donationAmount').textContent = `LKR ${donationData.actualDonationAmount?.toLocaleString() || '0'}`;
    document.getElementById('netAmount').textContent = `LKR ${donationData.netDonationAmount?.toLocaleString() || '0'}`;
    document.getElementById('paymentReference').textContent = donationData.paymentReferenceNumber || '-';
    document.getElementById('donationStatus').textContent = donationData.status || 'PENDING';
    document.getElementById('successMessage').textContent = donationData.message || 'Thank you for your generous donation!';
    
    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';
}

// Close donation success modal
function closeDonationSuccessModal() {
    const modal = document.getElementById('donationSuccessModal');
    modal.style.display = 'none';
    document.body.style.overflow = 'auto';
}

// Download receipt (placeholder)
function downloadReceipt() {
    showMessage('Receipt download feature will be available soon!', 'success');
}

// Update existing donate button handlers
function updateDonateButtonHandlers() {
    // Update modal donate button
    const modalDonateBtn = document.getElementById('modalDonateBtn');
    if (modalDonateBtn) {
        modalDonateBtn.onclick = () => {
            closeModal();
            openDonationModal(currentDonationProgram);
        };
    }
    
    // Update cause card donate buttons
    document.querySelectorAll('.donate-btn').forEach(btn => {
        btn.onclick = (e) => {
            e.stopPropagation();
            const program = {
                programId: btn.dataset.programId || '1',
                id: btn.dataset.programId || '1',
                title: btn.dataset.programTitle || 'This Cause'
            };
            openDonationModal(program);
        };
    });
    
    // Update hero section donate buttons
    document.querySelectorAll('button[onclick="scrollToDonate()"]').forEach(btn => {
        btn.onclick = (e) => {
            e.preventDefault();
            openDonationModal();
        };
    });
}

// Export remaining functions for global access
window.donateToCause = donateToCause;
window.openProgramDetails = openProgramDetails;
window.openProgramModal = openProgramModal;
window.closeModal = closeModal;
window.viewAllPrograms = viewAllPrograms;
window.scrollCarousel = scrollCarousel;
window.scrollCategories = scrollCategories;

// Export donation modal functions
window.openDonationModal = openDonationModal;
window.closeDonationModal = closeDonationModal;
window.selectDonorType = selectDonorType;
window.backToDonorTypeSelection = backToDonorTypeSelection;
window.handleFileUpload = handleFileUpload;
window.closeDonationSuccessModal = closeDonationSuccessModal;
window.downloadReceipt = downloadReceipt;

// ==================== PROFILE DROPDOWN FUNCTIONS ====================

// Setup Profile Dropdown
function setupProfileDropdown() {
    // Check if user is authenticated
    checkAuthenticationStatus();
    
    // Close dropdown when clicking outside
    document.addEventListener('click', (event) => {
        const profileDropdown = document.querySelector('.profile-dropdown');
        if (profileDropdown && !profileDropdown.contains(event.target)) {
            closeProfileDropdown();
        }
    });
}

// Check Authentication Status
function checkAuthenticationStatus() {
    const token = localStorage.getItem('daana_access_token');
    const userData = localStorage.getItem('daana_user_data');
    
    const navAuth = document.getElementById('navAuth');
    const navProfile = document.getElementById('navProfile');
    
    if (token && userData) {
        // User is authenticated
        navAuth.style.display = 'none';
        navProfile.style.display = 'block';
        
        // Load user profile data
        loadUserProfileData();
    } else {
        // User is not authenticated
        navAuth.style.display = 'block';
        navProfile.style.display = 'none';
    }
}

// Load User Profile Data
async function loadUserProfileData() {
    try {
        const userData = JSON.parse(localStorage.getItem('daana_user_data') || '{}');
        
        // Update navigation profile info
        const navProfileName = document.getElementById('navProfileName');
        const dropdownProfileName = document.getElementById('dropdownProfileName');
        const dropdownProfileEmail = document.getElementById('dropdownProfileEmail');
        
        if (userData.fullName || userData.displayName) {
            const displayName = userData.fullName || userData.displayName;
            navProfileName.textContent = displayName;
            dropdownProfileName.textContent = displayName;
        }
        
        if (userData.email) {
            dropdownProfileEmail.textContent = userData.email;
        }
        
        // Update profile images
        updateProfileImages(userData.profileImageUrl);
        
    } catch (error) {
        console.error('Error loading user profile data:', error);
    }
}

// Update Profile Images
function updateProfileImages(profileImageUrl) {
    const navProfileImage = document.getElementById('navProfileImage');
    const navAvatarPlaceholder = document.getElementById('navAvatarPlaceholder');
    const dropdownProfileImage = document.getElementById('dropdownProfileImage');
    const dropdownAvatarPlaceholder = document.getElementById('dropdownAvatarPlaceholder');
    
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

// Toggle Profile Dropdown
function toggleProfileDropdown() {
    const dropdownMenu = document.getElementById('profileDropdownMenu');
    const isOpen = dropdownMenu.classList.contains('show');
    
    if (isOpen) {
        closeProfileDropdown();
    } else {
        openProfileDropdown();
    }
}

// Open Profile Dropdown
function openProfileDropdown() {
    const dropdownMenu = document.getElementById('profileDropdownMenu');
    dropdownMenu.classList.add('show');
}

// Close Profile Dropdown
function closeProfileDropdown() {
    const dropdownMenu = document.getElementById('profileDropdownMenu');
    dropdownMenu.classList.remove('show');
}

// Show Dashboard
function showDashboard() {
    closeProfileDropdown();
    // TODO: Implement dashboard page/modal
    alert('Dashboard feature coming soon!');
}

// Show Donation History
function showDonationHistory() {
    closeProfileDropdown();
    // TODO: Implement donation history page/modal
    alert('Donation History feature coming soon!');
}

// Show Account Settings
function showAccountSettings() {
    closeProfileDropdown();
    // TODO: Implement account settings page/modal
    alert('Account Settings feature coming soon!');
}

// Logout Function
function logout() {
    closeProfileDropdown();
    
    // Clear authentication data
    localStorage.removeItem('daana_access_token');
    localStorage.removeItem('daana_refresh_token');
    localStorage.removeItem('daana_user_data');
    
    // Redirect to home page
    window.location.href = 'index.html';
}

// Export profile dropdown functions
window.toggleProfileDropdown = toggleProfileDropdown;
window.showDashboard = showDashboard;
window.showDonationHistory = showDonationHistory;
window.showAccountSettings = showAccountSettings;
window.logout = logout;

