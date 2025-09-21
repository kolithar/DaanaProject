package lk.kolitha.dana.service.impl;


import lk.kolitha.dana.dto.CharityDto;
import lk.kolitha.dana.dto.charity.CharityRegistrationStep1Dto;
import lk.kolitha.dana.dto.charity.CharityRegistrationStep2Dto;
import lk.kolitha.dana.dto.charity.CharityRegistrationStep3Dto;
import lk.kolitha.dana.dto.charity.CharityRegistrationResponseDto;
import lk.kolitha.dana.dto.charity.CharityProfileDto;
import lk.kolitha.dana.dto.charity.PasswordChangeDto;
import lk.kolitha.dana.dto.charity.ProfileUpdateDto;
import lk.kolitha.dana.entity.BankBetail;
import lk.kolitha.dana.entity.Charity;
import lk.kolitha.dana.entity.CharityProofDocument;
import lk.kolitha.dana.enums.CharityProofDocumentType;
import lk.kolitha.dana.enums.Status;
import lk.kolitha.dana.exception.CustomServiceException;
import lk.kolitha.dana.repository.CharityRepository;
import lk.kolitha.dana.repository.CampaignsRepository;
import lk.kolitha.dana.repository.DonationRepository;
import lk.kolitha.dana.service.CharityService;
import lk.kolitha.dana.util.AwsFileHandler;
import lk.kolitha.dana.util.CustomGenerator;
import lk.kolitha.dana.util.DaanaSESEmailSender;
import lk.kolitha.dana.constants.ApplicationConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Log4j2
public class CharityServiceImpl implements CharityService {

    private final CharityRepository charityRepository;
    private final CampaignsRepository campaignsRepository;
    private final DonationRepository donationRepository;
    private final PasswordEncoder passwordEncoder;
    private final DaanaSESEmailSender emailSender;
    private final AwsFileHandler awsFileHandler;

    @Override
    public void registerCharity(CharityDto charityDto) {
        Charity charity = new Charity();
        charity.setName(charityDto.getName());
        charity.setEmail(charityDto.getEmail());
        charity.setPasswordHash(passwordEncoder.encode(charityDto.getPassword()));
        charity.setExecutionType(charityDto.getExecutionType());
        charity.setCreated(new Date());
        charity.setUpdated(new Date());

        // mark as PENDING until admin reviews
        charity.setStatus(Status.PENDING);

        charityRepository.save(charity);
    }

    @Override
    public Charity approveCharity(Long charityId) {
        Charity charity = charityRepository.findById(charityId)
                .orElseThrow(() -> new RuntimeException("Charity not found"));
        charity.setStatus(Status.ACTIVE);
        charity.setUpdated(new Date());
        return charityRepository.save(charity);
    }

    @Override
    public Charity rejectCharity(Long charityId) {
        Charity charity = charityRepository.findById(charityId)
                .orElseThrow(() -> new RuntimeException("Charity not found"));
        charity.setStatus(Status.INACTIVE);
        charity.setUpdated(new Date());
        return charityRepository.save(charity);
    }

    @Override
    @Transactional
    public CharityRegistrationResponseDto registerCharityStep1(CharityRegistrationStep1Dto step1Dto) {
        log.info("Starting charity registration step 1 for email: {}", step1Dto.getEmail());
        
        try {
            // Check if charity already exists
            Optional<Charity> existingCharity = charityRepository.findByEmail(step1Dto.getEmail());
            if (existingCharity.isPresent()) {
                log.error("Charity registration failed: Email already exists - {}", step1Dto.getEmail());
                throw new CustomServiceException(400, "Email address is already registered");
            }
            
            // Create new charity entity
            Charity charity = new Charity();
            charity.setName(step1Dto.getName());
            charity.setEmail(step1Dto.getEmail());
            charity.setPasswordHash(passwordEncoder.encode(step1Dto.getPassword()));
            charity.setExecutionType(step1Dto.getExecutionType());
            charity.setWebsite(step1Dto.getWebsite());
            charity.setDescription(step1Dto.getDescription());
            charity.setMobileNumber(step1Dto.getMobileNumber());
            charity.setNicNumberOrRegistrationNumber(step1Dto.getNicNumberOrRegistrationNumber());
            charity.setContactPersonName(step1Dto.getContactPersonName());
            charity.setContactPersonMobile(step1Dto.getContactPersonMobile());
            charity.setContactPersonEmail(step1Dto.getContactPersonEmail());
            charity.setStatus(Status.DRAFT);
            charity.setAccountVerifyStatus(false);
            charity.setCreated(new Date());
            charity.setUpdated(new Date());
            
            // Generate and set OTP
            String otpCode = CustomGenerator.generateSixDigitOtp();
            charity.setOtpCode(otpCode);
            charity.setOtpCodeGeneratedTimestamp(new Date());
            
            // Save charity
            Charity savedCharity = charityRepository.save(charity);
            
            // Send OTP email
            sendCharityOtpEmail(step1Dto.getEmail(), step1Dto.getName(), otpCode);
            
            log.info("Charity registration step 1 completed successfully for email: {}", step1Dto.getEmail());
            
            // Create and return response DTO
            return createCharityResponseDto(savedCharity);
            
        } catch (CustomServiceException e) {
            log.error("Charity registration step 1 failed with custom exception: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during charity registration step 1 for email: {}. Error: {}", 
                     step1Dto.getEmail(), e.getMessage(), e);
            throw new CustomServiceException(500, "Registration failed. Please try again later.");
        }
    }

