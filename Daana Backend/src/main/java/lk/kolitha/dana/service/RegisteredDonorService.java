package lk.kolitha.dana.service;

import lk.kolitha.dana.dto.donor.DonorProfileResponseDto;
import lk.kolitha.dana.dto.donor.DonorProfileUpdateDto;
import org.springframework.web.multipart.MultipartFile;

public interface RegisteredDonorService {
    
    /**
     * Get donor profile by ID
     * @param donorId The donor ID
     * @return DonorProfileResponseDto
     */
    DonorProfileResponseDto getDonorProfile(Long donorId);
    
    /**
     * Update donor profile information
     * @param donorId The donor ID
     * @param profileUpdate The profile update data
     * @return Updated DonorProfileResponseDto
     */
    DonorProfileResponseDto updateDonorProfile(Long donorId, DonorProfileUpdateDto profileUpdate);
    
    /**
     * Update donor profile picture
     * @param donorId The donor ID
     * @param profileImage The profile image file
     * @return Updated profile image URL
     */
    String updateProfilePicture(Long donorId, MultipartFile profileImage);
    
    /**
     * Get donor profile by email
     * @param email The donor email
     * @return DonorProfileResponseDto
     */
    DonorProfileResponseDto getDonorProfileByEmail(String email);
}
