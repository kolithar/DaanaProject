package lk.kolitha.dana.entity;


import jakarta.persistence.*;
import lk.kolitha.dana.enums.CardType;
import lk.kolitha.dana.enums.ExpireType;
import lk.kolitha.dana.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * DonorCard entity class
 *
 * <p><b>IDEA:</b> Represents payment card information used by a registered donor
 * to make donations. Includes basic card metadata, banking details, and verification flags.</p>
 *
 * @author Kolitha Rathnayake
 * @since 2025-07-27
 */

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonorCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean requestApply;
    private String cardNo;
    private String contactNumber;
    private boolean isDeleted;
    private String cardHolderName;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status acceptingStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpireType cardExpireType;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn( nullable = false)
    private RegisteredDonor registeredDonor;



}
