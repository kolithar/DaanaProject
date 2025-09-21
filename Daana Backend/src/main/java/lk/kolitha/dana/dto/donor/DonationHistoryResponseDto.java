package lk.kolitha.dana.dto.donor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationHistoryResponseDto {
    
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
    private Date updated;
    
    // Campaign information
    private Long campaignId;
    private String campaignName;
    private String campaignTitle;
    private String campaignUrlSlug;
    private String campaignImage;
    private String campaignDescription;
    private BigDecimal campaignTargetAmount;
    private BigDecimal campaignRaisedAmount;
    private Date campaignStartDate;
    private Date campaignEndDate;
    private String campaignStatus;
    
    // Charity information
    private Long charityId;
    private String charityName;
    private String charityLogo;
    
    // Category information
    private Long categoryId;
    private String categoryName;
    private Long subCategoryId;
    private String subCategoryName;
    
    // Computed fields
    private String donationDateFormatted;
    private BigDecimal campaignCompletionPercentage;
    private String donorDisplayName;
}
