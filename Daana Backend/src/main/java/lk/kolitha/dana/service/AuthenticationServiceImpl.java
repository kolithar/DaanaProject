package lk.kolitha.dana.service;

import jakarta.transaction.Transactional;
import lk.kolitha.dana.entity.RegisteredDonor;
import lk.kolitha.dana.enums.Gender;
import lk.kolitha.dana.repository.AdminUserRepository;
import lk.kolitha.dana.repository.CharityRepository;
import lk.kolitha.dana.repository.RegisteredDonorRepository;
import lk.kolitha.dana.util.CustomGenerator;
import lk.kolitha.dana.util.DaanaSESEmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lk.kolitha.dana.constants.ApplicationConstants;
import lk.kolitha.dana.dto.AuthenticationRequest;
import lk.kolitha.dana.dto.AuthenticationResponse;
import lk.kolitha.dana.dto.charity.CharityForgotPasswordRequest;
import lk.kolitha.dana.dto.charity.CharityLoginRequest;
import lk.kolitha.dana.dto.charity.CharityResetPasswordRequest;
import lk.kolitha.dana.dto.donor.DonorRegisterRequest;
import lk.kolitha.dana.entity.AdminUser;
import lk.kolitha.dana.entity.Charity;
import lk.kolitha.dana.enums.Role;
import lk.kolitha.dana.enums.Status;
import lk.kolitha.dana.exception.CustomServiceException;
import lk.kolitha.dana.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthenticationServiceImpl implements  AuthenticationService {

    private final RegisteredDonorRepository donorRepository;
    private final CharityRepository charityRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final DaanaSESEmailSender emailSender;

    @Override
    @Transactional
    public void donorRegister(DonorRegisterRequest request) {
        log.info("Donor registration request received for email: {}", request.getEmail());
        
        try {
            // Validate request data
            if (request == null) {
                log.error("Donor registration request is null");
                throw new CustomServiceException(400, "Registration request cannot be null");
            }
            
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                log.error("Donor registration failed: Email is required");
                throw new CustomServiceException(400, "Email is required for registration");
            }
            
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                log.error("Donor registration failed: Password is required");
                throw new CustomServiceException(400, "Password is required for registration");
            }
            
            if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
                log.error("Donor registration failed: First name is required");
                throw new CustomServiceException(400, "First name is required for registration");
            }
            
            if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
                log.error("Donor registration failed: Last name is required");
                throw new CustomServiceException(400, "Last name is required for registration");
            }
            
            log.debug("Validating donor registration request: firstName={}, lastName={}, email={}", 
                     request.getFirstName(), request.getLastName(), request.getEmail());
            
            // Check if email already exists and is verified
            if (donorRepository.findFirstByEmailAndAccountVerifyStatus(request.getEmail(), true).isPresent()) {
                log.warn("Donor registration failed: Email {} is already registered and verified", request.getEmail());
                throw new CustomServiceException(409, "Email is already registered and verified. Please login instead.");
            }

            RegisteredDonor donor = null;
            Optional<RegisteredDonor> optionalRegisteredDonor = donorRepository.findByEmail(request.getEmail());
            
            if (optionalRegisteredDonor.isPresent()) {
                RegisteredDonor existingDonor = optionalRegisteredDonor.get();
                log.info("Found existing unverified donor account for email: {}", request.getEmail());
                
                if (!existingDonor.isAccountVerifyStatus()) {
                    // If the account is not verified, allow re-registration by updating the existing record
                    log.info("Updating existing unverified donor account for email: {}", request.getEmail());
                    
                    try {
                        // Generate OTP for email verification
                        String otpCode = CustomGenerator.generateSixDigitOtp();
                        
                        existingDonor.setFirstName(request.getFirstName());
                        existingDonor.setLastName(request.getLastName());
                        existingDonor.setPasswordHash(passwordEncoder.encode(request.getPassword()));
                        existingDonor.setAccountVerifyStatus(false); // Set to false until email is verified
                        existingDonor.setOptCode(otpCode);
                        existingDonor.setOptCodeGeneratedTimestamp(new Date());
                        existingDonor.setUpdated(new Date());
                        donor = donorRepository.save(existingDonor);
                        log.info("Successfully updated existing donor account for email: {}", request.getEmail());
                        
                        // Send OTP verification email
                        sendOtpVerificationEmail(donor.getEmail(), donor.getFirstName(), otpCode);

                    } catch (Exception e) {
                        log.error("Failed to update existing donor account for email: {}. Error: {}", 
                                 request.getEmail(), e.getMessage(), e);
                        throw new CustomServiceException(500, "Failed to update donor account. Please try again later.");
                    }
                } else {
                    // If the account is verified, throw conflict exception
                    log.warn("Donor registration failed: Email {} is already verified", request.getEmail());
                    throw new CustomServiceException(409, "Email is already registered and verified. Please login instead.");
                }
            } else {
                // Create new donor account
                log.info("Creating new donor account for email: {}", request.getEmail());
                
                try {
                    // Generate OTP for email verification
                    String otpCode = CustomGenerator.generateSixDigitOtp();
                    
                    RegisteredDonor newRegisteredDonor = RegisteredDonor.builder()
                            .firstName(request.getFirstName())
                            .lastName(request.getLastName())
                            .email(request.getEmail())
                            .gender(Gender.MALE)
                            .passwordHash(passwordEncoder.encode(request.getPassword()))
                            .accountVerifyStatus(false) // Set to false until email is verified
                            .optCode(otpCode)
                            .optCodeGeneratedTimestamp(new Date())
                            .isDeleted(false)
                            .created(new Date())
                            .updated(new Date())
                            .build();

                    donor = donorRepository.save(newRegisteredDonor);
                    log.info("Successfully created new donor account for email: {}", request.getEmail());
                    
                    // Send OTP verification email
                    sendOtpVerificationEmail(donor.getEmail(), donor.getFirstName(), otpCode);
                    
                } catch (Exception e) {
                    log.error("Failed to create new donor account for email: {}. Error: {}", 
                             request.getEmail(), e.getMessage(), e);
                    throw new CustomServiceException(500, "Failed to create donor account. Please try again later.");
                }
            }

            // Generate authentication response
            log.info("Generating authentication response for donor: {}", donor.getEmail());
                                     
        } catch (CustomServiceException e) {
            // Re-throw custom exceptions as they are already properly formatted
            log.error("Donor registration failed with custom exception: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // Handle unexpected exceptions
            log.error("Unexpected error during donor registration for email: {}. Error: {}", 
                     request != null ? request.getEmail() : "unknown", e.getMessage(), e);
            throw new CustomServiceException(500, "An unexpected error occurred during registration. Please try again later.");
        }
    }

    

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("Authentication request received for email: {}", request.getEmail());
        
        try {
            // Validate request
            if (request == null) {
                log.error("Authentication request is null");
                throw new CustomServiceException(400, "Authentication request cannot be null");
            }
            
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                log.error("Authentication failed: Email is required");
                throw new CustomServiceException(400, "Email is required for authentication");
            }
            
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                log.error("Authentication failed: Password is required");
                throw new CustomServiceException(400, "Password is required for authentication");
            }
            
            log.debug("Authenticating user with email: {}", request.getEmail());
            
            // Authenticate credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            log.info("Credentials authenticated successfully for email: {}", request.getEmail());

            // Find user and generate response - using orElseThrow pattern
            try {
                // Check if it's a donor
                log.debug("Checking if user is a donor");
                RegisteredDonor donor = donorRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new CustomServiceException(404, ApplicationConstants.NotFoundConstants.NO_USER_FOUND));
                
                if (!donor.isDeleted() && donor.isAccountVerifyStatus()) {
                    log.info("User authenticated as donor: {}", request.getEmail());
                    return generateAuthResponse(donor.getEmail(), donor.getFirstName()+" "+donor.getLastName(), Role.DONOR,
                            donor.getProfileImageUrl()==null?"":donor.getProfileImageUrl());
                } else {
                    log.warn("Donor account is deleted or not verified: {}", request.getEmail());
                }
            } catch (CustomServiceException e) {
                log.debug("User is not a donor, checking other user types");
                // Donor not found, continue to check other user types
            }

            try {
                // Check if it's a charity
                log.debug("Checking if user is a charity");
                Charity charity = charityRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new CustomServiceException(404, ApplicationConstants.NotFoundConstants.NO_USER_FOUND));
                
                if (!charity.isDeleted() && charity.getStatus() == Status.ACTIVE) {
                    log.info("User authenticated as charity: {}", request.getEmail());
                    return generateAuthResponse(charity.getEmail(), charity.getName(), Role.CHARITY, "");
                } else {
                    log.warn("Charity account is deleted or not active: {}", request.getEmail());
                }
            } catch (CustomServiceException e) {
                log.debug("User is not a charity, checking admin");
                // Charity not found, continue to check admin
            }

            try {
                // Check if it's an admin
                log.debug("Checking if user is an admin");
                AdminUser admin = adminUserRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new CustomServiceException(404, ApplicationConstants.NotFoundConstants.NO_USER_FOUND));
                
                if (admin.getAdminStatus() == Status.ACTIVE) {
                    log.info("User authenticated as admin: {}", request.getEmail());
                    return generateAuthResponse(admin.getEmail(), admin.getFullName(), admin.getAdminRole(), "");
                } else {
                    log.warn("Admin account is not active: {}", request.getEmail());
                }
            } catch (CustomServiceException e) {
                log.debug("User is not an admin");
                // Admin not found
            }

            // If we reach here, user exists but is not verified or active
            log.warn("Authentication failed: User exists but is not verified or active: {}", request.getEmail());
            throw new CustomServiceException(403, ApplicationConstants.AuthenticationConstants.USER_NOT_VERIFIED);
            
        } catch (CustomServiceException e) {
            // Re-throw custom exceptions as they are already properly formatted
            log.error("Authentication failed with custom exception: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // Handle unexpected exceptions
            log.error("Unexpected error during authentication for email: {}. Error: {}", 
                     request != null ? request.getEmail() : "unknown", e.getMessage(), e);
            throw new CustomServiceException(500, "An unexpected error occurred during authentication. Please try again later.");
        }
    }

    private AuthenticationResponse generateAuthResponse(String email, String fullName, Role role, String profileImageUrl) {
        log.debug("Generating authentication response for user: {} with role: {}", email, role);
        
        try {
            var user = org.springframework.security.core.userdetails.User.builder()
                    .username(email)
                    .password("")
                    .authorities(role.name())
                    .build();

            // Generate scope based on role
            String scope = generateScopeForRole(role);
            
            // Generate enhanced JWT token with scope and JTI
            var jwtToken = jwtService.generateTokenWithScope(user, scope);
            var refreshToken = jwtService.generateRefreshToken(user);
            
            log.debug("JWT token and refresh token generated successfully for user: {}", email);
            
            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .tokenType("bearer")
                    .expiresIn(24 * 60 * 60L) // 24 hours in seconds
                    .scope(scope)
                    .userType(role.name())
                    .email(email)
                    .fullName(fullName)
                    .profileImageUrl(profileImageUrl)
                    .jti(extractJtiFromToken(jwtToken))
                    .userId(extractUserIdFromEmail(email))
                    .domain("http://api.dana.lk/dana")
                    .build();
        } catch (Exception e) {
            log.error("Failed to generate authentication response for user: {}. Error: {}", email, e.getMessage(), e);
            throw new CustomServiceException(500, "Failed to generate authentication tokens. Please try again later.");
        }
    }

    private String generateScopeForRole(Role role) {
        switch (role) {
            case ADMIN:
                return "read write delete admin";
            case CHARITY:
                return "read write charity";
            case DONOR:
                return "read write donate";
            case MONITOR:
                return "read monitor";
            default:
                return "read";
        }
    }

    private String extractJtiFromToken(String token) {
        try {
            // Extract JTI from token claims
            return jwtService.extractClaim(token, claims -> claims.get("jti", String.class));
        } catch (Exception e) {
            log.warn("Failed to extract JTI from token: {}", e.getMessage());
            return UUID.randomUUID().toString();
        }
    }

    private Long extractUserIdFromEmail(String email) {
        // This is a placeholder - in real implementation, you'd get this from the user entity
        // For now, we'll generate a hash-based ID
        return (long) email.hashCode();
    }

    /**
     * Sends OTP verification email to the user
     * @param email User's email address
     * @param firstName User's first name
     * @param otpCode Generated OTP code
     */
    private void sendOtpVerificationEmail(String email, String firstName, String otpCode) {
        try {
            log.info("Sending OTP verification email to: {}", email);
            
            String subject = "Verify Your Email - Daana.lk";
            String htmlContent = loadAndPopulateOtpEmailTemplate(firstName, otpCode);
            
            emailSender.sendHtmEmail(email, subject, htmlContent);
            log.info("OTP verification email sent successfully to: {}", email);
            
        } catch (Exception e) {
            log.error("Failed to send OTP verification email to: {}. Error: {}", email, e.getMessage(), e);
            // Don't throw exception here as registration should still succeed
            // The user can request a new OTP later
        }
    }

    /**
     * Loads the OTP email template and populates it with user data
     * @param firstName User's first name
     * @param otpCode Generated OTP code
     * @return HTML email content with populated data
     */
    private String loadAndPopulateOtpEmailTemplate(String firstName, String otpCode) {
        try {
            log.debug("Loading OTP email template from resources");
            
            // Load the HTML template from resources
            ClassPathResource resource = new ClassPathResource("EmailTemplates/OTPEmailTemplates.html");
            String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            
            // Replace placeholders with actual values
            String populatedTemplate = template
                    .replace("{{firstName}}", firstName != null ? firstName : "User")
                    .replace("{{otpCode}}", otpCode != null ? otpCode : "000000");
            
            log.debug("OTP email template loaded and populated successfully");
            return populatedTemplate;
            
        } catch (IOException e) {
            log.error("Failed to load OTP email template: {}", e.getMessage(), e);
            throw new CustomServiceException(500, "Failed to load OTP email template. Please try again later.");
        }
    }



    @Override
    @Transactional
    public void verifyOtp(String email, String otpCode) {
        log.info("OTP verification request received for email: {}", email);
        
        try {
            // Validate input parameters
            if (email == null || email.trim().isEmpty()) {
                log.error("OTP verification failed: Email is required");
                throw new CustomServiceException(400, "Email is required for OTP verification");
            }
            
            if (otpCode == null || otpCode.trim().isEmpty()) {
                log.error("OTP verification failed: OTP code is required");
                throw new CustomServiceException(400, "OTP code is required for verification");
            }
            
            // Find the donor by email
            RegisteredDonor donor = donorRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomServiceException(404, "User not found with email: " + email));
            
            // Check if account is already verified
            if (donor.isAccountVerifyStatus()) {
                log.warn("OTP verification failed: Account is already verified for email: {}", email);
                throw new CustomServiceException(400, "Account is already verified");
            }
            
            // Check if OTP code exists
            if (donor.getOptCode() == null || donor.getOptCode().trim().isEmpty()) {
                log.error("OTP verification failed: No OTP code found for email: {}", email);
                throw new CustomServiceException(400, "No OTP code found. Please request a new one.");
            }
            
            // Check if OTP code matches
            if (!donor.getOptCode().equals(otpCode.trim())) {
                log.warn("OTP verification failed: Invalid OTP code for email: {}", email);
                throw new CustomServiceException(400, "Invalid OTP code. Please check and try again.");
            }
            
            // Check if OTP is expired (10 minutes)
            if (donor.getOptCodeGeneratedTimestamp() == null) {
                log.error("OTP verification failed: No timestamp found for OTP code for email: {}", email);
                throw new CustomServiceException(400, "OTP code has expired. Please request a new one.");
            }
            
            long currentTime = System.currentTimeMillis();
            long otpTime = donor.getOptCodeGeneratedTimestamp().getTime();
            long timeDifference = currentTime - otpTime;
            long tenMinutesInMillis = 10 * 60 * 1000; // 10 minutes
            
            if (timeDifference > tenMinutesInMillis) {
                log.warn("OTP verification failed: OTP code expired for email: {}", email);
                throw new CustomServiceException(400, "OTP code has expired. Please request a new one.");
            }
            
            // Verify the account
            donor.setAccountVerifyStatus(true);
            donor.setOptCode(null); // Clear the OTP code after successful verification
            donor.setOptCodeGeneratedTimestamp(null);
            donor.setUpdated(new Date());
            
            donorRepository.save(donor);
            
            log.info("OTP verification successful for email: {}", email);
            
        } catch (CustomServiceException e) {
            // Re-throw custom exceptions as they are already properly formatted
            log.error("OTP verification failed with custom exception: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // Handle unexpected exceptions
            log.error("Unexpected error during OTP verification for email: {}. Error: {}", 
                     email, e.getMessage(), e);
            throw new CustomServiceException(500, "An unexpected error occurred during OTP verification. Please try again later.");
        }
    }

    @Override
    @Transactional
    public void resendOtp(String email) {
        log.info("Resend OTP request received for email: {}", email);
        
        try {
            // Validate input parameter
            if (email == null || email.trim().isEmpty()) {
                log.error("Resend OTP failed: Email is required");
                throw new CustomServiceException(400, "Email is required to resend OTP");
            }
            
            // Find the donor by email
            RegisteredDonor donor = donorRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomServiceException(404, "User not found with email: " + email));
            
            // Check if account is already verified
            if (donor.isAccountVerifyStatus()) {
                log.warn("Resend OTP failed: Account is already verified for email: {}", email);
                throw new CustomServiceException(400, "Account is already verified. No need to resend OTP.");
            }
            
            // Generate new OTP code
            String newOtpCode = CustomGenerator.generateSixDigitOtp();
            
            // Update donor with new OTP
            donor.setOptCode(newOtpCode);
            donor.setOptCodeGeneratedTimestamp(new Date());
            donor.setUpdated(new Date());
            
            donorRepository.save(donor);
            
            // Send new OTP verification email
            sendOtpVerificationEmail(donor.getEmail(), donor.getFirstName(), newOtpCode);
            
            log.info("OTP resent successfully for email: {}", email);
            
        } catch (CustomServiceException e) {
            // Re-throw custom exceptions as they are already properly formatted
            log.error("Resend OTP failed with custom exception: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // Handle unexpected exceptions
            log.error("Unexpected error during resend OTP for email: {}. Error: {}", 
                     email, e.getMessage(), e);
            throw new CustomServiceException(500, "An unexpected error occurred while resending OTP. Please try again later.");
        }
    }

    @Override
    public AuthenticationResponse charityLogin(CharityLoginRequest request) {
        log.info("Charity login request received for email: {}", request.getEmail());
        
        try {
            // Validate request
            if (request == null) {
                log.error("Charity login request is null");
                throw new CustomServiceException(400, "Login request cannot be null");
            }
            
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                log.error("Charity login failed: Email is required");
                throw new CustomServiceException(400, "Email is required for login");
            }
            
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                log.error("Charity login failed: Password is required");
                throw new CustomServiceException(400, "Password is required for login");
            }
            
            log.debug("Authenticating charity with email: {}", request.getEmail());
            
            // Authenticate credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            log.info("Credentials authenticated successfully for charity email: {}", request.getEmail());

            // Find charity and generate response
            Charity charity = charityRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new CustomServiceException(404, "Charity not found with email: " + request.getEmail()));
            
            // Check if charity account is active and not deleted
            if (charity.isDeleted()) {
                log.warn("Charity login failed: Account is deleted for email: {}", request.getEmail());
                throw new CustomServiceException(403, "Your charity account has been deactivated. Please contact support.");
            }
            
            if (charity.getStatus() != Status.ACTIVE) {
                log.warn("Charity login failed: Account is not active for email: {}. Status: {}", 
                        request.getEmail(), charity.getStatus());
                throw new CustomServiceException(403, "Your charity account is not active. Please contact support for activation.");
            }
            
            if (!charity.isAccountVerifyStatus()) {
                log.warn("Charity login failed: Account is not verified for email: {}", request.getEmail());
                throw new CustomServiceException(403, "Please verify your email address before logging in. Check your email for verification code.");
            }
            
            log.info("Charity authenticated successfully: {}", request.getEmail());
            
            // Generate authentication response
            return generateAuthResponse(charity.getEmail(), charity.getName(), Role.CHARITY, 
                    charity.getLogoUrl() == null ? "" : charity.getLogoUrl());
            
        } catch (CustomServiceException e) {
            // Re-throw custom exceptions as they are already properly formatted
            log.error("Charity login failed with custom exception: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // Handle unexpected exceptions
            log.error("Unexpected error during charity login for email: {}. Error: {}", 
                     request.getEmail(), e.getMessage(), e);
            throw new CustomServiceException(500, "An unexpected error occurred during login. Please try again later.");
        }
    }

    @Override
    @Transactional
    public void charityForgotPassword(CharityForgotPasswordRequest request) {
        log.info("Charity forgot password request received for email: {}", request.getEmail());
        
        try {
            // Validate request
            if (request == null) {
                log.error("Charity forgot password request is null");
                throw new CustomServiceException(400, "Request cannot be null");
            }
            
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                log.error("Charity forgot password failed: Email is required");
                throw new CustomServiceException(400, "Email is required");
            }
            
            // Find charity by email
            Charity charity = charityRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new CustomServiceException(404, "No charity account found with this email address"));
            
            // Check if charity account is active and not deleted
            if (charity.isDeleted()) {
                log.warn("Charity forgot password failed: Account is deleted for email: {}", request.getEmail());
                throw new CustomServiceException(403, "Your charity account has been deactivated. Please contact support.");
            }
            
            if (charity.getStatus() != Status.ACTIVE) {
                log.warn("Charity forgot password failed: Account is not active for email: {}. Status: {}", 
                        request.getEmail(), charity.getStatus());
                throw new CustomServiceException(403, "Your charity account is not active. Please contact support for activation.");
            }
            
            // Generate OTP code for password reset
            String otpCode = CustomGenerator.generateSixDigitOtp();
            
            // Update charity with OTP code and timestamp
            charity.setOtpCode(otpCode);
            charity.setOtpCodeGeneratedTimestamp(new Date());
            charity.setUpdated(new Date());
            
            charityRepository.save(charity);
            
            // Send password reset OTP email
            sendCharityPasswordResetEmail(charity.getEmail(), charity.getName(), otpCode);
            
            log.info("Password reset OTP sent successfully for charity email: {}", request.getEmail());
            
        } catch (CustomServiceException e) {
            // Re-throw custom exceptions as they are already properly formatted
            log.error("Charity forgot password failed with custom exception: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // Handle unexpected exceptions
            log.error("Unexpected error during charity forgot password for email: {}. Error: {}", 
                     request.getEmail(), e.getMessage(), e);
            throw new CustomServiceException(500, "An unexpected error occurred. Please try again later.");
        }
    }

    @Override
    @Transactional
    public void charityResetPassword(CharityResetPasswordRequest request) {
        log.info("Charity reset password request received for email: {}", request.getEmail());
        
        try {
            // Validate request
            if (request == null) {
                log.error("Charity reset password request is null");
                throw new CustomServiceException(400, "Request cannot be null");
            }
            
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                log.error("Charity reset password failed: Email is required");
                throw new CustomServiceException(400, "Email is required");
            }
            
            if (request.getOtpCode() == null || request.getOtpCode().trim().isEmpty()) {
                log.error("Charity reset password failed: OTP code is required");
                throw new CustomServiceException(400, "OTP code is required");
            }
            
            if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                log.error("Charity reset password failed: New password is required");
                throw new CustomServiceException(400, "New password is required");
            }
            
            if (request.getNewPassword().length() < 8) {
                log.error("Charity reset password failed: Password too short");
                throw new CustomServiceException(400, "Password must be at least 8 characters long");
            }
            
            // Find charity by email
            Charity charity = charityRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new CustomServiceException(404, "No charity account found with this email address"));
            
            // Check if charity account is active and not deleted
            if (charity.isDeleted()) {
                log.warn("Charity reset password failed: Account is deleted for email: {}", request.getEmail());
                throw new CustomServiceException(403, "Your charity account has been deactivated. Please contact support.");
            }
            
            if (charity.getStatus() != Status.ACTIVE) {
                log.warn("Charity reset password failed: Account is not active for email: {}. Status: {}", 
                        request.getEmail(), charity.getStatus());
                throw new CustomServiceException(403, "Your charity account is not active. Please contact support for activation.");
            }
            
            // Verify OTP code
            if (charity.getOtpCode() == null || charity.getOtpCode().trim().isEmpty()) {
                log.warn("Charity reset password failed: No OTP code found for email: {}", request.getEmail());
                throw new CustomServiceException(400, "No password reset request found. Please request a new password reset.");
            }
            
            if (!charity.getOtpCode().equals(request.getOtpCode())) {
                log.warn("Charity reset password failed: Invalid OTP code for email: {}", request.getEmail());
                throw new CustomServiceException(400, "Invalid OTP code. Please check and try again.");
            }
            
            // Check OTP expiry (10 minutes)
            if (charity.getOtpCodeGeneratedTimestamp() == null) {
                log.warn("Charity reset password failed: No OTP timestamp for email: {}", request.getEmail());
                throw new CustomServiceException(400, "OTP code has expired. Please request a new one.");
            }
            
            long timeDifference = System.currentTimeMillis() - charity.getOtpCodeGeneratedTimestamp().getTime();
            long tenMinutesInMillis = 10 * 60 * 1000; // 10 minutes
            
            if (timeDifference > tenMinutesInMillis) {
                log.warn("Charity reset password failed: OTP code expired for email: {}", request.getEmail());
                throw new CustomServiceException(400, "OTP code has expired. Please request a new one.");
            }
            
            // Reset password
            String hashedPassword = passwordEncoder.encode(request.getNewPassword());
            charity.setPasswordHash(hashedPassword);
            charity.setOtpCode(null); // Clear the OTP code after successful reset
            charity.setOtpCodeGeneratedTimestamp(null);
            charity.setUpdated(new Date());
            
            charityRepository.save(charity);
            
            log.info("Password reset successful for charity email: {}", request.getEmail());
            
        } catch (CustomServiceException e) {
            // Re-throw custom exceptions as they are already properly formatted
            log.error("Charity reset password failed with custom exception: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // Handle unexpected exceptions
            log.error("Unexpected error during charity reset password for email: {}. Error: {}", 
                     request.getEmail(), e.getMessage(), e);
            throw new CustomServiceException(500, "An unexpected error occurred. Please try again later.");
        }
    }

    private void sendCharityPasswordResetEmail(String email, String charityName, String otpCode) {
        try {
            // Load email template
            ClassPathResource resource = new ClassPathResource("EmailTemplates/CharityPasswordResetEmailTemplate.html");
            String emailTemplate = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            
            // Replace placeholders
            String emailContent = emailTemplate
                    .replace("{{firstName}}", charityName)
                    .replace("{{otpCode}}", otpCode);
            
            // Send email
            emailSender.sendHtmEmail(
                    email,
                    "Password Reset - Daana.lk",
                    emailContent
            );
            
            log.info("Password reset email sent successfully to charity: {}", email);
            
        } catch (IOException e) {
            log.error("Failed to send password reset email to charity: {}. Error: {}", email, e.getMessage(), e);
            throw new CustomServiceException(500, "Failed to send password reset email. Please try again later.");
        } catch (Exception e) {
            log.error("Unexpected error sending password reset email to charity: {}. Error: {}", email, e.getMessage(), e);
            throw new CustomServiceException(500, "Failed to send password reset email. Please try again later.");
        }
    }
}
