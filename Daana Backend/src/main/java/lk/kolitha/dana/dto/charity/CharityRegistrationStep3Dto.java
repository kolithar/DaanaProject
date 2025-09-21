package lk.kolitha.dana.dto.charity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CharityRegistrationStep3Dto {

    private Long id;

    @NotBlank(message = "OTP code is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP code must be exactly 6 digits")
    private String otpCode;
}
