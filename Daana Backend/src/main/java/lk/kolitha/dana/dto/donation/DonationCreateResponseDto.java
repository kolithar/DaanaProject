package lk.kolitha.dana.dto.donation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationCreateResponseDto {
    
    private Long donationId;
    private BigDecimal actualDonationAmount;
    private BigDecimal netDonationAmount;
    private double serviceCharge;
    private Boolean isAnonymousDonation;
    private String comments;
    private String status;
    private String paymentMethod;
    private String paymentSlipUrl;
    private String paymentReferenceNumber;
    private Date created;
    
    // Campaign information
    private Long campaignId;
    private String campaignName;
    private String campaignTitle;
    
    // Success message
    private String message;
}
