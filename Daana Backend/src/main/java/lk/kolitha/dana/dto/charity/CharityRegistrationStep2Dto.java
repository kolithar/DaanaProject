package lk.kolitha.dana.dto.charity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lk.kolitha.dana.enums.CharityProofDocumentType;
import lk.kolitha.dana.enums.ExecutionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CharityRegistrationStep2Dto {

    private Long id;

    @NotBlank(message = "Document type is required")
    private String documentType;

    @NotNull(message = "Document file is required")
    private MultipartFile documentFile;

    private MultipartFile logoFile;

    // Bank details
    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Branch name is required")
    private String branchName;

    @NotBlank(message = "Account holder name is required")
    private String accountHolderName;

    private String swiftCode;

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    /**
     * Gets the document type as enum
     */
    public CharityProofDocumentType getDocumentTypeAsEnum() {
        try {
            return CharityProofDocumentType.valueOf(documentType);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Validates that the document type matches the execution type requirements
     * PERSON => ID_CARD (NIC)
     * ORGANIZATION => BUSINESS_REGISTRATION_CERTIFICATE (BR)
     */
    public boolean isValidDocumentForExecutionType(ExecutionType executionType) {
        if (executionType == null || documentType == null) {
            return false;
        }
        
        CharityProofDocumentType docTypeEnum = getDocumentTypeAsEnum();
        if (docTypeEnum == null) {
            return false;
        }
 
        return switch (executionType) {
            case PERSON -> docTypeEnum == CharityProofDocumentType.ID_CARD;
            case ORGANIZATION -> docTypeEnum == CharityProofDocumentType.BUSINESS_REGISTRATION_CERTIFICATE;
            default -> false;
        };
    }
    
    /**
     * Gets the required document type for the given execution type
     */
    public static CharityProofDocumentType getRequiredDocumentType(ExecutionType executionType) {
        if (executionType == null) {
            return null;
        }
        
        switch (executionType) {
            case PERSON:
                return CharityProofDocumentType.ID_CARD;
            case ORGANIZATION:
                return CharityProofDocumentType.BUSINESS_REGISTRATION_CERTIFICATE;
            default:
                return null;
        }
    }
}
