package lk.kolitha.dana.dto.program;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignUpdateStep2Dto {

    private MultipartFile programImage;
    private MultipartFile programVideo;
    private MultipartFile relatedDocument1;
    private MultipartFile relatedDocument2;
    private MultipartFile relatedDocument3;
}
