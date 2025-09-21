package lk.kolitha.dana.service.impl;

import lk.kolitha.dana.dto.donation.DonationCreateResponseDto;
import lk.kolitha.dana.dto.donation.DonationRequestDto;
import lk.kolitha.dana.dto.donation.DonationResponseDto;
import lk.kolitha.dana.dto.donor.DonationHistoryResponseDto;
import lk.kolitha.dana.entity.Campaigns;
import lk.kolitha.dana.entity.Donation;
import lk.kolitha.dana.entity.RegisteredDonor;
import lk.kolitha.dana.enums.Status;
import lk.kolitha.dana.exception.CustomServiceException;
import lk.kolitha.dana.repository.CampaignsRepository;
import lk.kolitha.dana.repository.CharityRepository;
import lk.kolitha.dana.repository.DonationRepository;
import lk.kolitha.dana.repository.RegisteredDonorRepository;
import lk.kolitha.dana.service.DonationService;
import lk.kolitha.dana.util.AwsFileHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static lk.kolitha.dana.constants.ApplicationConstants.S3FolderConstants.PAYMENT_SLIP;

@Service
@RequiredArgsConstructor
@Log4j2
public class DonationServiceImpl implements DonationService {
    
    private final DonationRepository donationRepository;
    private final CharityRepository charityRepository;
    private final CampaignsRepository campaignsRepository;
    private final RegisteredDonorRepository registeredDonorRepository;
    private final AwsFileHandler awsFileHandler;
    
    @Override
    public Page<DonationResponseDto> getCharityDonations(Long charityId, 
                                                        java.util.Date startDate, 
                                                        java.util.Date endDate, 
                                                        String donorEmail, 
                                                        Boolean isAnonymous, 
                                                        Long programId, 
                                                        Pageable pageable) {
        log.info("Getting donations for charity ID: {} with filters", charityId);
        
        // Verify charity exists
        charityRepository.findById(charityId)
                .orElseThrow(() -> new CustomServiceException("Charity not found with id: " + charityId));
        
        // Prepare date range for end date (set to end of day)
        java.util.Date processedEndDate = endDate;
        if (processedEndDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(processedEndDate);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            processedEndDate = cal.getTime();
        }
        
        // Get paginated donations with filters
        Page<Donation> donations = donationRepository.findDonationsByCharityIdWithFilters(
                charityId,
                startDate,
                processedEndDate,
                donorEmail,
                isAnonymous,
                programId,
                pageable
        );
        
        // Convert to DTOs
        Page<DonationResponseDto> donationDtos = donations.map(this::convertToDonationResponseDto);
        
        log.info("Retrieved {} donations for charity ID: {}", donationDtos.getTotalElements(), charityId);
        return donationDtos;
    }
    
