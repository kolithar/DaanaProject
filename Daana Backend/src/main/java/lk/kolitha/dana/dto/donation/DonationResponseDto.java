package lk.kolitha.dana.dto.donation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationResponseDto {
    
    private Long id;
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
    private Date updated;
    
    // Program information
    private Long programId;
    private String programName;
    private String programTitle;
    private String programUrlSlug;
    
    // Donor information (only if not anonymous)
    private Long donorId;
    private String donorName;
    private String donorEmail;
    private String donorMobile;
    private String donorProfileImage;
    
    // Additional calculated fields
    private BigDecimal completionPercentage;
    private String donationDateFormatted;
    private String donorDisplayName;
}
