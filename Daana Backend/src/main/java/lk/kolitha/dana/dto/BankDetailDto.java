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
public class BankDetailDto {
    private Long bankDetailId;
    private String accountNo;
    private String bankName;
    private String branchName;
    private String bankLogo;
    private LocalDateTime created;
    private LocalDateTime updated;
    private Long charityId;
    private Long programId;
}