    private DonationResponseDto convertToDonationResponseDto(Donation donation) {
        DonationResponseDto dto = new DonationResponseDto();
        
        // Basic donation information
        dto.setId(donation.getId());
        dto.setActualDonationAmount(donation.getActualDonationAmount());
        dto.setNetDonationAmount(donation.getNetDonationAmount());
        dto.setServiceCharge(donation.getServiceCharge());
        dto.setIsAnonymousDonation(donation.getIsAnonymousDonation());
        dto.setComments(donation.getComments());
        dto.setStatus(donation.getStatus() != null ? donation.getStatus().toString() : null);
        dto.setPaymentMethod(donation.getPaymentMethod() != null ? donation.getPaymentMethod().toString() : null);
        dto.setPaymentSlipUrl(donation.getPaymentSlipUrl());
        dto.setPaymentReferenceNumber(donation.getPaymentReferenceNumber());
        dto.setCreated(donation.getCreated());
        dto.setUpdated(donation.getUpdated());
        
        // Program information
        if (donation.getCampaigns() != null) {
            dto.setProgramId(donation.getCampaigns().getId());
            dto.setProgramName(donation.getCampaigns().getProgramName());
            dto.setProgramTitle(donation.getCampaigns().getTitle());
            dto.setProgramUrlSlug(donation.getCampaigns().getUrlName());
        }
        
        // Donor information (only if not anonymous)
        if (donation.getRegisteredDonor() != null && !Boolean.TRUE.equals(donation.getIsAnonymousDonation())) {
            dto.setDonorId(donation.getRegisteredDonor().getId());
            String fullName = donation.getRegisteredDonor().getFirstName() + " " + donation.getRegisteredDonor().getLastName();
            dto.setDonorName(fullName);
            dto.setDonorDisplayName(fullName);
            dto.setDonorEmail(donation.getRegisteredDonor().getEmail());
            dto.setDonorMobile(donation.getRegisteredDonor().getPhoneNumber());
            dto.setDonorProfileImage(donation.getRegisteredDonor().getProfileImageUrl());
        } else {
            // For anonymous donations, don't expose donor information
            dto.setDonorId(null);
            dto.setDonorName("Anonymous");
            dto.setDonorDisplayName("Anonymous Donor");
            dto.setDonorEmail(null);
            dto.setDonorMobile(null);
            dto.setDonorProfileImage(null);
        }
        
        // Calculate completion percentage for the program
        if (donation.getCampaigns() != null && 
            donation.getCampaigns().getTargetDonationAmount() != null && 
            donation.getCampaigns().getTargetDonationAmount().compareTo(java.math.BigDecimal.ZERO) > 0 &&
            donation.getCampaigns().getRaised() != null) {
            
            java.math.BigDecimal completionPercentage = donation.getCampaigns().getRaised()
                    .divide(donation.getCampaigns().getTargetDonationAmount(), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(java.math.BigDecimal.valueOf(100));
            dto.setCompletionPercentage(completionPercentage);
        } else {
            dto.setCompletionPercentage(java.math.BigDecimal.ZERO);
        }
        
        // Format donation date
        if (donation.getCreated() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm");
            dto.setDonationDateFormatted(dateFormat.format(donation.getCreated()));
        } else {
            dto.setDonationDateFormatted("N/A");
        }
        
        return dto;
    }
    
    @Override
    @Transactional
    public DonationCreateResponseDto createDonation(DonationRequestDto donationRequest, Long authenticatedDonorId) {
        log.info("Creating donation for campaign ID: {} with amount: {}", 
                donationRequest.getCampaignId(), donationRequest.getActualDonationAmount());
        
        // Validate campaign exists and is active
        Campaigns campaign = campaignsRepository.findById(donationRequest.getCampaignId())
                .orElseThrow(() -> new CustomServiceException("Campaign not found with id: " + donationRequest.getCampaignId()));
        
        if (campaign.isDeleted()) {
            throw new CustomServiceException("Campaign is no longer available");
        }
        
        if (campaign.getStatus() != Status.ACTIVE) {
            throw new CustomServiceException("Campaign is not currently active");
        }
        
        // Get authenticated donor if available
        RegisteredDonor authenticatedDonor = null;
        if (authenticatedDonorId != null) {
            authenticatedDonor = registeredDonorRepository.findById(authenticatedDonorId)
                    .orElseThrow(() -> new CustomServiceException("Authenticated donor not found"));
        }
        
        // Create donation entity
        Donation donation = new Donation();
        donation.setActualDonationAmount(donationRequest.getActualDonationAmount());
        donation.setCampaigns(campaign);
        donation.setComments(donationRequest.getComments());
        donation.setPaymentMethod(donationRequest.getPaymentMethod());
        donation.setStatus(Status.PENDING); // Initial status
        donation.setPaymentReferenceNumber("DON-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        
        // Handle payment slip file upload
        String paymentSlipUrl = null;
        if (donationRequest.getPaymentSlipUrl() != null && !donationRequest.getPaymentSlipUrl().isEmpty()) {
            try {
                paymentSlipUrl = awsFileHandler.uploadToS3Bucket(donationRequest.getPaymentSlipUrl(), donation.getPaymentReferenceNumber(), PAYMENT_SLIP)
                                 .orElseThrow(() -> new CustomServiceException("Failed to save payment slip file"));
                donation.setPaymentSlipUrl(paymentSlipUrl);
            } catch (Exception e) {
                log.error("Error saving payment slip file: {}", e.getMessage());
                throw new CustomServiceException("Failed to save payment slip file: " + e.getMessage());
            }
        }

        // Calculate service charge (assuming 2.5% service charge)
        double serviceChargePercentage = 2.5;
        double serviceCharge = donationRequest.getActualDonationAmount().doubleValue() * (serviceChargePercentage / 100);
        donation.setServiceCharge(serviceCharge);
        
        // Calculate net donation amount
        BigDecimal netAmount = donationRequest.getActualDonationAmount()
                .subtract(BigDecimal.valueOf(serviceCharge))
                .setScale(2, RoundingMode.HALF_UP);
        donation.setNetDonationAmount(netAmount);
        
        // Set donor information based on anonymous flag and authentication
            if (authenticatedDonor != null) {
                // Use authenticated donor
                donation.setIsAnonymousDonation(false);
                donation.setRegisteredDonor(authenticatedDonor);
            } else {
                donation.setIsAnonymousDonation(true);
            }
        
        // Set timestamps
        Date now = new Date();
        donation.setCreated(now);
        donation.setUpdated(now);
        
        // Save donation
        Donation savedDonation = donationRepository.save(donation);
        
        // Update campaign raised amount
        BigDecimal currentRaised = campaign.getRaised() != null ? campaign.getRaised() : BigDecimal.ZERO;
        campaign.setRaised(currentRaised.add(netAmount));
        campaign.setUpdated(now);
        campaignsRepository.save(campaign);
        
        log.info("Donation created successfully with ID: {} and reference: {}", 
                savedDonation.getId(), savedDonation.getPaymentReferenceNumber());
        
        // Create response DTO
        DonationCreateResponseDto response = new DonationCreateResponseDto();
        response.setDonationId(savedDonation.getId());
        response.setActualDonationAmount(savedDonation.getActualDonationAmount());
        response.setNetDonationAmount(savedDonation.getNetDonationAmount());
        response.setServiceCharge(savedDonation.getServiceCharge());
        response.setIsAnonymousDonation(savedDonation.getIsAnonymousDonation());
        response.setComments(savedDonation.getComments());
        response.setStatus(savedDonation.getStatus().toString());
        response.setPaymentMethod(savedDonation.getPaymentMethod().toString());
        response.setPaymentSlipUrl(paymentSlipUrl);
        response.setPaymentReferenceNumber(savedDonation.getPaymentReferenceNumber());
        response.setCreated(savedDonation.getCreated());
        
        // Campaign information
        response.setCampaignId(campaign.getId());
        response.setCampaignName(campaign.getProgramName());
        response.setCampaignTitle(campaign.getTitle());
        response.setMessage("Thank you for your donation. Your upload slip under review and will be sent to you shortly.");
        return response;
    }
    
    @Override
    public Page<DonationHistoryResponseDto> getDonorDonationHistory(Long donorId, 
                                                                   java.util.Date startDate, 
                                                                   java.util.Date endDate,
                                                                   Pageable pageable) {
        log.info("Getting donation history for donor ID: {} with filters", donorId);
        
        // Verify donor exists
        registeredDonorRepository.findById(donorId)
                .orElseThrow(() -> new CustomServiceException("Donor not found with id: " + donorId));
        
        // Prepare date range for end date (set to end of day)
        java.util.Date processedEndDate = endDate;
        if (processedEndDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(processedEndDate);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            processedEndDate = cal.getTime();
        }
        
        // Get paginated donations with filters
        Page<Donation> donations = donationRepository.findDonationsByDonorIdWithFilters(
                donorId,
                startDate,
                processedEndDate,
                pageable
        );
        
        // Convert to DTOs
        Page<DonationHistoryResponseDto> donationDtos = donations.map(this::convertToDonationHistoryResponseDto);
        
        log.info("Retrieved {} donations for donor ID: {}", donationDtos.getTotalElements(), donorId);
        return donationDtos;
    }
    
    private DonationHistoryResponseDto convertToDonationHistoryResponseDto(Donation donation) {
        DonationHistoryResponseDto dto = new DonationHistoryResponseDto();
        
        // Basic donation information
        dto.setDonationId(donation.getId());
        dto.setActualDonationAmount(donation.getActualDonationAmount());
        dto.setNetDonationAmount(donation.getNetDonationAmount());
        dto.setServiceCharge(donation.getServiceCharge());
        dto.setIsAnonymousDonation(donation.getIsAnonymousDonation());
        dto.setComments(donation.getComments());
        dto.setStatus(donation.getStatus() != null ? donation.getStatus().toString() : null);
        dto.setPaymentMethod(donation.getPaymentMethod() != null ? donation.getPaymentMethod().toString() : null);
        dto.setPaymentSlipUrl(donation.getPaymentSlipUrl());
        dto.setPaymentReferenceNumber(donation.getPaymentReferenceNumber());
        dto.setCreated(donation.getCreated());
        dto.setUpdated(donation.getUpdated());
        
        // Campaign information
        if (donation.getCampaigns() != null) {
            Campaigns campaign = donation.getCampaigns();
            dto.setCampaignId(campaign.getId());
            dto.setCampaignName(campaign.getProgramName());
            dto.setCampaignTitle(campaign.getTitle());
            dto.setCampaignUrlSlug(campaign.getUrlName());
            dto.setCampaignImage(campaign.getProgramImage());
            dto.setCampaignDescription(campaign.getDescription());
            dto.setCampaignTargetAmount(campaign.getTargetDonationAmount());
            dto.setCampaignRaisedAmount(campaign.getRaised());
            dto.setCampaignStartDate(campaign.getStartDate());
            dto.setCampaignEndDate(campaign.getEndDate());
            dto.setCampaignStatus(campaign.getStatus() != null ? campaign.getStatus().toString() : null);
            
            // Calculate campaign completion percentage
            if (campaign.getTargetDonationAmount() != null && 
                campaign.getTargetDonationAmount().compareTo(java.math.BigDecimal.ZERO) > 0 &&
                campaign.getRaised() != null) {
                
                java.math.BigDecimal completionPercentage = campaign.getRaised()
                        .divide(campaign.getTargetDonationAmount(), 4, java.math.RoundingMode.HALF_UP)
                        .multiply(java.math.BigDecimal.valueOf(100));
                dto.setCampaignCompletionPercentage(completionPercentage);
            } else {
                dto.setCampaignCompletionPercentage(java.math.BigDecimal.ZERO);
            }
            
            // Charity information
            if (campaign.getCharity() != null) {
                dto.setCharityId(campaign.getCharity().getId());
                dto.setCharityName(campaign.getCharity().getName());
                dto.setCharityLogo(campaign.getCharity().getLogoUrl());
            }
            
            // Category information
            if (campaign.getSubCategory() != null) {
                dto.setSubCategoryId(campaign.getSubCategory().getId());
                dto.setSubCategoryName(campaign.getSubCategory().getName());
                
                if (campaign.getSubCategory().getCategory() != null) {
                    dto.setCategoryId(campaign.getSubCategory().getCategory().getId());
                    dto.setCategoryName(campaign.getSubCategory().getCategory().getName());
                }
            }
        }
        
        // Format donation date
        if (donation.getCreated() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm");
            dto.setDonationDateFormatted(dateFormat.format(donation.getCreated()));
        } else {
            dto.setDonationDateFormatted("N/A");
        }
        
        // Set donor display name
        if (donation.getRegisteredDonor() != null && !Boolean.TRUE.equals(donation.getIsAnonymousDonation())) {
            String fullName = donation.getRegisteredDonor().getFirstName() + " " + donation.getRegisteredDonor().getLastName();
            dto.setDonorDisplayName(fullName);
        } else {
            dto.setDonorDisplayName("Anonymous Donor");
        }
        
        return dto;
    }
 
}
