package lk.kolitha.dana.service;

import lk.kolitha.dana.dto.CharityDto;
import lk.kolitha.dana.dto.charity.CharityRegistrationStep1Dto;
import lk.kolitha.dana.dto.charity.CharityRegistrationStep2Dto;
import lk.kolitha.dana.dto.charity.CharityRegistrationStep3Dto;
import lk.kolitha.dana.dto.charity.CharityRegistrationResponseDto;
import lk.kolitha.dana.dto.charity.CharityProfileDto;
import lk.kolitha.dana.dto.charity.PasswordChangeDto;
import lk.kolitha.dana.dto.charity.ProfileUpdateDto;
import lk.kolitha.dana.entity.Charity;

public interface CharityService {

        void registerCharity(CharityDto charityDto);
        Charity approveCharity(Long charityId);
        Charity rejectCharity(Long charityId);
        
        // New 3-step registration methods
        CharityRegistrationResponseDto registerCharityStep1(CharityRegistrationStep1Dto step1Dto);
        CharityRegistrationResponseDto registerCharityStep2(CharityRegistrationStep2Dto step2Dto);
        CharityRegistrationResponseDto verifyCharityOtp(CharityRegistrationStep3Dto step3Dto);
        void resendCharityOtp(String email);
        
        // Charity profile methods
        CharityProfileDto getCharityProfile(Long charityId);
        
        // Charity account management methods
        void changePassword(Long charityId, PasswordChangeDto passwordChangeDto);
        CharityProfileDto updateProfile(Long charityId, ProfileUpdateDto profileUpdateDto);
    }

