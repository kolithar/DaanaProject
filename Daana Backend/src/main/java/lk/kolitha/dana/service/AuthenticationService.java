package lk.kolitha.dana.service;

import lk.kolitha.dana.dto.AuthenticationRequest;
import lk.kolitha.dana.dto.AuthenticationResponse;
import lk.kolitha.dana.dto.charity.CharityForgotPasswordRequest;
import lk.kolitha.dana.dto.charity.CharityLoginRequest;
import lk.kolitha.dana.dto.charity.CharityResetPasswordRequest;
import lk.kolitha.dana.dto.donor.DonorRegisterRequest;
import org.springframework.stereotype.Service;

@Service
public interface AuthenticationService {
    void donorRegister(DonorRegisterRequest request);

    AuthenticationResponse authenticate(AuthenticationRequest request);
    
    AuthenticationResponse charityLogin(CharityLoginRequest request);
    
    void charityForgotPassword(CharityForgotPasswordRequest request);
    
    void charityResetPassword(CharityResetPasswordRequest request);
    
    void verifyOtp(String email, String otpCode);
    
    void resendOtp(String email);
}