    @Override
    @Transactional
    public CharityRegistrationResponseDto registerCharityStep2(CharityRegistrationStep2Dto step2Dto) {
        log.info("Starting charity registration step 2 for email: {}", step2Dto.getId());
        
        try {
            // Find charity by email
            Charity charity = charityRepository.findById(step2Dto.getId())
                    .orElseThrow(() -> new CustomServiceException(404, "Charity registration not found. Please complete step 1 first."));
            
            // Check if account is already verified
            if (charity.isAccountVerifyStatus()) {
                log.warn("Charity registration step 2 failed: Account already verified for email: {}", step2Dto.getId());
                throw new CustomServiceException(400, "Account is already verified. No need to upload documents.");
            }
            
            // Validate document type based on execution type
            if (!step2Dto.isValidDocumentForExecutionType(charity.getExecutionType())) {
                CharityProofDocumentType requiredDocType = CharityRegistrationStep2Dto.getRequiredDocumentType(charity.getExecutionType());
                String errorMessage = String.format("Invalid document type. For %s execution type, you must upload %s document.", 
                    charity.getExecutionType().name(), 
                    requiredDocType != null ? requiredDocType.name() : "appropriate");
                log.error("Charity registration step 2 failed: {} for email: {}", errorMessage, step2Dto.getId());
                throw new CustomServiceException(400, errorMessage);
            }
            
            // Upload document file to S3
            String documentFileName = charity.getId() + "_" + step2Dto.getDocumentTypeAsEnum().name() + "_" + 
                            System.currentTimeMillis();

            Optional<String> documentFileUrl = awsFileHandler.uploadToS3Bucket(
                step2Dto.getDocumentFile(), 
                documentFileName, ApplicationConstants.S3FolderConstants.CHARITY_DOCUMENTS
            );
            
            if (documentFileUrl.isEmpty()) {
                log.error("Failed to upload document to S3 for charity: {}", step2Dto.getId());
                throw new CustomServiceException(500, "Failed to upload document. Please try again later.");
            }

            // Upload logo file to S3 (if provided)
            String logoUrl = null;
            if (step2Dto.getLogoFile() != null && !step2Dto.getLogoFile().isEmpty()) {
                String logoFileName = charity.getId() + "_logo_" + System.currentTimeMillis();
                
                Optional<String> logoFileUrl = awsFileHandler.uploadToS3Bucket(
                    step2Dto.getLogoFile(), 
                    logoFileName, ApplicationConstants.S3FolderConstants.LOGO_IMAGES
                );
                
                if (logoFileUrl.isPresent()) {
                    logoUrl = logoFileUrl.get();
                    charity.setLogoUrl(logoUrl);
                } else {
                    log.warn("Failed to upload logo to S3 for charity: {}, continuing without logo", step2Dto.getId());
                }
            }
            
            // Create charity proof document
            CharityProofDocument document = new CharityProofDocument();
            document.setCharityProofDocumentType(step2Dto.getDocumentTypeAsEnum());
            document.setFileName(documentFileUrl.get()); // Store the full S3 URL
            document.setStatus(Status.PENDING);
            document.setPublic(false);
            document.setCharity(charity);
            document.setCreated(new Date());
            document.setUpdated(new Date());
            
            // Add document to charity
            charity.getCharityProofDocuments().add(document);
            
            // Create bank details
            BankBetail bankDetail = new BankBetail();
            bankDetail.setBankName(step2Dto.getBankName());
            bankDetail.setBranchName(step2Dto.getBranchName());
            bankDetail.setAccountHolderName(step2Dto.getAccountHolderName());
            bankDetail.setSwiftCode(step2Dto.getSwiftCode());
            bankDetail.setAccountNumber(step2Dto.getAccountNumber());
            bankDetail.setCharity(charity);
            bankDetail.setCreated(new Date());
            bankDetail.setUpdated(new Date());
            
            // Set bank detail to charity
            charity.setBankDetail(bankDetail);
            charity.setUpdated(new Date());
            
            // Save charity with documents and bank details
            Charity savedCharity = charityRepository.save(charity);
            
            log.info("Charity registration step 2 completed successfully for email: {}", step2Dto.getId());
            
            // Create and return response DTO with document and bank details
            return createCharityResponseDtoWithDocuments(savedCharity, step2Dto);
            
        } catch (CustomServiceException e) {
            log.error("Charity registration step 2 failed with custom exception: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during charity registration step 2 for email: {}. Error: {}", 
                     step2Dto.getId(), e.getMessage(), e);
            throw new CustomServiceException(500, "Document upload failed. Please try again later.");
        }
    }

