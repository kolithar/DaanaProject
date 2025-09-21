package lk.kolitha.dana.dto.charity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateDto {
    private String name;
    private String description;
    private String website;
    private String contactPersonName;
    private String contactPersonMobile;
    private String contactPersonEmail;
    private String nicNumberOrRegistrationNumber;
    private String phoneNumber;
}
