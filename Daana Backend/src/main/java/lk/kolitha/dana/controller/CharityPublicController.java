package lk.kolitha.dana.controller;


import lk.kolitha.dana.dto.CharityDto;
import lk.kolitha.dana.dto.CommonResponse;
import lk.kolitha.dana.dto.charity.CharityRegistrationResponseDto;
import lk.kolitha.dana.dto.charity.CharityRegistrationStep1Dto;
import lk.kolitha.dana.dto.charity.CharityRegistrationStep2Dto;
import lk.kolitha.dana.dto.charity.CharityRegistrationStep3Dto;
import lk.kolitha.dana.service.CharityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/public/charity")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Log4j2
public class CharityPublicController {
    private final CharityService charityService;


    // Step 1: Register charity with text information
    @PostMapping("/register/step1")
    public ResponseEntity<?> registerStep1(@Valid @RequestBody CharityRegistrationStep1Dto step1Dto) {
        log.info("Charity registration step 1 request received for email: {}", step1Dto.getEmail());
        CharityRegistrationResponseDto savedCharity = charityService.registerCharityStep1(step1Dto);
        return ResponseEntity.ok(new CommonResponse<>(true, "Registration step 1 completed. Please check your email for OTP code.", savedCharity));
    }

    // Step 2: Upload documents and bank details
    @PostMapping(value = "/register/step2", consumes = "multipart/form-data")
    public ResponseEntity<?> registerStep2(@ModelAttribute CharityRegistrationStep2Dto step2Dto) {
        log.info("Charity registration step 2 request received for ID: {}", step2Dto.getId());
        
        try {
            // Validate document type enum
            if (step2Dto.getDocumentTypeAsEnum() == null) {
                log.error("Invalid document type provided: {}", step2Dto.getDocumentType());
                return ResponseEntity.badRequest().body(new CommonResponse<>(false, 
                    "Invalid document type. Valid types are: ID_CARD, PASSPORT, DRIVING_LICENCE, BUSINESS_REGISTRATION_CERTIFICATE, REQUEST_FOR_PROOF_OF_ADDRESS, REQUEST_LETTER_OF_REFERRAL, OTHERS", null));
            }
            
            // Validate document file
            if (step2Dto.getDocumentFile() == null || step2Dto.getDocumentFile().isEmpty()) {
                log.error("Document file is required");
                return ResponseEntity.badRequest().body(new CommonResponse<>(false, "Document file is required", null));
            }

            CharityRegistrationResponseDto savedCharity = charityService.registerCharityStep2(step2Dto);
            return ResponseEntity.ok(new CommonResponse<>(true, "Documents uploaded successfully. Please proceed to email verification.", savedCharity));
            
        } catch (Exception e) {
            log.error("Error in charity registration step 2 for email: {}", step2Dto.getId(), e);
            return ResponseEntity.badRequest().body(new CommonResponse<>(false, e.getMessage(), null));
        }
    }

    // Step 3: Verify OTP and complete registration
    @PostMapping("/register/step3")
    public ResponseEntity<?> registerStep3(@Valid @RequestBody CharityRegistrationStep3Dto step3Dto) {
        log.info("Charity registration step 3 (OTP verification) request received for email: {}", step3Dto.getId());
        CharityRegistrationResponseDto savedCharity = charityService.verifyCharityOtp(step3Dto);
        return ResponseEntity.ok(new CommonResponse<>(true, "Your request is under review and we will get back to you soon.", savedCharity));
    }

    // Resend OTP for charity registration
    @PostMapping("/register/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestParam("email") String email) {
        log.info("Resend OTP request received for charity email: {}", email);
        charityService.resendCharityOtp(email);
        return ResponseEntity.ok(new CommonResponse<>(true, "OTP code has been resent to your email address.", null));
    }

    // Get required document type for execution type
    @GetMapping("/register/required-document-type")
    public ResponseEntity<?> getRequiredDocumentType(@RequestParam("executionType") String executionType) {
        log.info("Get required document type request for execution type: {}", executionType);
        
        try {
            lk.kolitha.dana.enums.ExecutionType execType = lk.kolitha.dana.enums.ExecutionType.valueOf(executionType.toUpperCase());
            lk.kolitha.dana.enums.CharityProofDocumentType requiredDocType = 
                CharityRegistrationStep2Dto.getRequiredDocumentType(execType);
            
            if (requiredDocType == null) {
                return ResponseEntity.badRequest().body(new CommonResponse<>(false, "Invalid execution type", null));
            }
            
            String message = String.format("For %s execution type, you must upload %s document", 
                execType.name(), requiredDocType.name());
            
            return ResponseEntity.ok(new CommonResponse<>(true, message, requiredDocType.name()));
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid execution type provided: {}", executionType);
            return ResponseEntity.badRequest().body(new CommonResponse<>(false, 
                "Invalid execution type. Valid types are: PERSON, ORGANIZATION", null));
        }
    }

}