    @Override
    @Transactional
    public CharityRegistrationResponseDto verifyCharityOtp(CharityRegistrationStep3Dto step3Dto) {
        log.info("Starting charity OTP verification for ID: {}", step3Dto.getId());
        
        try {
            // Find charity by email
            Charity charity = charityRepository.findById(step3Dto.getId())
                    .orElseThrow(() -> new CustomServiceException(404, "Charity registration not found. Please complete step 1 first."));
            
            // Check if account is already verified
            if (charity.isAccountVerifyStatus()) {
                log.warn("Charity OTP verification failed: Account already verified for email: {}", step3Dto.getId());
                throw new CustomServiceException(400, "Account is already verified.");
            }
            
            // Validate OTP code
            if (charity.getOtpCode() == null || !charity.getOtpCode().equals(step3Dto.getOtpCode())) {
                log.error("Charity OTP verification failed: Invalid OTP code for email: {}", step3Dto.getId());
                throw new CustomServiceException(400, "Invalid OTP code. Please check and try again.");
            }
            
            // Check if OTP is expired (10 minutes)
            if (charity.getOtpCodeGeneratedTimestamp() == null) {
                log.error("Charity OTP verification failed: No timestamp found for OTP code for email: {}", step3Dto.getId());
                throw new CustomServiceException(400, "OTP code has expired. Please request a new one.");
            }
            
            long currentTime = System.currentTimeMillis();
            long otpTime = charity.getOtpCodeGeneratedTimestamp().getTime();
            long timeDifference = currentTime - otpTime;
            long tenMinutesInMillis = 10 * 60 * 1000; // 10 minutes
            
            if (timeDifference > tenMinutesInMillis) {
                log.warn("Charity OTP verification failed: OTP code expired for email: {}", step3Dto.getId());
                throw new CustomServiceException(400, "OTP code has expired. Please request a new one.");
            }
            
            // Verify the account
            charity.setAccountVerifyStatus(true);
            charity.setOtpCode(null); // Clear the OTP code after successful verification
            charity.setOtpCodeGeneratedTimestamp(null);
            charity.setUpdated(new Date());
            
            Charity savedCharity = charityRepository.save(charity);
            
            log.info("Charity OTP verification successful for email: {}", step3Dto.getId());
            
            // Create and return response DTO
            return createCharityResponseDto(savedCharity);
            
        } catch (CustomServiceException e) {
            log.error("Charity OTP verification failed with custom exception: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during charity OTP verification for email: {}. Error: {}", 
                     step3Dto.getId(), e.getMessage(), e);
            throw new CustomServiceException(500, "OTP verification failed. Please try again later.");
        }
    }

