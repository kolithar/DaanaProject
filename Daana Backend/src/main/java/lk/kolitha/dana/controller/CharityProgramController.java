package lk.kolitha.dana.controller;


import lk.kolitha.dana.dto.CommonResponse;
import lk.kolitha.dana.dto.program.AdminFullProgramDto;
import lk.kolitha.dana.dto.program.CampaignUpdateStep1Dto;
import lk.kolitha.dana.dto.program.CampaignUpdateStep2Dto;
import lk.kolitha.dana.dto.program.CharityDashboardStatsDto;
import lk.kolitha.dana.dto.program.CharityProgramTableDto;
import lk.kolitha.dana.dto.program.ProgramRegisterStep1Dto;
import lk.kolitha.dana.dto.program.ProgramRegisterStep2Dto;
import lk.kolitha.dana.service.ProgramService;
import lk.kolitha.dana.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/charity/programs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Log4j2
public class CharityProgramController {
    private final ProgramService programService;
    private final SecurityUtils securityUtils;

    // Step 1: Register program with basic information (JSON)
    @PostMapping("/register/step1")
    @PreAuthorize("hasRole('CHARITY')")
    public ResponseEntity<?> registerProgramStep1(@Valid @RequestBody ProgramRegisterStep1Dto step1Dto) {
        try {
            log.info("Received program registration step 1 request: {}", step1Dto.getProgramName());
            Long charityId = securityUtils.getCurrentCharityId();
            if (charityId == null) {
                return ResponseEntity.badRequest()
                        .body(new CommonResponse<>(false, "Unable to identify charity from token", null));
            }
            AdminFullProgramDto adminFullProgramDto = programService.registerProgramStep1(charityId, step1Dto);
            return ResponseEntity.ok(new CommonResponse<>(true, "Program basic information saved successfully. Please proceed to step 2 for file uploads.", adminFullProgramDto));
        } catch (Exception e) {
            log.error("Error in program registration step 1: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new CommonResponse<>(false, "Failed to save program basic information: " + e.getMessage(), null));
        }
    }

    // Step 2: Upload files for program (Multipart)
    @PostMapping(value = "/register/step2", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('CHARITY')")
    public ResponseEntity<?> registerProgramStep2(@Valid @ModelAttribute ProgramRegisterStep2Dto step2Dto) {
        try {
            log.info("Received program registration step 2 request for program ID: {}", step2Dto.getProgramId());
            Long charityId = securityUtils.getCurrentCharityId();
            if (charityId == null) {
                return ResponseEntity.badRequest()
                        .body(new CommonResponse<>(false, "Unable to identify charity from token", null));
            }
            AdminFullProgramDto adminFullProgramDto = programService.registerProgramStep2(charityId, step2Dto);
            return ResponseEntity.ok(new CommonResponse<>(true, "Files uploaded successfully. Program registration completed.", adminFullProgramDto));
        } catch (Exception e) {
            log.error("Error in program registration step 2: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new CommonResponse<>(false, "Failed to upload files: " + e.getMessage(), null));
        }
    }



    @GetMapping("/list")
    @PreAuthorize("hasRole('CHARITY')")
    public ResponseEntity<CommonResponse<Page<CharityProgramTableDto>>> getCharityPrograms(Pageable pageable) {
        log.info("Getting charity programs list with pagination");
        
        Long charityId = securityUtils.getCurrentCharityId();
        if (charityId == null) {
            return ResponseEntity.badRequest()
                    .body(new CommonResponse<>(false, "Unable to identify charity from token", null));
        }
        
        Page<CharityProgramTableDto> programs = programService.getCharityPrograms(charityId, pageable);
        return ResponseEntity.ok(new CommonResponse<>(true, "Charity programs retrieved successfully", programs));
    }

    @GetMapping("/{programId}")
    @PreAuthorize("hasRole('CHARITY')")
    public ResponseEntity<CommonResponse<AdminFullProgramDto>> getCharityProgramById(@PathVariable Long programId) {
        log.info("Getting charity program details for ID: {}", programId);
        
        Long charityId = securityUtils.getCurrentCharityId();
        if (charityId == null) {
            return ResponseEntity.badRequest()
                    .body(new CommonResponse<>(false, "Unable to identify charity from token", null));
        }
        
        AdminFullProgramDto program = programService.getCharityProgramById(programId, charityId);
        return ResponseEntity.ok(new CommonResponse<>(true, "Program details retrieved successfully", program));
    }

    // Step 1: Update campaign basic information (JSON)
    @PutMapping("/{campaignId}/update/step1")
    @PreAuthorize("hasRole('CHARITY')")
    public ResponseEntity<?> updateCampaignStep1(
            @PathVariable Long campaignId,
            @Valid @RequestBody CampaignUpdateStep1Dto step1Dto) {
        try {
            log.info("Received campaign update step 1 request for campaign ID: {}", campaignId);
            Long charityId = securityUtils.getCurrentCharityId();
            if (charityId == null) {
                return ResponseEntity.badRequest()
                        .body(new CommonResponse<>(false, "Unable to identify charity from token", null));
            }
            AdminFullProgramDto updatedCampaign = programService.updateCampaignStep1(campaignId, charityId, step1Dto);
            return ResponseEntity.ok(new CommonResponse<>(true, "Campaign basic information updated successfully. Please proceed to step 2 for file uploads.", updatedCampaign));
        } catch (Exception e) {
            log.error("Error in campaign update step 1: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new CommonResponse<>(false, "Failed to update campaign basic information: " + e.getMessage(), null));
        }
    }

    // Step 2: Update campaign files (Multipart)
    @PutMapping(value = "/{campaignId}/update/step2", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('CHARITY')")
    public ResponseEntity<?> updateCampaignStep2(
            @PathVariable Long campaignId,
            @Valid @ModelAttribute CampaignUpdateStep2Dto step2Dto) {
        try {
            log.info("Received campaign update step 2 request for campaign ID: {}", campaignId);
            Long charityId = securityUtils.getCurrentCharityId();
            if (charityId == null) {
                return ResponseEntity.badRequest()
                        .body(new CommonResponse<>(false, "Unable to identify charity from token", null));
            }
            AdminFullProgramDto updatedCampaign = programService.updateCampaignStep2(campaignId, charityId, step2Dto);
            return ResponseEntity.ok(new CommonResponse<>(true, "Files updated successfully. Campaign update completed. Status changed to PENDING for admin review.", updatedCampaign));
        } catch (Exception e) {
            log.error("Error in campaign update step 2: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new CommonResponse<>(false, "Failed to update files: " + e.getMessage(), null));
        }
    }


    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasRole('CHARITY')")
    public ResponseEntity<?> getCharityDashboardStats() {
        try {
            log.info("Getting charity dashboard statistics");
            Long charityId = securityUtils.getCurrentCharityId();
            if (charityId == null) {
                return ResponseEntity.badRequest()
                        .body(new CommonResponse<>(false, "Unable to identify charity from token", null));
            }
            
            CharityDashboardStatsDto dashboardStats = programService.getCharityDashboardStats(charityId);
            return ResponseEntity.ok(new CommonResponse<>(true, "Dashboard statistics retrieved successfully", dashboardStats));
        } catch (Exception e) {
            log.error("Error getting dashboard statistics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new CommonResponse<>(false, "Failed to get dashboard statistics: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{campaignId}")
    @PreAuthorize("hasRole('CHARITY')")
    public ResponseEntity<CommonResponse<String>> deleteCampaign(@PathVariable Long campaignId) {
        log.info("Deleting campaign ID: {}", campaignId);
        
        Long charityId = securityUtils.getCurrentCharityId();
        if (charityId == null) {
            return ResponseEntity.badRequest()
                    .body(new CommonResponse<>(false, "Unable to identify charity from token", null));
        }
        
        programService.deleteCampaign(campaignId, charityId);
        return ResponseEntity.ok(new CommonResponse<>(true, "Campaign deleted successfully", null));
    }

}
