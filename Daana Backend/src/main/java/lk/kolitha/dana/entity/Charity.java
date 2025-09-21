package lk.kolitha.dana.entity;

import jakarta.persistence.*;
import lk.kolitha.dana.enums.ExecutionType;
import lk.kolitha.dana.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Charity entity class
 *
 * <p><b>IDEA:</b> Represents an organization registered to receive donations and run programs.
 * Contains registration, document, representative, and status tracking information.</p>
 *
 * @author Kolitha Rathnayake
 * @since 2025-07-27
 */

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Charity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private ExecutionType executionType;

    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String passwordHash;


    @Column(nullable = false)
    private String name; //charity name or person name
    private String website;
    @Lob
    private String description;
    private String logoUrl;
    private int mobileNumber;
    private String nicNumberOrRegistrationNumber;

    //contact person (charity representative)
    private String contactPersonName;
    private int contactPersonMobile;
    private String contactPersonEmail;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private boolean isDeleted;

    // OTP verification fields
    private String otpCode;
    @Temporal(TemporalType.TIMESTAMP)
    private Date otpCodeGeneratedTimestamp;
    private boolean accountVerifyStatus;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    @OneToMany(mappedBy = "charity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CharityCategory> charityCategories = new ArrayList<>();

    @OneToMany(mappedBy = "charity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CharityProofDocument> charityProofDocuments = new ArrayList<>();

    @OneToOne(mappedBy = "charity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private BankBetail bankDetail;

    @OneToMany(mappedBy = "charity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Campaigns> campaigns = new ArrayList<>();

}
