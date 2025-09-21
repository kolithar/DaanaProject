package lk.kolitha.dana.dto.donor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lk.kolitha.dana.enums.Gender;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonorProfileResponseDto {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Gender gender;
    private String profileImageUrl;
    private String phoneNumber;
    private String billingAddress;
    private boolean billingVerify;
    private boolean accountVerifyStatus;
    private String uniqueCustomerId;
    private Date created;
    private Date updated;
    
    // Computed fields
    private String fullName;
    private String displayName;
}
