package lk.kolitha.dana.entity;

import jakarta.persistence.*;
import lk.kolitha.dana.enums.Role;
import lk.kolitha.dana.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * AdminUser entity class
 * Represents an administrator user in the system.
 * This class is currently empty and can be extended in the future
 * to include properties and methods relevant to admin users.
 * It serves as a placeholder for future development
 * and can be used to manage administrative functionalities.
 * @author Kolitha Rathnayake
 * @since 2025-07-27
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, unique = true)
    private String email;

    private String fullName;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status adminStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role adminRole;

}
