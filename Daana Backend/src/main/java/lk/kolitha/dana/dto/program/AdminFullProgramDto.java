package lk.kolitha.dana.dto.program;

import lk.kolitha.dana.dto.CharityDto;
import lk.kolitha.dana.dto.category.CategoryDto;
import lk.kolitha.dana.dto.category.SubCategoryDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminFullProgramDto {

    private Long id;
    private String urlName;
    private String programName;
    private String title;
    private String description;

    private String contactPersonEmail;
    private String contactPersonMobile;
    private String contactPersonName;

    private String programLocation;
    private BigDecimal targetDonationAmount;
    private BigDecimal raised;

    private String programImage;
    private String programVideo;

    private Date startDate;
    private Date endDate;
    private Date created;
    private Date updated;

    // Enhanced relationships
    private CharityDto charity;
    private SubCategoryDto subCategory;
    private CategoryDto category;

    private String relatedDocument1;
    private String relatedDocument2;
    private String relatedDocument3;
}
