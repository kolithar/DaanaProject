package lk.kolitha.dana.dto.charity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.kolitha.dana.enums.ExecutionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CharityRegistrationStep1Dto {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotNull(message = "Execution type is required")
    private ExecutionType executionType;

    private String website;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Mobile number is required")
    private Integer mobileNumber;

    @NotBlank(message = "NIC or Registration number is required")
    private String nicNumberOrRegistrationNumber;

    @NotBlank(message = "Contact person name is required")
    private String contactPersonName;

    @NotNull(message = "Contact person mobile is required")
    private Integer contactPersonMobile;

    @NotBlank(message = "Contact person email is required")
    @Email(message = "Please provide a valid contact person email address")
    private String contactPersonEmail;
}
