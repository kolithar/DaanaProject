package lk.kolitha.dana.entity;

import jakarta.persistence.*;
import lk.kolitha.dana.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.*;
import jakarta.validation.constraints.Digits;


/**
 * Program entity class
 * @author Kolitha Rathnayake
 * @since 2025-08-05
 */

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Campaigns {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String urlName;
    private String programName;
    private String title;
    @Column(length = 2000)
    private String description;
    private String contactPersonEmail;
    private String contactPersonMobile;
    private String contactPersonName;

    private boolean deleted;
    @Lob
    private String programLocation;

    @Digits(integer = 9, fraction = 2)
    private BigDecimal targetDonationAmount;
    @Digits(integer = 9, fraction = 2)
    private BigDecimal raised;

    private String programImage;
    private String programVideo;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = false, nullable = true)
    private Charity charity;

    private String relatedDocument1;
    private String relatedDocument2;
    private String relatedDocument3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = false, nullable = true)
    private SubCategory subCategory;

    @OneToMany(mappedBy = "campaigns", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Donation> donation = new ArrayList<>();



}
