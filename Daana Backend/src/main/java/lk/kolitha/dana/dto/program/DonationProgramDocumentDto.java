package lk.kolitha.dana.dto.program;

import lk.kolitha.dana.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DonationProgramDocumentDto {
    private Long id;
    private String documentName;
    private String documentPath;
    private Status status;
    private boolean published;
    private Date updated;
    private Date deleted;
}
