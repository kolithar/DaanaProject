package lk.kolitha.dana.controller;

import lk.kolitha.dana.dto.CommonResponse;
import lk.kolitha.dana.dto.charity.CharityProfileDto;
import lk.kolitha.dana.dto.charity.PasswordChangeDto;
import lk.kolitha.dana.dto.charity.ProfileUpdateDto;
import lk.kolitha.dana.service.CharityService;
import lk.kolitha.dana.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/charity/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Log4j2
public class CharityProfileController {
    
    private final CharityService charityService;
    private final SecurityUtils securityUtils;
    
    @GetMapping
    @PreAuthorize("hasRole('CHARITY')")
    public ResponseEntity<?> getCharityProfile() {
        try {
            log.info("Getting charity profile");
            Long charityId = securityUtils.getCurrentCharityId();
            if (charityId == null) {
                return ResponseEntity.badRequest()
                        .body(new CommonResponse<>(false, "Unable to identify charity from token", null));
            }
            
            CharityProfileDto profile = charityService.getCharityProfile(charityId);
            return ResponseEntity.ok(new CommonResponse<>(true, "Charity profile retrieved successfully", profile));
            
        } catch (Exception e) {
            log.error("Error getting charity profile: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new CommonResponse<>(false, "Failed to get charity profile: " + e.getMessage(), null));
        }
    }
    
    @PutMapping("/password")
    @PreAuthorize("hasRole('CHARITY')")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PasswordChangeDto passwordChangeDto) {
        try {
            log.info("Changing charity password");
            Long charityId = securityUtils.getCurrentCharityId();
            if (charityId == null) {
                return ResponseEntity.badRequest()
                        .body(new CommonResponse<>(false, "Unable to identify charity from token", null));
            }
            
            charityService.changePassword(charityId, passwordChangeDto);
            return ResponseEntity.ok(new CommonResponse<>(true, "Password changed successfully", null));
            
        } catch (Exception e) {
            log.error("Error changing password: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new CommonResponse<>(false, "Failed to change password: " + e.getMessage(), null));
        }
    }
    
    @PutMapping
    @PreAuthorize("hasRole('CHARITY')")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody ProfileUpdateDto profileUpdateDto) {
        try {
            log.info("Updating charity profile");
            Long charityId = securityUtils.getCurrentCharityId();
            if (charityId == null) {
                return ResponseEntity.badRequest()
                        .body(new CommonResponse<>(false, "Unable to identify charity from token", null));
            }
            
            CharityProfileDto updatedProfile = charityService.updateProfile(charityId, profileUpdateDto);
            return ResponseEntity.ok(new CommonResponse<>(true, "Profile updated successfully", updatedProfile));
            
        } catch (Exception e) {
            log.error("Error updating profile: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new CommonResponse<>(false, "Failed to update profile: " + e.getMessage(), null));
        }
    }
}
