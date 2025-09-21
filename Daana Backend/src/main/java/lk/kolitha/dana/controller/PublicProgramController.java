package lk.kolitha.dana.controller;

import jakarta.validation.Valid;
import lk.kolitha.dana.dto.CommonResponse;
import lk.kolitha.dana.dto.category.CategoryWithSubCategoriesDto;
import lk.kolitha.dana.dto.donation.DonationCreateResponseDto;
import lk.kolitha.dana.dto.donation.DonationRequestDto;
import lk.kolitha.dana.dto.program.BasicProgramCardDataResDto;
import lk.kolitha.dana.dto.program.FullProgramDto;
import lk.kolitha.dana.service.CategoryService;
import lk.kolitha.dana.service.DonationService;
import lk.kolitha.dana.service.ProgramService;
import lk.kolitha.dana.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/programs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Log4j2
public class PublicProgramController {
    
    private final ProgramService programService;
    private final CategoryService categoryService;
    private final SecurityUtils securityUtils;
    private final DonationService donationService;

    @GetMapping("/trending")
    public ResponseEntity<CommonResponse<List<BasicProgramCardDataResDto>>> getTrendingPrograms() {
        log.info("Received request for trending programs");
        try {
            List<BasicProgramCardDataResDto> trendingPrograms = programService.getTrendingPrograms();
            log.info("Successfully retrieved {} trending programs", trendingPrograms.size());
            
            CommonResponse<List<BasicProgramCardDataResDto>> response = new CommonResponse<>(
                true, 
                "Trending programs retrieved successfully", 
                trendingPrograms
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving trending programs: {}", e.getMessage(), e);
            CommonResponse<List<BasicProgramCardDataResDto>> errorResponse = new CommonResponse<>(
                false, 
                "Failed to retrieve trending programs: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    

    @GetMapping("/latest")
    public ResponseEntity<CommonResponse<List<BasicProgramCardDataResDto>>> getLatestPrograms() {
        log.info("Received request for latest programs");
        try {
            List<BasicProgramCardDataResDto> latestPrograms = programService.getLatestPrograms();
            log.info("Successfully retrieved {} latest programs", latestPrograms.size());
            
            CommonResponse<List<BasicProgramCardDataResDto>> response = new CommonResponse<>(
                true, 
                "Latest programs retrieved successfully", 
                latestPrograms
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving latest programs: {}", e.getMessage(), e);
            CommonResponse<List<BasicProgramCardDataResDto>> errorResponse = new CommonResponse<>(
                false, 
                "Failed to retrieve latest programs: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    

    @GetMapping("/categories")
    public ResponseEntity<CommonResponse<List<CategoryWithSubCategoriesDto>>> getCategoriesWithSubCategories() {
        log.info("Received request for categories with subcategories");
        try {
            List<CategoryWithSubCategoriesDto> categories = categoryService.getAllCategoriesWithSubCategories();
            log.info("Successfully retrieved {} categories with subcategories", categories.size());
            
            CommonResponse<List<CategoryWithSubCategoriesDto>> response = new CommonResponse<>(
                true, 
                "Categories with subcategories retrieved successfully", 
                categories
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving categories with subcategories: {}", e.getMessage(), e);
            CommonResponse<List<CategoryWithSubCategoriesDto>> errorResponse = new CommonResponse<>(
                false, 
                "Failed to retrieve categories with subcategories: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<?> filterPrograms(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long subCategoryId,
            @RequestParam(required = false) String searchText,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<BasicProgramCardDataResDto> resDto = programService.filterPrograms(categoryId, subCategoryId, searchText, PageRequest.of(page, size));
        return ResponseEntity.ok(new CommonResponse<>(
                true,
                "Program retrieved successfully",
                resDto
        ));
    }


    @GetMapping("/{urlName}")
    public ResponseEntity<CommonResponse<FullProgramDto>> getProgramByUrl(@PathVariable String urlName) {
        try {
            FullProgramDto program = programService.getProgramByUrl(urlName);

            CommonResponse<FullProgramDto> response = new CommonResponse<>(
                    true,
                    "Program retrieved successfully",
                    program
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            CommonResponse<FullProgramDto> errorResponse = new CommonResponse<>(
                    false,
                    "Failed to retrieve program: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }



    @PostMapping(value = "/donate", consumes = "multipart/form-data")
    public ResponseEntity<?> createDonation(
            @Valid @ModelAttribute DonationRequestDto donationRequest) {
        try {
            log.info("Processing donation request for campaign ID: {} with amount: {}",
                    donationRequest.getCampaignId(), donationRequest.getActualDonationAmount());

            // Get authenticated donor ID if token is present
            Long authenticatedDonorId = securityUtils.getCurrentDonorId();

            // Create donation
            DonationCreateResponseDto donationResponse = donationService.createDonation(donationRequest, authenticatedDonorId);

            log.info("Donation created successfully with ID: {} and reference: {}",
                    donationResponse.getDonationId(), donationResponse.getPaymentReferenceNumber());

            return ResponseEntity.ok(new CommonResponse<>(true, "Donation created successfully", donationResponse));

        } catch (Exception e) {
            log.error("Error creating donation: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new CommonResponse<>(false, "Failed to create donation: " + e.getMessage(), null));
        }
    }




    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Public Program service is running!");
    }
}

