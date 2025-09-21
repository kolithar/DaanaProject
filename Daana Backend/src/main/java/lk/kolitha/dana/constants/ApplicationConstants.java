package lk.kolitha.dana.constants;

public final class ApplicationConstants {
    
    public static final class NotFoundConstants {
        public static final String NO_USER_FOUND = "User not found";
        public static final String NO_DONOR_FOUND = "Donor not found";
        public static final String NO_CHARITY_FOUND = "Charity not found";
        public static final String NO_ADMIN_FOUND = "Admin user not found";
        public static final String NO_PROGRAM_FOUND = "Program not found";
        public static final String NO_DONATION_FOUND = "Donation not found";
        public static final String NO_CATEGORY_FOUND = "Category not found";
        public static final String NO_SUBCATEGORY_FOUND = "Subcategory not found";
        public static final String NO_BANK_DETAIL_FOUND = "Bank detail not found";
        public static final String NO_PACKAGE_FOUND = "Package not found";
    }
    
    public static final class ConflictConstants {
        public static final String USER_ALREADY_EXISTS = "User already exists";
        public static final String EMAIL_ALREADY_REGISTERED = "Email already registered";
        public static final String CHARITY_ALREADY_EXISTS = "Charity already exists";
        public static final String PROGRAM_ALREADY_EXISTS = "Program already exists";
    }
    
    public static final class ValidationConstants {
        public static final String INVALID_ROLE = "Invalid role specified";
        public static final String INVALID_EMAIL_FORMAT = "Invalid email format";
        public static final String INVALID_PHONE_FORMAT = "Invalid phone number format";
        public static final String PASSWORD_TOO_SHORT = "Password must be at least 6 characters";
        public static final String REQUIRED_FIELD_MISSING = "Required field is missing";
    }
    
    public static final class AuthenticationConstants {
        public static final String INVALID_CREDENTIALS = "Invalid email or password";
        public static final String USER_NOT_VERIFIED = "User account not verified";
        public static final String ACCOUNT_LOCKED = "Account is locked";
        public static final String TOKEN_EXPIRED = "Authentication token expired";
        public static final String INVALID_TOKEN = "Invalid authentication token";
    }
    
    public static final class BusinessConstants {
        public static final String DONATION_FAILED = "Donation processing failed";
        public static final String INSUFFICIENT_FUNDS = "Insufficient funds";
        public static final String PROGRAM_NOT_ACTIVE = "Program is not active";
        public static final String CHARITY_NOT_ACTIVE = "Charity is not active";
    }
    
    public static final class S3FolderConstants {
        public static final String CHARITY_DOCUMENTS = "charity-documents/";
        public static final String PROGRAM_DOCUMENTS = "program-documents/";
        public static final String DONOR_DOCUMENTS = "donor-documents/";
        public static final String PROFILE_IMAGES = "profile-images/";
        public static final String LOGO_IMAGES = "logo-images/";
        public static final String PROGRAM_VIDEO = "program-video/";
        public static final String PROOF_DOCUMENTS = "proof-documents/";
        public static final String BANK_DOCUMENTS = "bank-documents/";
        public static final String PAYMENT_SLIP = "bank-payment-slip/";
    }
    
    private ApplicationConstants() {
        // Private constructor to prevent instantiation
    }
}
