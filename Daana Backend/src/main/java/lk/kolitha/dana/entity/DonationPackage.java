package lk.kolitha.dana.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lk.kolitha.dana.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Package entity class
 * @author Kolitha Rathnayake
 * @since 2025-08-05
 */

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

public class DonationPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Digits(integer = 9, fraction = 2)
    private BigDecimal paketAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    @OneToMany(mappedBy = "donationPackage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Donation> donation = new ArrayList<>();


}
