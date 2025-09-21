package lk.kolitha.dana.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lk.kolitha.dana.enums.PaymentMethod;
import lk.kolitha.dana.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Digits(integer = 9, fraction = 2)
    private BigDecimal actualDonationAmount;

    @Digits(integer = 9, fraction = 2)
    private BigDecimal netDonationAmount;

    private double serviceCharge;

    private Boolean isAnonymousDonation; //if an anonymous donation is true, donor details or mapping will not be saved

    @Lob
    private String comments; //positive comments from donor

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    private String paymentSlipUrl;

    @Column(nullable = false, unique = true)
    private String paymentReferenceNumber;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = false, nullable = false)
    private Campaigns campaigns;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = false, nullable = true)
    private DonationPackage donationPackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = false, nullable = true)
    private RegisteredDonor registeredDonor;

}
