package lk.kolitha.dana.dto.charity;

import lk.kolitha.dana.enums.ExecutionType;
import lk.kolitha.dana.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CharityRegistrationResponseDto {

    private Long id;
    private String name;
    private String email;
    private ExecutionType executionType;
    private String website;
    private String description;
    private String logoUrl;
    private Integer mobileNumber;
    private String nicNumberOrRegistrationNumber;
    private String contactPersonName;
    private Integer contactPersonMobile;
    private String contactPersonEmail;
    private Status status;
    private boolean accountVerifyStatus;
    private Date created;
    private Date updated;
    
    // Additional fields for step 2 response
    private String documentType;
    private String documentFileName;
    private String bankName;
    private String branchName;
    private String accountHolderName;
    private String swiftCode;
    private String accountNumber;
}
