package lk.kolitha.dana.dto.donation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lk.kolitha.dana.enums.PaymentMethod;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationRequestDto {
    
    @NotNull(message = "Campaign ID is required")
    private Long campaignId;
    
    @NotNull(message = "Donation amount is required")
    @DecimalMin(value = "1.00", message = "Donation amount must be at least 1.00")
    private BigDecimal actualDonationAmount;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    @Size(max = 1000, message = "Comments cannot exceed 1000 characters")
    private String comments;
    
    // Payment slip upload (optional for some payment methods)
    private MultipartFile paymentSlipUrl;
}
