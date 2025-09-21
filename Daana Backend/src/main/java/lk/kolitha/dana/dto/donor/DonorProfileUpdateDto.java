package lk.kolitha.dana.dto.donor;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lk.kolitha.dana.enums.Gender;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonorProfileUpdateDto {
    
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;
    
    private Gender gender;
    
    @Size(max = 15, message = "Phone number cannot exceed 15 characters")
    private String phoneNumber;
    
    @Size(max = 500, message = "Billing address cannot exceed 500 characters")
    private String billingAddress;
}
