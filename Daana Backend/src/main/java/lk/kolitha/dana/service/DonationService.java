package lk.kolitha.dana.service;

import lk.kolitha.dana.dto.donation.DonationCreateResponseDto;
import lk.kolitha.dana.dto.donation.DonationRequestDto;
import lk.kolitha.dana.dto.donation.DonationResponseDto;
import lk.kolitha.dana.dto.donor.DonationHistoryResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface DonationService {
    

    Page<DonationResponseDto> getCharityDonations(Long charityId, 
                                                 java.util.Date startDate, 
                                                 java.util.Date endDate, 
                                                 String donorEmail, 
                                                 Boolean isAnonymous, 
                                                 Long programId, 
                                                 org.springframework.data.domain.Pageable pageable);
    

    DonationCreateResponseDto createDonation(DonationRequestDto donationRequest, Long authenticatedDonorId);

    Page<DonationHistoryResponseDto> getDonorDonationHistory(Long donorId, 
                                                           java.util.Date startDate, 
                                                           java.util.Date endDate,
                                                           org.springframework.data.domain.Pageable pageable);
}
