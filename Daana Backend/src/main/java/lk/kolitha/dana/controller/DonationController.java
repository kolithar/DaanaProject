package lk.kolitha.dana.controller;

import jakarta.validation.Valid;
import lk.kolitha.dana.dto.CommonResponse;
import lk.kolitha.dana.dto.donation.DonationCreateResponseDto;
import lk.kolitha.dana.dto.donation.DonationRequestDto;
import lk.kolitha.dana.dto.donation.DonationResponseDto;
import lk.kolitha.dana.service.DonationService;
import lk.kolitha.dana.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/charity/donations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Log4j2
public class DonationController {
    
    private final DonationService donationService;
    private final SecurityUtils securityUtils;
    
    @GetMapping
    @PreAuthorize("hasRole('CHARITY')")
    public ResponseEntity<?> getCharityDonations(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String donorEmail,
            @RequestParam(required = false) Boolean isAnonymous,
            @RequestParam(required = false) Long programId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        try {
            log.info("Getting charity donations with filters");
            Long charityId = securityUtils.getCurrentCharityId();
            if (charityId == null) {
                return ResponseEntity.badRequest()
                        .body(new CommonResponse<>(false, "Unable to identify charity from token", null));
            }
            
            Page<DonationResponseDto> donations = donationService.getCharityDonations(
                    charityId,
                    parseDate(startDate),
                    parseDate(endDate),
                    donorEmail,
                    isAnonymous,
                    programId,
                    pageable
            );
            
            return ResponseEntity.ok(new CommonResponse<>(true, "Donations retrieved successfully", donations));
            
        } catch (Exception e) {
            log.error("Error getting charity donations: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new CommonResponse<>(false, "Failed to get donations: " + e.getMessage(), null));
        }
    }
    
    private java.util.Date parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return new java.text.SimpleDateFormat("yyyy-MM-dd").parse(dateString);
        } catch (Exception e) {
            log.warn("Invalid date format: {}", dateString);
            return null;
        }
    }
    

}
