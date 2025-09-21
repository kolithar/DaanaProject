package lk.kolitha.dana.controller;

import jakarta.validation.Valid;
import lk.kolitha.dana.dto.CommonResponse;
import lk.kolitha.dana.dto.donor.DonationHistoryResponseDto;
import lk.kolitha.dana.dto.donor.DonorProfileResponseDto;
import lk.kolitha.dana.dto.donor.DonorProfileUpdateDto;
import lk.kolitha.dana.service.DonationService;
import lk.kolitha.dana.service.RegisteredDonorService;
import lk.kolitha.dana.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@RestController
@RequestMapping("/donor")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Log4j2
public class DonorController {
    
    private final RegisteredDonorService registeredDonorService;
    private final DonationService donationService;
    private final SecurityUtils securityUtils;
    
    /**
     * Get current donor's profile
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<?> getCurrentDonorProfile() {
        try {
            log.info("Getting current donor profile");
            
            Long donorId = securityUtils.getCurrentDonorId();
            if (donorId == null) {
                return ResponseEntity.badRequest()
                        .body(new CommonResponse<>(false, "Unable to identify donor from token", null));
            }
            
            DonorProfileResponseDto profile = registeredDonorService.getDonorProfile(donorId);
            
            return ResponseEntity.ok(new CommonResponse<>(true, "Profile retrieved successfully", profile));
            
        } catch (Exception e) {
            log.error("Error getting donor profile: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new CommonResponse<>(false, "Failed to get profile: " + e.getMessage(), null));
        }
    }
    
    /**
     * Update current donor's profile information
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<?> updateDonorProfile(@Valid @RequestBody DonorProfileUpdateDto profileUpdate) {
        try {
            log.info("Updating donor profile");
            
            Long donorId = securityUtils.getCurrentDonorId();
            if (donorId == null) {
                return ResponseEntity.badRequest()
                        .body(new CommonResponse<>(false, "Unable to identify donor from token", null));
            }
            
            DonorProfileResponseDto updatedProfile = registeredDonorService.updateDonorProfile(donorId, profileUpdate);
            
            log.info("Donor profile updated successfully for ID: {}", donorId);
            return ResponseEntity.ok(new CommonResponse<>(true, "Profile updated successfully", updatedProfile));
            
        } catch (Exception e) {
            log.error("Error updating donor profile: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new CommonResponse<>(false, "Failed to update profile: " + e.getMessage(), null));
        }
    }
    
    /**
     * Update current donor's profile picture
     */
    @PostMapping(value = "/profile/picture", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<?> updateProfilePicture(@RequestParam("profileImage") MultipartFile profileImage) {
        try {
            log.info("Updating donor profile picture");
            
            Long donorId = securityUtils.getCurrentDonorId();
            if (donorId == null) {
                return ResponseEntity.badRequest()
                        .body(new CommonResponse<>(false, "Unable to identify donor from token", null));
            }
            
            // Validate file
            if (profileImage == null || profileImage.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new CommonResponse<>(false, "Profile image file is required and cannot be null or empty", null));
            }
            
            // Check file type
            String contentType = profileImage.getContentType();
            if (contentType == null || (!contentType.startsWith("image/"))) {
                return ResponseEntity.badRequest()
                        .body(new CommonResponse<>(false, "Only image files are allowed", null));
            }
            
            // Check file size (max 5MB)
            if (profileImage.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(new CommonResponse<>(false, "File size cannot exceed 5MB", null));
            }
            
            // Check if file has content
            if (profileImage.getSize() == 0) {
                return ResponseEntity.badRequest()
                        .body(new CommonResponse<>(false, "Profile image file cannot be empty", null));
            }
            
            // Check original filename
            String originalFilename = profileImage.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new CommonResponse<>(false, "Profile image file must have a valid filename", null));
            }
            
            String profileImageUrl = registeredDonorService.updateProfilePicture(donorId, profileImage);
            
            log.info("Profile picture updated successfully for donor ID: {}", donorId);
            return ResponseEntity.ok(new CommonResponse<>(true, "Profile picture updated successfully", profileImageUrl));
            
        } catch (Exception e) {
            log.error("Error updating profile picture: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new CommonResponse<>(false, "Failed to update profile picture: " + e.getMessage(), null));
        }
    }
    
    /**
     * Get donor profile by ID (admin only)
     */
    @GetMapping("/profile/{donorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDonorProfileById(@PathVariable Long donorId) {
        try {
            log.info("Getting donor profile for ID: {}", donorId);
            
            DonorProfileResponseDto profile = registeredDonorService.getDonorProfile(donorId);
            
            return ResponseEntity.ok(new CommonResponse<>(true, "Profile retrieved successfully", profile));
            
        } catch (Exception e) {
            log.error("Error getting donor profile: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new CommonResponse<>(false, "Failed to get profile: " + e.getMessage(), null));
        }
    }
    
    /**
     * Get current donor's donation history
     */
    @GetMapping("/donations")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<?> getDonorDonationHistory(
            @RequestParam(required = false) Date startDate,
            @RequestParam(required = false) Date endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            log.info("Getting donation history for current donor");
            
            Long donorId = securityUtils.getCurrentDonorId();
            if (donorId == null) {
                return ResponseEntity.badRequest()
                        .body(new CommonResponse<>(false, "Unable to identify donor from token", null));
            }
            
            Page<DonationHistoryResponseDto> donations = donationService.getDonorDonationHistory(
                    donorId,
                    startDate,
                    endDate,
                    pageable
            );
            
            return ResponseEntity.ok(new CommonResponse<>(true, "Donation history retrieved successfully", donations));
            
        } catch (Exception e) {
            log.error("Error getting donation history: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new CommonResponse<>(false, "Failed to get donation history: " + e.getMessage(), null));
        }
    }

}
