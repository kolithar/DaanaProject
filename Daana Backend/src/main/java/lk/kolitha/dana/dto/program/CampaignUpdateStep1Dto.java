package lk.kolitha.dana.dto.program;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignUpdateStep1Dto {

    @NotBlank(message = "Program name is required")
    private String programName;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @Email(message = "Invalid email format")
    @NotBlank(message = "Contact person email is required")
    private String contactPersonEmail;
    
    @NotBlank(message = "Contact person mobile is required")
    private String contactPersonMobile;
    
    @NotBlank(message = "Contact person name is required")
    private String contactPersonName;
    
    @NotBlank(message = "Program location is required")
    private String programLocation;
    
    @NotNull(message = "Target donation amount is required")
    @DecimalMin(value = "0.01", message = "Target donation amount must be greater than 0")
    private BigDecimal targetDonationAmount;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;
    
    @NotNull(message = "SubCategory ID is required")
    private Long subCategoryId;
}
