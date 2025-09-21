package lk.kolitha.dana.dto.program;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BasicProgramCardDataResDto {
    private Long programId;
    private String programName;
    private String programTitle;
    private String programDescription;
    private String subCategoryName;
    private String location;
    private String programImageUrl;
    private String urlSlug;
    private BigDecimal targetDonationAmount;
    private BigDecimal raised;

}
