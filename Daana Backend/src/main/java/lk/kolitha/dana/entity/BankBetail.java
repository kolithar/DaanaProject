package lk.kolitha.dana.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * BankDetail entity class
 *
 * <p><b>IDEA:</b> Stores bank account details associated with a donor or charity.
 * This information is used for processing transactions, verifying bank information,
 * and linking financial accounts to the system's entities.</p>
 *
 * @author Kolitha Rathnayake
 * @since 2025-08-09
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

public class BankBetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String branchName;

    @Column(nullable = false)
    private String accountHolderName;

    private String swiftCode;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programId", nullable = true)
    private Campaigns campaigns;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charity_id", nullable = true)
    private Charity charity;
}
