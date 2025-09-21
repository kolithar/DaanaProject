package lk.kolitha.dana.service;

import lombok.RequiredArgsConstructor;
import lk.kolitha.dana.constants.ApplicationConstants;
import lk.kolitha.dana.entity.AdminUser;
import lk.kolitha.dana.entity.Charity;
import lk.kolitha.dana.entity.RegisteredDonor;
import lk.kolitha.dana.enums.Role;
import lk.kolitha.dana.enums.Status;
import lk.kolitha.dana.exception.CustomServiceException;
import lk.kolitha.dana.repository.AdminUserRepository;
import lk.kolitha.dana.repository.CharityRepository;
import lk.kolitha.dana.repository.RegisteredDonorRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final RegisteredDonorRepository donorRepository;
    private final CharityRepository charityRepository;
    private final AdminUserRepository adminUserRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        // Check if it's a donor
        try {
            RegisteredDonor donor = donorRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomServiceException(404, ApplicationConstants.NotFoundConstants.NO_DONOR_FOUND));
            
            if (!donor.isDeleted() && donor.isAccountVerifyStatus()) {
                return User.builder()
                        .username(donor.getEmail())
                        .password(donor.getPasswordHash())
                        .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + Role.DONOR.name())))
                        .build();
            }
        } catch (CustomServiceException e) {
            // Donor not found, continue to check other user types
        }
        
        // Check if it's a charity
        try {
            Charity charity = charityRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomServiceException(404, ApplicationConstants.NotFoundConstants.NO_CHARITY_FOUND));
            
            if (!charity.isDeleted() && charity.getStatus() == Status.ACTIVE) {
                return User.builder()
                        .username(charity.getEmail())
                        .password(charity.getPasswordHash())
                        .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + Role.CHARITY.name())))
                        .build();
            }
        } catch (CustomServiceException e) {
            // Charity not found, continue to check admin
        }
        
        // Check if it's an admin
        try {
            AdminUser admin = adminUserRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomServiceException(404, ApplicationConstants.NotFoundConstants.NO_ADMIN_FOUND));
            
            if (admin.getAdminStatus() == Status.ACTIVE) {
                return User.builder()
                        .username(admin.getEmail())
                        .password(admin.getPasswordHash())
                        .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + admin.getAdminRole().name())))
                        .build();
            }
        } catch (CustomServiceException e) {
            // Admin not found
        }
        
        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
