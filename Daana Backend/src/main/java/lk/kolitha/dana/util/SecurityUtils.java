package lk.kolitha.dana.util;

import lk.kolitha.dana.entity.Charity;
import lk.kolitha.dana.entity.RegisteredDonor;
import lk.kolitha.dana.repository.CharityRepository;
import lk.kolitha.dana.repository.RegisteredDonorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final CharityRepository charityRepository;
    private final RegisteredDonorRepository registeredDonorRepository;

    /**
     * Get the current authenticated user's email
     * @return email of the authenticated user
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * Get the current authenticated charity's ID
     * @return charity ID of the authenticated user
     */
    public Long getCurrentCharityId() {
        String email = getCurrentUserEmail();
        if (email != null) {
            Charity charity = charityRepository.findByEmail(email).orElse(null);
            return charity != null ? charity.getId() : null;
        }
        return null;
    }

    /**
     * Get the current authenticated donor's ID
     * @return donor ID of the authenticated user
     */
    public Long getCurrentDonorId() {
        String email = getCurrentUserEmail();
        if (email != null) {
            RegisteredDonor donor = registeredDonorRepository.findByEmail(email).orElse(null);
            return donor != null ? donor.getId() : null;
        }
        return null;
    }

    /**
     * Get the current authenticated donor entity
     * @return RegisteredDonor entity of the authenticated user
     */
    public RegisteredDonor getCurrentDonor() {
        String email = getCurrentUserEmail();
        if (email != null) {
            return registeredDonorRepository.findByEmail(email).orElse(null);
        }
        return null;
    }
}
