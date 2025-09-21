package lk.kolitha.dana.dto;

import lk.kolitha.dana.enums.ExecutionType;
import lk.kolitha.dana.enums.Status;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class CharityDto {

    private String name;
    private String email;
    private String password;   // will hash before saving
    private String charityDescription;
    private ExecutionType executionType;
    private String charityEstablishment;
    private String charityLogo;
    private String charityRepresentPerson;
    private boolean needSupportingDocuments;
    private String executionLetterStatus;
    private boolean didExeLetterServiceCharge;
    private int fixedLoginAttemptCount;
    private String createdUsername;
    private String updatedUsername;
    private double executionLetterCharge;

    private Date created;
    private Date updated;

    // KYC data
    private String nicNumber; // only if PERSON
    private MultipartFile charityRegistrationDocument; // only if ORGANIZATION

    private String contactPersonName;
    private int contactPersonMobile;

}
