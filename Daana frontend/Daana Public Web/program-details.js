/**
 * Program Details Page JavaScript
 * Handles program details display, donation functionality, and dynamic URL routing
 */

// Global variables
let currentProgram = null;
let selectedAmount = 0;
let selectedDonationOption = 'card';
let programSelectedDonorType = null; // Renamed to avoid conflict with script.js

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    initializeProgramPage();
});

// Initialize program page functionality
async function initializeProgramPage() {
    try {
        setupNavigation();
        setupEventListeners();
        await loadProgramDetails();
        
        // Test donation button
        console.log('Testing donation button...');
        const donateBtn = document.querySelector('button[onclick="openDonationModal()"]');
        if (donateBtn) {
            console.log('Donate button found:', donateBtn);
            // Add a test click handler
            donateBtn.addEventListener('click', function(e) {
                console.log('Donate button clicked!');
                e.preventDefault();
                openDonationModal();
            });
        } else {
            console.log('Donate button not found');
        }
    } catch (error) {
        console.error('Error initializing program page:', error);
        showErrorState();
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
    // Custom amount input
    const customAmountInput = document.getElementById('customAmount');
    if (customAmountInput) {
        customAmountInput.addEventListener('input', updateDonationSummary);
    }

    // Donation form submission
    const donationForm = document.getElementById('donationForm');
    if (donationForm) {
        donationForm.addEventListener('submit', handleDonationSubmission);
    }

    // Modal close events
    window.addEventListener('click', (e) => {
        const paymentModal = document.getElementById('paymentModal');
        const donationModal = document.getElementById('donationModal');
        
        if (e.target === paymentModal) {
            closePaymentModal();
        }
        if (e.target === donationModal) {
            closeDonationModal();
        }
    });

    // Escape key to close modals
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            closePaymentModal();
            closeDonationModal();
        }
    });
}

// Load program details from URL slug
async function loadProgramDetails() {
    try {
        showLoadingState();
        
        // Get URL slug from path
        const urlSlug = getUrlSlug();
        if (!urlSlug) {
            throw new Error('No program slug provided');
        }

        console.log('Loading program with slug:', urlSlug);

        // Fetch program details from API
        const response = await apiService.get(`/programs/${urlSlug}`, null, 'public');
        
        if (response && response.success && response.body) {
            currentProgram = response.body;
            displayProgramDetails(currentProgram);
            hideLoadingState();
        } else {
            throw new Error('Program not found');
        }
    } catch (error) {
        console.error('Error loading program details:', error);
        hideLoadingState();
        showErrorState();
    }
}

// Get URL slug from query parameters
function getUrlSlug() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('slug');
}

// Display program details
function displayProgramDetails(program) {
    // Update page title
    document.getElementById('pageTitle').textContent = `${program.programName} - Daana.lk`;
    
    // Update hero section
    document.getElementById('heroTitle').textContent = program.programName;
    document.getElementById('breadcrumbCategory').textContent = program.category?.name || 'Program';
    document.getElementById('breadcrumbTitle').textContent = program.programName;
    
    // Update main content
    document.getElementById('programImage').src = program.programImage || 'resources/slider/youtube thumbnail.webp';
    document.getElementById('programImage').alt = program.programName;
    document.getElementById('programCategory').textContent = program.subCategory?.name || program.category?.name || 'General';
    document.getElementById('programTitle').textContent = program.programName;
    document.getElementById('programDescription').textContent = program.description;
    document.getElementById('programLocation').textContent = program.programLocation || 'Not specified';
    
    // Calculate and display duration
    const duration = calculateDuration(program.startDate, program.endDate);
    document.getElementById('programDuration').textContent = duration;
    
    // Update contact information
    document.getElementById('contactPerson').textContent = program.contactPersonName || 'Not specified';
    document.getElementById('contactEmail').textContent = program.contactPersonEmail || 'Not specified';
    
    // Update progress section
    updateProgressSection(program);
    
    // Update charity information
    updateCharitySection(program.charity);
    
    // Update documents section
    updateDocumentsSection(program.donationProgramDocuments);
    
    // Update video section
    updateVideoSection(program.programVideo);
    
    // Show the program content
    document.getElementById('programHero').style.display = 'block';
    document.getElementById('programDetails').style.display = 'block';
}