    @Override
    @Transactional
    public void resendCharityOtp(String email) {
        log.info("Resend charity OTP request received for email: {}", email);
        
        try {
            // Validate input parameter
            if (email == null || email.trim().isEmpty()) {
                log.error("Resend charity OTP failed: Email is required");
                throw new CustomServiceException(400, "Email is required to resend OTP");
            }
            
            // Find the charity by email
            Charity charity = charityRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomServiceException(404, "Charity registration not found with email: " + email));
            
            // Check if account is already verified
            if (charity.isAccountVerifyStatus()) {
                log.warn("Resend charity OTP failed: Account is already verified for email: {}", email);
                throw new CustomServiceException(400, "Account is already verified. No need to resend OTP.");
            }
            
            // Generate new OTP code
            String newOtpCode = CustomGenerator.generateSixDigitOtp();
            
            // Update charity with new OTP
            charity.setOtpCode(newOtpCode);
            charity.setOtpCodeGeneratedTimestamp(new Date());
            charity.setUpdated(new Date());
            
            charityRepository.save(charity);
            
            // Send new OTP verification email
            sendCharityOtpEmail(charity.getEmail(), charity.getName(), newOtpCode);
            
            log.info("Charity OTP resent successfully for email: {}", email);
            
        } catch (CustomServiceException e) {
            log.error("Resend charity OTP failed with custom exception: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during resend charity OTP for email: {}. Error: {}", 
                     email, e.getMessage(), e);
            throw new CustomServiceException(500, "Failed to resend OTP. Please try again later.");
        }
    }

    /**
     * Sends OTP verification email to the charity
     * @param email Charity's email address
     * @param charityName Charity's name
     * @param otpCode Generated OTP code
     */
    private void sendCharityOtpEmail(String email, String charityName, String otpCode) {
        try {
            log.info("Sending OTP verification email to charity: {}", email);
            
            String subject = "Verify Your Charity Registration - Daana.lk";
            String htmlContent = loadAndPopulateCharityOtpEmailTemplate(charityName, otpCode);
            
            emailSender.sendHtmEmail(email, subject, htmlContent);
            log.info("OTP verification email sent successfully to charity: {}", email);
            
        } catch (Exception e) {
            log.error("Failed to send OTP verification email to charity: {}. Error: {}", email, e.getMessage(), e);
            // Don't throw exception here as registration should still succeed
            // The user can request a new OTP later
        }
    }

    /**
     * Loads the OTP email template and populates it with charity data
     * @param charityName Charity's name
     * @param otpCode Generated OTP code
     * @return HTML email content with populated data
     */
    private String loadAndPopulateCharityOtpEmailTemplate(String charityName, String otpCode) {
        try {
            log.debug("Loading charity OTP email template from resources");
            
            // Load the HTML template from resources
            ClassPathResource resource = new ClassPathResource("EmailTemplates/OTPEmailTemplates.html");
            String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            
            // Replace placeholders with actual values
            String populatedTemplate = template
                    .replace("{{firstName}}", charityName != null ? charityName : "Charity")
                    .replace("{{otpCode}}", otpCode != null ? otpCode : "000000");
            
            log.debug("Charity OTP email template loaded and populated successfully");
            return populatedTemplate;
            
        } catch (IOException e) {
            log.error("Failed to load charity OTP email template: {}", e.getMessage(), e);
            throw new CustomServiceException(500, "Failed to load OTP email template. Please try again later.");
        }
    }

    /**
     * Creates a basic charity response DTO from charity entity
     */
    private CharityRegistrationResponseDto createCharityResponseDto(Charity charity) {
        CharityRegistrationResponseDto responseDto = new CharityRegistrationResponseDto();
        responseDto.setId(charity.getId());
        responseDto.setName(charity.getName());
        responseDto.setEmail(charity.getEmail());
        responseDto.setExecutionType(charity.getExecutionType());
        responseDto.setWebsite(charity.getWebsite());
        responseDto.setDescription(charity.getDescription());
        responseDto.setLogoUrl(charity.getLogoUrl());
        responseDto.setMobileNumber(charity.getMobileNumber());
        responseDto.setNicNumberOrRegistrationNumber(charity.getNicNumberOrRegistrationNumber());
        responseDto.setContactPersonName(charity.getContactPersonName());
        responseDto.setContactPersonMobile(charity.getContactPersonMobile());
        responseDto.setContactPersonEmail(charity.getContactPersonEmail());
        responseDto.setStatus(charity.getStatus());
        responseDto.setAccountVerifyStatus(charity.isAccountVerifyStatus());
        responseDto.setCreated(charity.getCreated());
        responseDto.setUpdated(charity.getUpdated());
        return responseDto;
    }

    /**
     * Creates a charity response DTO with document and bank details
     */
    private CharityRegistrationResponseDto createCharityResponseDtoWithDocuments(Charity charity, CharityRegistrationStep2Dto step2Dto) {
        CharityRegistrationResponseDto responseDto = createCharityResponseDto(charity);
        
        // Add document information
        responseDto.setDocumentType(step2Dto.getDocumentType());
        responseDto.setDocumentFileName(step2Dto.getDocumentFile().getOriginalFilename());
        
        // Add logo information if uploaded
        if (step2Dto.getLogoFile() != null && !step2Dto.getLogoFile().isEmpty()) {
            responseDto.setLogoUrl(charity.getLogoUrl());
        }
        
        // Add bank details
        responseDto.setBankName(step2Dto.getBankName());
        responseDto.setBranchName(step2Dto.getBranchName());
        responseDto.setAccountHolderName(step2Dto.getAccountHolderName());
        responseDto.setSwiftCode(step2Dto.getSwiftCode());
        responseDto.setAccountNumber(step2Dto.getAccountNumber());
        
        return responseDto;
    }
    
    @Override
    public CharityProfileDto getCharityProfile(Long charityId) {
        log.info("Getting charity profile for charity ID: {}", charityId);
        
        // Find charity by ID
        Charity charity = charityRepository.findById(charityId)
                .orElseThrow(() -> new CustomServiceException("Charity not found with id: " + charityId));
        
        // Create profile DTO
        CharityProfileDto profileDto = new CharityProfileDto();
        
        // Basic Information
        profileDto.setId(charity.getId());
        profileDto.setName(charity.getName());
        profileDto.setEmail(charity.getEmail());
        profileDto.setDescription(charity.getDescription());
        profileDto.setExecutionType(charity.getExecutionType() != null ? charity.getExecutionType().toString() : null);
        profileDto.setLogoUrl(charity.getLogoUrl());
        profileDto.setContactPersonName(charity.getContactPersonName());
        profileDto.setContactPersonMobile(String.valueOf(charity.getContactPersonMobile()));
        profileDto.setNicNumberOrRegistrationNumber(charity.getNicNumberOrRegistrationNumber());
        profileDto.setWebsite(charity.getWebsite());
        profileDto.setPhoneNumber(String.valueOf(charity.getMobileNumber()));
        
        // Status Information
        profileDto.setStatus(charity.getStatus() != null ? charity.getStatus().toString() : null);
        profileDto.setAccountVerifyStatus(charity.isAccountVerifyStatus());
        profileDto.setDeleted(charity.isDeleted());
        
        // Document Information - Get from CharityProofDocument
        List<CharityProofDocument> documents = charity.getCharityProofDocuments();
        if (documents != null && !documents.isEmpty()) {
            for (CharityProofDocument doc : documents) {
                switch (doc.getCharityProofDocumentType()) {
                    case BUSINESS_REGISTRATION_CERTIFICATE:
                        profileDto.setRegistrationDocumentUrl(doc.getFileName());
                        break;
                    case ID_CARD:
                        profileDto.setTaxDocumentUrl(doc.getFileName());
                        break;
                    case PASSPORT:
                        profileDto.setBankDocumentUrl(doc.getFileName());
                        break;
                    case OTHERS:
                        profileDto.setOtherDocumentUrl(doc.getFileName());
                        break;
                    default:
                        // Handle other document types if needed
                        break;
                }
            }
        }
        
        // Bank Information - Get from BankBetail
        BankBetail bankDetail = charity.getBankDetail();
        if (bankDetail != null) {
            profileDto.setBankName(bankDetail.getBankName());
            profileDto.setBankAccountNumber(bankDetail.getAccountNumber());
            profileDto.setBankBranch(bankDetail.getBranchName());
        }
        
        // Timestamps
        profileDto.setCreated(charity.getCreated());
        profileDto.setUpdated(charity.getUpdated());
        
        // Statistics
        try {
            List<lk.kolitha.dana.entity.Campaigns> programs = campaignsRepository.findByCharityIdAndDeletedFalse(charityId, Pageable.unpaged()).getContent();
            profileDto.setTotalPrograms((long) programs.size());
            profileDto.setActivePrograms(programs.stream()
                    .filter(p -> p.getStatus() == Status.ACTIVE)
                    .count());
            
            Long totalDonations = donationRepository.countDonationsByCharityId(charityId);
            profileDto.setTotalDonations(totalDonations);
            
            BigDecimal totalRaised = donationRepository.getTotalDonationAmountByCharityId(charityId);
            profileDto.setTotalRaisedAmount(totalRaised != null ? totalRaised.toString() : "0.00");
        } catch (Exception e) {
            log.warn("Error calculating statistics for charity ID: {}", charityId, e);
            profileDto.setTotalPrograms(0L);
            profileDto.setActivePrograms(0L);
            profileDto.setTotalDonations(0L);
            profileDto.setTotalRaisedAmount("0.00");
        }
        
        // Additional Information
        profileDto.setOptCode(charity.getOtpCode());
        profileDto.setOptCodeGeneratedTimestamp(charity.getOtpCodeGeneratedTimestamp());
        
        log.info("Charity profile retrieved successfully for charity ID: {}", charityId);
        return profileDto;
    }
    
    @Override
    @Transactional
    public void changePassword(Long charityId, PasswordChangeDto passwordChangeDto) {
        log.info("Changing password for charity ID: {}", charityId);
        
        // Find charity by ID
        Charity charity = charityRepository.findById(charityId)
                .orElseThrow(() -> new CustomServiceException("Charity not found with id: " + charityId));
        
        // Validate current password
        if (!passwordEncoder.matches(passwordChangeDto.getCurrentPassword(), charity.getPasswordHash())) {
            log.warn("Invalid current password for charity ID: {}", charityId);
            throw new CustomServiceException("Current password is incorrect");
        }
        
        // Validate new password confirmation
        if (!passwordChangeDto.getNewPassword().equals(passwordChangeDto.getConfirmPassword())) {
            log.warn("Password confirmation mismatch for charity ID: {}", charityId);
            throw new CustomServiceException("New password and confirm password do not match");
        }
        
        // Validate new password is different from current password
        if (passwordEncoder.matches(passwordChangeDto.getNewPassword(), charity.getPasswordHash())) {
            log.warn("New password is same as current password for charity ID: {}", charityId);
            throw new CustomServiceException("New password must be different from current password");
        }
        
        // Update password
        String newPasswordHash = passwordEncoder.encode(passwordChangeDto.getNewPassword());
        charity.setPasswordHash(newPasswordHash);
        charity.setUpdated(new Date());
        
        charityRepository.save(charity);
        
        log.info("Password changed successfully for charity ID: {}", charityId);
    }
    
    @Override
    @Transactional
    public CharityProfileDto updateProfile(Long charityId, ProfileUpdateDto profileUpdateDto) {
        log.info("Updating profile for charity ID: {}", charityId);
        
        // Find charity by ID
        Charity charity = charityRepository.findById(charityId)
                .orElseThrow(() -> new CustomServiceException("Charity not found with id: " + charityId));

        // Update basic information only if provided (not null and not empty)
        if (profileUpdateDto.getName() != null && !profileUpdateDto.getName().trim().isEmpty()) {
            charity.setName(profileUpdateDto.getName());
        }
        
        if (profileUpdateDto.getDescription() != null && !profileUpdateDto.getDescription().trim().isEmpty()) {
            charity.setDescription(profileUpdateDto.getDescription());
        }
        
        if (profileUpdateDto.getWebsite() != null && !profileUpdateDto.getWebsite().trim().isEmpty()) {
            charity.setWebsite(profileUpdateDto.getWebsite());
        }
        
        if (profileUpdateDto.getContactPersonName() != null && !profileUpdateDto.getContactPersonName().trim().isEmpty()) {
            charity.setContactPersonName(profileUpdateDto.getContactPersonName());
        }
        
        if (profileUpdateDto.getContactPersonMobile() != null && !profileUpdateDto.getContactPersonMobile().trim().isEmpty()) {
            charity.setContactPersonMobile(Integer.parseInt(profileUpdateDto.getContactPersonMobile()));
        }
        
        if (profileUpdateDto.getContactPersonEmail() != null && !profileUpdateDto.getContactPersonEmail().trim().isEmpty()) {
            charity.setContactPersonEmail(profileUpdateDto.getContactPersonEmail());
        }
        
        if (profileUpdateDto.getNicNumberOrRegistrationNumber() != null && !profileUpdateDto.getNicNumberOrRegistrationNumber().trim().isEmpty()) {
            charity.setNicNumberOrRegistrationNumber(profileUpdateDto.getNicNumberOrRegistrationNumber());
        }
        
        if (profileUpdateDto.getPhoneNumber() != null && !profileUpdateDto.getPhoneNumber().trim().isEmpty()) {
            charity.setMobileNumber(Integer.parseInt(profileUpdateDto.getPhoneNumber()));
        }
        
        // Update timestamp
        charity.setUpdated(new Date());
        
        // Save updated charity
        charityRepository.save(charity);
        
        log.info("Profile updated successfully for charity ID: {}", charityId);
        
        // Return updated profile
        return getCharityProfile(charityId);
    }
}
