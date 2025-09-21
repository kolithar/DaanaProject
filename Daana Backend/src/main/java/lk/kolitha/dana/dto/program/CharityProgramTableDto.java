package lk.kolitha.dana.dto.program;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CharityProgramTableDto {
    private Long id;
    private String programName;
    private String title;
    private String subCategoryName;
    private String status;
    private BigDecimal targetDonationAmount;
    private BigDecimal raised;
    private Date created;
    private Date updated;
    private String programImage;
}
