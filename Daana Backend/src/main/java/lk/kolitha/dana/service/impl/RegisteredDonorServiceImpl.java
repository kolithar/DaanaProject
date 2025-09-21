package lk.kolitha.dana.service.impl;

import lk.kolitha.dana.dto.donor.DonorProfileResponseDto;
import lk.kolitha.dana.dto.donor.DonorProfileUpdateDto;
import lk.kolitha.dana.entity.RegisteredDonor;
import lk.kolitha.dana.exception.CustomServiceException;
import lk.kolitha.dana.repository.RegisteredDonorRepository;
import lk.kolitha.dana.service.RegisteredDonorService;
import lk.kolitha.dana.util.AwsFileHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.UUID;

import static lk.kolitha.dana.constants.ApplicationConstants.S3FolderConstants.PROFILE_IMAGES;

@Service
@RequiredArgsConstructor
@Log4j2
public class RegisteredDonorServiceImpl implements RegisteredDonorService {
    
    private final RegisteredDonorRepository registeredDonorRepository;
    private final AwsFileHandler awsFileHandler;
    
    @Override
    public DonorProfileResponseDto getDonorProfile(Long donorId) {
        log.info("Getting donor profile for ID: {}", donorId);
        
        RegisteredDonor donor = registeredDonorRepository.findById(donorId)
                .orElseThrow(() -> new CustomServiceException("Donor not found with id: " + donorId));
        
        return convertToProfileResponseDto(donor);
    }
    
    @Override
    public DonorProfileResponseDto getDonorProfileByEmail(String email) {
        log.info("Getting donor profile for email: {}", email);
        
        RegisteredDonor donor = registeredDonorRepository.findByEmail(email)
                .orElseThrow(() -> new CustomServiceException("Donor not found with email: " + email));
        
        return convertToProfileResponseDto(donor);
    }
    
    @Override
    @Transactional
    public DonorProfileResponseDto updateDonorProfile(Long donorId, DonorProfileUpdateDto profileUpdate) {
        log.info("Updating donor profile for ID: {}", donorId);
        
        RegisteredDonor donor = registeredDonorRepository.findById(donorId)
                .orElseThrow(() -> new CustomServiceException("Donor not found with id: " + donorId));

        
        // Update donor information - only update non-null/non-empty properties
        if (profileUpdate.getFirstName() != null && !profileUpdate.getFirstName().trim().isEmpty()) {
            donor.setFirstName(profileUpdate.getFirstName().trim());
        }
        
        if (profileUpdate.getLastName() != null && !profileUpdate.getLastName().trim().isEmpty()) {
            donor.setLastName(profileUpdate.getLastName().trim());
        }
        
        if (profileUpdate.getGender() != null) {
            donor.setGender(profileUpdate.getGender());
        }
        
        if (profileUpdate.getPhoneNumber() != null && !profileUpdate.getPhoneNumber().trim().isEmpty()) {
            donor.setPhoneNumber(profileUpdate.getPhoneNumber().trim());
        }
        
        if (profileUpdate.getBillingAddress() != null && !profileUpdate.getBillingAddress().trim().isEmpty()) {
            donor.setBillingAddress(profileUpdate.getBillingAddress().trim());
        }
        
        donor.setUpdated(new Date());
        
        RegisteredDonor savedDonor = registeredDonorRepository.save(donor);
        
        log.info("Donor profile updated successfully for ID: {}", donorId);
        return convertToProfileResponseDto(savedDonor);
    }
    
    @Override
    @Transactional
    public String updateProfilePicture(Long donorId, MultipartFile profileImage) {
        log.info("Updating profile picture for donor ID: {}", donorId);

        
        RegisteredDonor donor = registeredDonorRepository.findById(donorId)
                .orElseThrow(() -> new CustomServiceException("Donor not found with id: " + donorId));
        
        try {
            // Generate unique filename using donor ID and timestamp
            String uniqueFilename = "donor-" + donorId + "-" + UUID.randomUUID().toString().substring(0, 8);
            
            // Upload to S3
            String profileImageUrl = awsFileHandler.uploadToS3Bucket(profileImage, uniqueFilename, PROFILE_IMAGES)
                    .orElseThrow(() -> new CustomServiceException("Failed to upload profile image"));
            
            // Update donor profile image URL
            donor.setProfileImageUrl(profileImageUrl);
            donor.setUpdated(new Date());
            registeredDonorRepository.save(donor);
            
            log.info("Profile picture updated successfully for donor ID: {}", donorId);
            return profileImageUrl;
            
        } catch (Exception e) {
            log.error("Error updating profile picture for donor ID {}: {}", donorId, e.getMessage());
            throw new CustomServiceException("Failed to update profile picture: " + e.getMessage());
        }
    }
    
    private DonorProfileResponseDto convertToProfileResponseDto(RegisteredDonor donor) {
        DonorProfileResponseDto dto = new DonorProfileResponseDto();
        
        dto.setId(donor.getId());
        dto.setFirstName(donor.getFirstName());
        dto.setLastName(donor.getLastName());
        dto.setEmail(donor.getEmail());
        dto.setGender(donor.getGender());
        dto.setProfileImageUrl(donor.getProfileImageUrl());
        dto.setPhoneNumber(donor.getPhoneNumber());
        dto.setBillingAddress(donor.getBillingAddress());
        dto.setBillingVerify(donor.isBillingVerify());
        dto.setAccountVerifyStatus(donor.isAccountVerifyStatus());
        dto.setUniqueCustomerId(donor.getUniqueCustomerId());
        dto.setCreated(donor.getCreated());
        dto.setUpdated(donor.getUpdated());
        
        // Computed fields
        String fullName = donor.getFirstName() + " " + donor.getLastName();
        dto.setFullName(fullName);
        dto.setDisplayName(fullName);
        
        return dto;
    }
}