// Calculate program duration
function calculateDuration(startDate, endDate) {
    if (!startDate || !endDate) return 'Ongoing';
    
    const start = new Date(startDate);
    const end = new Date(endDate);
    const now = new Date();
    
    if (now < start) {
        return `Starting ${formatDate(start)}`;
    } else if (now > end) {
        return `Ended ${formatDate(end)}`;
    } else {
        return `Until ${formatDate(end)}`;
    }
}

// Format date for display
function formatDate(date) {
    return new Date(date).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

// Update progress section
function updateProgressSection(program) {
    const raised = program.raised || 0;
    const target = program.targetDonationAmount || 0;
    const progress = target > 0 ? Math.min((raised / target) * 100, 100) : 0;
    
    // Update progress bar
    document.getElementById('progressFill').style.width = `${progress}%`;
    document.getElementById('progressAmount').textContent = `LKR ${formatCurrency(raised)}`;
    document.getElementById('raisedAmount').textContent = `LKR ${formatCurrency(raised)}`;
    document.getElementById('targetAmount').textContent = `LKR ${formatCurrency(target)}`;
    document.getElementById('progressPercentage').textContent = `${Math.round(progress)}% Funded`;
}

// Update charity section
function updateCharitySection(charity) {
    if (!charity) return;
    
    document.getElementById('charityName').textContent = charity.name || 'Unknown Charity';
    document.getElementById('charityDescription').textContent = charity.charityDescription || 'No description available';
    document.getElementById('charityContactPerson').textContent = charity.contactPersonName || charity.charityRepresentPerson || 'Not specified';
    document.getElementById('charityContactMobile').textContent = charity.contactPersonMobile || 'Not specified';
    document.getElementById('charityContactEmail').textContent = charity.email || 'Not specified';
    
    // Set charity logo
    const charityLogo = document.getElementById('charityLogo');
    if (charity.charityLogo) {
        charityLogo.src = charity.charityLogo;
        charityLogo.alt = charity.name;
    } else {
        charityLogo.src = 'resources/logo/Danna-logo.webp';
        charityLogo.alt = 'Default Logo';
    }
}

// Update documents section
function updateDocumentsSection(documents) {
    const documentsSection = document.getElementById('documentsSection');
    const documentList = document.getElementById('documentList');
    
    if (!documents || documents.length === 0) {
        documentsSection.style.display = 'none';
        return;
    }
    
    documentList.innerHTML = '';
    
    documents.forEach(doc => {
        if (doc.status === 'ACTIVE' && doc.published) {
            const documentItem = document.createElement('div');
            documentItem.className = 'document-item';
            documentItem.onclick = () => openDocument(doc.documentPath);
            
            documentItem.innerHTML = `
                <div class="document-icon">
                    <i class="fas fa-file-pdf"></i>
                </div>
                <div class="document-info">
                    <h4>${doc.documentName}</h4>
                    <p>Click to view document</p>
                </div>
            `;
            
            documentList.appendChild(documentItem);
        }
    });
    
    documentsSection.style.display = 'block';
}

// Update video section
function updateVideoSection(videoUrl) {
    const videoSection = document.getElementById('videoSection');
    const videoContainer = document.getElementById('videoContainer');
    
    if (!videoUrl) {
        videoSection.style.display = 'none';
        return;
    }
    
    // Extract YouTube video ID
    const videoId = extractYouTubeId(videoUrl);
    if (videoId) {
        videoContainer.innerHTML = `
            <iframe 
                src="https://www.youtube.com/embed/${videoId}" 
                title="Program Video"
                allowfullscreen>
            </iframe>
        `;
        videoSection.style.display = 'block';
    } else {
        videoSection.style.display = 'none';
    }
}

// Extract YouTube video ID from URL
function extractYouTubeId(url) {
    const regExp = /^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|&v=)([^#&?]*).*/;
    const match = url.match(regExp);
    return (match && match[2].length === 11) ? match[2] : null;
}

// Open document in new tab
function openDocument(documentPath) {
    window.open(documentPath, '_blank');
}

// Select donation option
function selectDonationOption(option) {
    selectedDonationOption = option;
    
    // Update active state
    document.querySelectorAll('.donation-option').forEach(el => {
        el.classList.remove('active');
    });
    event.currentTarget.classList.add('active');
}



// Open donation modal - now uses the new donation modal system
function openDonationModal() {
    console.log('openDonationModal called');
    console.log('currentProgram:', currentProgram);
    
    if (!currentProgram) {
        console.log('No currentProgram, using fallback data');
        // Use fallback program data for testing
        currentProgram = {
            programId: 1,
            programName: 'Test Program',
            description: 'Test program description',
            programLocation: 'Test Location',
            programImage: 'resources/slider/youtube thumbnail.webp',
            targetDonationAmount: 100000,
            raised: 50000
        };
    }
    
    // Create program object for the new donation modal
    const program = {
        programId: currentProgram.programId,
        id: currentProgram.programId,
        title: currentProgram.programName,
        programTitle: currentProgram.programName,
        description: currentProgram.description,
        category: currentProgram.subCategory?.name || currentProgram.category?.name,
        location: currentProgram.programLocation,
        image: currentProgram.programImage,
        target: currentProgram.targetDonationAmount,
        raised: currentProgram.raised
    };
    
    console.log('Created program object:', program);
    
    // Try to use the new donation modal system from script.js
    if (typeof window.openDonationModal === 'function' && window.openDonationModal !== openDonationModal) {
        console.log('Using new donation modal system from script.js');
        window.openDonationModal(program);
    } else {
        console.log('Using direct modal approach');
        // Directly open the new donation modal
        const modal = document.getElementById('donationModal');
        if (modal) {
            // Update the modal title
            const titleElement = document.getElementById('donationModalProgramTitle');
            if (titleElement) {
                titleElement.textContent = `Supporting: ${program.title}`;
            }
            
            // Show the modal
            modal.style.display = 'block';
            document.body.style.overflow = 'hidden';
            
            // Reset modal state
            resetDonationModal();
        } else {
            console.error('Donation modal not found');
            showError('Donation modal not available');
        }
    }
}

// Reset donation modal state
function resetDonationModal() {
    // Show donor type selection
    document.getElementById('donorTypeSelection').style.display = 'block';
    document.getElementById('anonymousDonationForm').style.display = 'none';
    document.getElementById('registeredDonorSection').style.display = 'none';
    
    // Reset form
    const form = document.getElementById('anonymousDonationFormElement');
    if (form) {
        form.reset();
    }
    
    // Reset amount buttons
    document.querySelectorAll('.amount-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // Reset summary
    updateDonationSummary();
}

// Close donation modal
function closeDonationModal() {
    document.getElementById('donationModal').style.display = 'none';
    document.body.style.overflow = 'auto';
}

// Open payment modal
function openPaymentModal() {
    if (!currentProgram) return;
    
    const amount = selectedAmount || parseFloat(document.getElementById('customAmount').value) || 0;
    const processingFee = Math.round(amount * 0.03);
    const total = amount + processingFee;
    
    // Generate reference number
    const reference = `REF${Date.now()}${Math.floor(Math.random() * 1000)}`;
    
    document.getElementById('paymentProgramName').textContent = currentProgram.programName;
    document.getElementById('paymentAmount').textContent = `LKR ${formatCurrency(amount)}`;
    document.getElementById('paymentReference').textContent = reference;
    document.getElementById('paymentTotal').textContent = `LKR ${formatCurrency(total)}`;
    
    document.getElementById('paymentModal').style.display = 'block';
    document.body.style.overflow = 'hidden';
}

// Close payment modal
function closePaymentModal() {
    document.getElementById('paymentModal').style.display = 'none';
    document.body.style.overflow = 'auto';
}

// Print payment slip
function printPaymentSlip() {
    window.print();
}

// Handle donation form submission
async function handleDonationSubmission(e) {
    e.preventDefault();
    
    if (!currentProgram) {
        showError('Program information not available');
        return;
    }
    
    const amount = selectedAmount || parseFloat(document.getElementById('customAmount').value) || 0;
    if (amount < 100) {
        showError('Minimum donation amount is LKR 100');
        return;
    }
    
    const donorName = document.getElementById('donorName').value.trim();
    const donorEmail = document.getElementById('donorEmail').value.trim();
    const donorPhone = document.getElementById('donorPhone').value.trim();
    const donorMessage = document.getElementById('donorMessage').value.trim();
    
    if (!donorName || !donorEmail) {
        showError('Please fill in all required fields');
        return;
    }
    
    try {
        showLoading('Processing donation...');
        
        if (selectedDonationOption === 'slip') {
            // Handle payment slip
            closeDonationModal();
            openPaymentModal();
            hideLoading();
        } else {
            // Handle card payment
            const donationData = {
                programId: currentProgram.urlName,
                amount: amount,
                donorName: donorName,
                donorEmail: donorEmail,
                donorPhone: donorPhone,
                donorMessage: donorMessage,
                paymentMethod: 'card'
            };
            
            // Simulate API call for card payment
            await simulateCardPayment(donationData);
            
            hideLoading();
            closeDonationModal();
            showSuccess('Donation processed successfully! Thank you for your contribution.');
        }
    } catch (error) {
        console.error('Error processing donation:', error);
        hideLoading();
        showError('Failed to process donation. Please try again.');
    }
}

// Simulate card payment
async function simulateCardPayment(donationData) {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 2000));
    
    // In a real implementation, this would integrate with a payment gateway
    console.log('Processing card payment:', donationData);
    
    // For demo purposes, we'll just log the donation
    localStorage.setItem('lastDonation', JSON.stringify({
        ...donationData,
        timestamp: new Date().toISOString(),
        transactionId: `TXN${Date.now()}`
    }));
}

// Format currency
function formatCurrency(amount) {
    return new Intl.NumberFormat('en-LK').format(amount);
}

// Show loading state
function showLoadingState() {
    document.getElementById('loadingState').style.display = 'block';
    document.getElementById('errorState').style.display = 'none';
    document.getElementById('programHero').style.display = 'none';
    document.getElementById('programDetails').style.display = 'none';
}

// Hide loading state
function hideLoadingState() {
    document.getElementById('loadingState').style.display = 'none';
}

// Show error state
function showErrorState() {
    document.getElementById('loadingState').style.display = 'none';
    document.getElementById('errorState').style.display = 'block';
    document.getElementById('programHero').style.display = 'none';
    document.getElementById('programDetails').style.display = 'none';
}

// Show loading message
function showLoading(message = 'Loading...') {
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

// Add donation modal functions for program details page
function selectDonorType(type) {
    console.log('selectDonorType called with:', type);
    
    if (typeof window.selectDonorType === 'function' && window.selectDonorType !== selectDonorType) {
        window.selectDonorType(type);
    } else {
        // Local implementation
        programSelectedDonorType = type;
        
        // Update UI
        document.querySelectorAll('.donor-type-card').forEach(card => {
            card.classList.remove('selected');
        });
        
        // Add selected class to clicked card
        if (event && event.currentTarget) {
            event.currentTarget.classList.add('selected');
        }
        
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
}

function backToDonorTypeSelection() {
    if (typeof window.backToDonorTypeSelection === 'function' && window.backToDonorTypeSelection !== backToDonorTypeSelection) {
        window.backToDonorTypeSelection();
    } else {
        // Local implementation
        document.getElementById('donorTypeSelection').style.display = 'block';
        document.getElementById('anonymousDonationForm').style.display = 'none';
        document.getElementById('registeredDonorSection').style.display = 'none';
    }
}

function handleFileUpload(input) {
    if (typeof window.handleFileUpload === 'function' && window.handleFileUpload !== handleFileUpload) {
        window.handleFileUpload(input);
    } else {
        // Local implementation
        const file = input.files[0];
        if (file) {
            const maxSize = 5 * 1024 * 1024; // 5MB
            const allowedTypes = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png'];
            
            if (file.size > maxSize) {
                showError('File size must be less than 5MB');
                input.value = '';
                return;
            }
            
            if (!allowedTypes.includes(file.type)) {
                showError('Please upload a PDF, JPG, or PNG file');
                input.value = '';
                return;
            }
            
            console.log('File uploaded:', file.name, file.size, file.type);
        }
    }
}

function closeDonationSuccessModal() {
    if (typeof window.closeDonationSuccessModal === 'function' && window.closeDonationSuccessModal !== closeDonationSuccessModal) {
        window.closeDonationSuccessModal();
    } else {
        // Local implementation
        const modal = document.getElementById('donationSuccessModal');
        if (modal) {
            modal.style.display = 'none';
            document.body.style.overflow = 'auto';
        }
    }
}

function downloadReceipt() {
    if (typeof window.downloadReceipt === 'function' && window.downloadReceipt !== downloadReceipt) {
        window.downloadReceipt();
    } else {
        // Local implementation
        showMessage('Receipt download feature coming soon!', 'info');
    }
}

// Setup anonymous donation form
function setupAnonymousDonationForm() {
    const form = document.getElementById('anonymousDonationFormElement');
    if (!form) return;
    
    // Remove existing event listeners
    form.removeEventListener('submit', handleAnonymousDonation);
    
    // Add new event listener
    form.addEventListener('submit', handleAnonymousDonation);
    
    // Setup amount buttons
    document.querySelectorAll('.amount-btn').forEach(btn => {
        btn.onclick = function() {
            const amount = parseFloat(this.dataset.amount);
            selectAmount(amount, this);
        };
    });
    
    // Setup custom amount input
    const amountInput = document.getElementById('anonymousAmount');
    if (amountInput) {
        amountInput.oninput = function() {
            // Clear selected amount when user types custom amount
            selectedAmount = 0;
            // Remove active class from all amount buttons
            document.querySelectorAll('.amount-btn').forEach(el => {
                el.classList.remove('active');
            });
            updateDonationSummary();
        };
    }
    
    // Setup payment method change
    const paymentMethod = document.getElementById('paymentMethod');
    if (paymentMethod) {
        paymentMethod.onchange = function() {
            const paymentSlipSection = document.getElementById('paymentSlipSection');
            if (this.value === 'BANK_TRANSFER') {
                paymentSlipSection.style.display = 'block';
            } else {
                paymentSlipSection.style.display = 'none';
            }
        };
    }
}

// Setup registered donation form
function setupRegisteredDonationForm() {
    const form = document.getElementById('anonymousDonationFormElement');
    if (!form) return;
    
    // Remove existing event listeners
    form.removeEventListener('submit', handleRegisteredDonation);
    
    // Add new event listener
    form.addEventListener('submit', handleRegisteredDonation);
    
    // Setup amount buttons
    document.querySelectorAll('.amount-btn').forEach(btn => {
        btn.onclick = function() {
            const amount = parseFloat(this.dataset.amount);
            selectAmount(amount, this);
        };
    });
    
    // Setup custom amount input
    const amountInput = document.getElementById('anonymousAmount');
    if (amountInput) {
        amountInput.oninput = function() {
            // Clear selected amount when user types custom amount
            selectedAmount = 0;
            // Remove active class from all amount buttons
            document.querySelectorAll('.amount-btn').forEach(el => {
                el.classList.remove('active');
            });
            updateDonationSummary();
        };
    }
    
    // Setup payment method change
    const paymentMethod = document.getElementById('paymentMethod');
    if (paymentMethod) {
        paymentMethod.onchange = function() {
            const paymentSlipSection = document.getElementById('paymentSlipSection');
            if (this.value === 'BANK_TRANSFER') {
                paymentSlipSection.style.display = 'block';
            } else {
                paymentSlipSection.style.display = 'none';
            }
        };
    }
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
        programId: currentProgram?.programId || currentProgram?.id || '1',
        actualDonationAmount: amount,
        paymentMethod: paymentMethod,
        comments: comments
    };
    
    console.log('Processing anonymous donation with data:', donationData);
    
    try {
        showLoading('Processing donation...');
        
        const response = await apiService.processAnonymousDonation(donationData, paymentSlipFile);
        
        if (response.success) {
            closeDonationModal();
            showDonationSuccess(response.body);
        } else {
            showError(response.message || 'Donation failed');
        }
    } catch (error) {
        console.error('Error processing donation:', error);
        showError('Failed to process donation. Please try again.');
    } finally {
        hideLoading();
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
        programId: currentProgram?.programId || currentProgram?.id || '1',
        actualDonationAmount: amount,
        paymentMethod: paymentMethod,
        comments: comments
    };
    
    console.log('Processing registered donation with data:', donationData);
    
    try {
        showLoading('Processing donation...');
        
        const response = await apiService.processRegisteredDonation(donationData, paymentSlipFile);
        
        if (response.success) {
            closeDonationModal();
            showDonationSuccess(response.body);
        } else {
            showError(response.message || 'Donation failed');
        }
    } catch (error) {
        console.error('Error processing donation:', error);
        showError('Failed to process donation. Please try again.');
    } finally {
        hideLoading();
    }
}

// Show donation success
function showDonationSuccess(donationData) {
    const modal = document.getElementById('donationSuccessModal');
    if (!modal) return;
    
    // Update success message
    const successMessage = document.getElementById('successMessage');
    if (successMessage) {
        successMessage.textContent = donationData.message || 'Thank you for your generous donation. Your contribution will make a real difference.';
    }
    
    // Update donation details
    document.getElementById('donationId').textContent = donationData.donationId || '-';
    document.getElementById('donationAmount').textContent = `LKR ${formatCurrency(donationData.actualDonationAmount || 0)}`;
    document.getElementById('netAmount').textContent = `LKR ${formatCurrency(donationData.netDonationAmount || 0)}`;
    document.getElementById('paymentReference').textContent = donationData.paymentReferenceNumber || '-';
    document.getElementById('donationStatus').textContent = donationData.status || '-';
    
    // Show modal
    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';
}

// Update donation summary for program details page
function updateDonationSummary() {
    const customAmount = parseFloat(document.getElementById('anonymousAmount')?.value) || 0;
    const amount = selectedAmount || customAmount;
    const serviceCharge = Math.round(amount * 0.025); // 2.5% service charge
    const total = amount + serviceCharge;
    
    console.log('updateDonationSummary - selectedAmount:', selectedAmount, 'customAmount:', customAmount, 'final amount:', amount);
    
    const summaryAmount = document.getElementById('summaryAmount');
    const summaryServiceCharge = document.getElementById('summaryServiceCharge');
    const summaryTotal = document.getElementById('summaryTotal');
    
    if (summaryAmount) summaryAmount.textContent = `LKR ${formatCurrency(amount)}`;
    if (summaryServiceCharge) summaryServiceCharge.textContent = `LKR ${formatCurrency(serviceCharge)}`;
    if (summaryTotal) summaryTotal.textContent = `LKR ${formatCurrency(total)}`;
    
    console.log('Updated summary elements:', {
        summaryAmount: summaryAmount?.textContent,
        summaryServiceCharge: summaryServiceCharge?.textContent,
        summaryTotal: summaryTotal?.textContent
    });
}

// Select amount function for program details page
function selectAmount(amount, clickedButton = null) {
    console.log('selectAmount called with amount:', amount, 'button:', clickedButton);
    selectedAmount = amount;
    
    // Update active state
    document.querySelectorAll('.amount-btn').forEach(el => {
        el.classList.remove('active');
    });
    
    // Add active class to clicked button
    if (clickedButton) {
        clickedButton.classList.add('active');
        console.log('Added active class to button:', clickedButton);
    }
    
    // Clear custom amount input
    const customAmountInput = document.getElementById('anonymousAmount');
    if (customAmountInput) {
        customAmountInput.value = '';
        console.log('Cleared custom amount input');
    }
    
    updateDonationSummary();
    console.log('Updated donation summary with amount:', selectedAmount);
}

// Make functions globally available
window.selectDonationOption = selectDonationOption;
window.selectAmount = selectAmount;
window.openDonationModal = openDonationModal;
window.closeDonationModal = closeDonationModal;
window.openPaymentModal = openPaymentModal;
window.closePaymentModal = closePaymentModal;
window.printPaymentSlip = printPaymentSlip;
window.selectDonorType = selectDonorType;
window.backToDonorTypeSelection = backToDonorTypeSelection;
window.handleFileUpload = handleFileUpload;
window.closeDonationSuccessModal = closeDonationSuccessModal;
window.downloadReceipt = downloadReceipt;
window.updateDonationSummary = updateDonationSummary;
