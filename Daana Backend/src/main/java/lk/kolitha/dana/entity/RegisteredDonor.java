package lk.kolitha.dana.entity;

import jakarta.persistence.*;
import lk.kolitha.dana.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * RegisteredDonor entity class
 *
 * <p><b>IDEA:</b> Represents a user who has registered in the system to donate.
 * Stores personal info, verification status, login attempts, and contact data.</p>
 *
 * @author Kolitha Rathnayake
 * @since 2025-07-27
 */


@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisteredDonor {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean accountVerifyStatus;

    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    @Lob
    private String passwordHash;
    
    private int threeLoginAttemptCount;
    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;


    private String profileImageUrl;
    private String phoneNumber;
    private String billingAddress;
    private boolean isBillingVerify;
    private String uniqueCustomerId;
    private boolean isDeleted;

    private String optCode;
    @Temporal(TemporalType.TIMESTAMP)
    private Date optCodeGeneratedTimestamp;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;


    @OneToMany(mappedBy = "registeredDonor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DonorCard> donorCards = new ArrayList<>();


    @OneToMany(mappedBy = "registeredDonor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Donation> donation = new ArrayList<>();

}