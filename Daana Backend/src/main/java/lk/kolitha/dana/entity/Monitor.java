package lk.kolitha.dana.entity;

import jakarta.persistence.*;
import lk.kolitha.dana.enums.Role;
import lk.kolitha.dana.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Kolitha Rathnayake
 * @since 2025-07-27
 * @implNote add Moderator entity
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Monitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String directorApprovalName;

    private int threeLoginAttemptCount;
    private String mode;
    private String moderatorUsername;
    private String moderatorDescription;
    private String moderatorPhone;
    private String moderatorName;
    private String password;
    private boolean isDeleted;
    private LocalDateTime lastLoginOutTimestamp;



    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    @Temporal(TemporalType.TIMESTAMP)
    private Date deleted;


    @OneToMany(mappedBy = "monitor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MonitorCategory> moderatorCategories = new ArrayList<>();
}
