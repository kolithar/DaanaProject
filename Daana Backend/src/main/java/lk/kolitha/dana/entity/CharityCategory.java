package lk.kolitha.dana.entity;

import jakarta.persistence.*;
import lk.kolitha.dana.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CharityCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private String registeredModeratorUsername;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    @Temporal(TemporalType.TIMESTAMP)
    private Date deleted;

    @ManyToOne
    @JoinColumn( nullable = false)
    private Charity charity;

    @ManyToOne
    @JoinColumn( nullable = false)
    private Category category;
}
