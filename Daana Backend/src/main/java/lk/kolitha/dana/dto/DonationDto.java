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
public class DonationDto {
    private Long id;
    private Double amount;
    private String donorName;
    private String donorEmail;
    private LocalDateTime donationDate;
    private String message;
    private boolean isAnonymous;


}
