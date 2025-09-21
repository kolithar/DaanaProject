package lk.kolitha.dana.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProgramDto {
    private Long programId;
    private String title;
    private String description;
    private String location;
    private String zone;
    private Double targetDonationAmount;
    private LocalDateTime created;
    private LocalDateTime updated;
    private Long charityId;       // reference to Charity
    private Long subCategoryId;   // reference to SubCategory
}
