package lk.kolitha.dana.dto.program;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CampaignUpdateRequestDto {
    private String programName;
    private String title;
    private String description;
    private String contactPersonEmail;
    private String contactPersonMobile;
    private String contactPersonName;
    private String programLocation;
    private BigDecimal targetDonationAmount;
    private Long subCategoryId;
    
    // Optional file uploads
    private MultipartFile programImage;
    private MultipartFile programVideo;
    private MultipartFile relatedDocument1;
    private MultipartFile relatedDocument2;
    private MultipartFile relatedDocument3;
}
