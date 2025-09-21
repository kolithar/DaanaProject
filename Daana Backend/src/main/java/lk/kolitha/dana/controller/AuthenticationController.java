package lk.kolitha.dana.controller;

import jakarta.validation.Valid;
import lk.kolitha.dana.dto.AuthenticationRequest;
import lk.kolitha.dana.dto.AuthenticationResponse;
import lk.kolitha.dana.dto.CommonResponse;
import lk.kolitha.dana.dto.charity.CharityForgotPasswordRequest;
import lk.kolitha.dana.dto.charity.CharityLoginRequest;
import lk.kolitha.dana.dto.charity.CharityResetPasswordRequest;
import lk.kolitha.dana.dto.donor.DonorRegisterRequest;
import lk.kolitha.dana.service.AuthenticationServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Log4j2
public class AuthenticationController {

    private final AuthenticationServiceImpl authenticationServiceImpl;

    @PostMapping("/register")
    public ResponseEntity<CommonResponse<String>> donorRegister(@Valid @RequestBody DonorRegisterRequest request) {
        log.info("Received donor registration request for email: {}", request.getEmail());
        authenticationServiceImpl.donorRegister(request);
        log.info("Donor registration successful for email: {}", request.getEmail());
        CommonResponse<String> response = new CommonResponse<>(
                true,
                "Donor registered successfully. Please check your email for verification code to complete registration.",
                null
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        log.info("Received login request for email: {}", request.getEmail());
        AuthenticationResponse response = authenticationServiceImpl.authenticate(request);
        log.info("Login successful for email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/charity/login")
    public ResponseEntity<AuthenticationResponse> charityLogin(@Valid @RequestBody CharityLoginRequest request) {
        log.info("Received charity login request for email: {}", request.getEmail());
        AuthenticationResponse response = authenticationServiceImpl.charityLogin(request);
        log.info("Charity login successful for email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/charity/forgot-password")
    public ResponseEntity<CommonResponse<String>> charityForgotPassword(@Valid @RequestBody CharityForgotPasswordRequest request) {
        log.info("Received charity forgot password request for email: {}", request.getEmail());
        authenticationServiceImpl.charityForgotPassword(request);
        log.info("Charity forgot password OTP sent successfully for email: {}", request.getEmail());
        CommonResponse<String> response = new CommonResponse<>(
                true,
                "Password reset code has been sent to your email address. Please check your inbox and follow the instructions to reset your password.",
                null
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/charity/reset-password")
    public ResponseEntity<CommonResponse<String>> charityResetPassword(@Valid @RequestBody CharityResetPasswordRequest request) {
        log.info("Received charity reset password request for email: {}", request.getEmail());
        authenticationServiceImpl.charityResetPassword(request);
        log.info("Charity password reset successful for email: {}", request.getEmail());
        CommonResponse<String> response = new CommonResponse<>(
                true,
                "Password has been reset successfully. You can now login with your new password.",
                null
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<CommonResponse<String>> verifyOtp(@RequestParam String email, @RequestParam String otpCode) {
        log.info("Received OTP verification request for email: {}", email);
        authenticationServiceImpl.verifyOtp(email, otpCode);
        log.info("OTP verification successful for email: {}", email);
        CommonResponse<String> response = new CommonResponse<>(
                true,
                "Email verified successfully. You can now login.",
                null
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<CommonResponse<String>> resendOtp(@RequestParam String email) {
        log.info("Received resend OTP request for email: {}", email);
        authenticationServiceImpl.resendOtp(email);
        log.info("OTP resent successfully for email: {}", email);
        CommonResponse<String> response = new CommonResponse<>(
                true,
                "OTP code has been resent to your email address.",
                null
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Authentication service is running!");
    }
}
