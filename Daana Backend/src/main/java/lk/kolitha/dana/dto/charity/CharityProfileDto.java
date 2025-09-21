package lk.kolitha.dana.dto.charity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CharityProfileDto {
    
    // Basic Information
    private Long id;
    private String name;
    private String email;
    private String description;
    private String executionType;
    private String logoUrl;
    private String contactPersonName;
    private String contactPersonMobile;
    private String nicNumberOrRegistrationNumber;
    private String address;
    private String website;
    private String phoneNumber;
    
    // Status Information
    private String status;
    private boolean accountVerifyStatus;
    private boolean isBillingVerify;
    private boolean isDeleted;
    
    // Document Information
    private String registrationDocumentUrl;
    private String taxDocumentUrl;
    private String bankDocumentUrl;
    private String otherDocumentUrl;
    
    // Bank Information
    private String bankName;
    private String bankAccountNumber;
    private String bankBranch;
    
    // Timestamps
    private Date created;
    private Date updated;
    
    // Statistics (if needed)
    private Long totalPrograms;
    private Long activePrograms;
    private Long totalDonations;
    private String totalRaisedAmount;
    
    // Additional Information
    private String optCode;
    private Date optCodeGeneratedTimestamp;
}
