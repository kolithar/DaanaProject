package lk.kolitha.dana.entity;

import jakarta.persistence.*;
import lk.kolitha.dana.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * SubCategory entity class
 *
 * <p><b>IDEA:</b> Subcategories are child categories that belong to a parent Category.
 * They allow more specific classification under broader categories (e.g., under Health: Mental Health).</p>
 *
 * @author Kolitha Rathnayake
 * @since 2025-07-27
 */

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;


    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = false, nullable = true)
    private Category category;

    @OneToMany(mappedBy = "subCategory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Campaigns> Campaigns = new ArrayList<>();


}
