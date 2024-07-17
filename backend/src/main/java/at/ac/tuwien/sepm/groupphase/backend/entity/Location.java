package at.ac.tuwien.sepm.groupphase.backend.entity;

import at.ac.tuwien.sepm.groupphase.backend.enums.AustriaState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "location")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_location")
    @SequenceGenerator(name = "seq_location", allocationSize = 5)
    @EqualsAndHashCode.Exclude
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", length = 1000, nullable = false)
    private String description;

    @Column(name = "is_removed", nullable = false)
    private boolean removed;

    @ManyToOne
    @JoinColumn(name = "plz")
    private Plz plz;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private AustriaState state;

    @Column(name = "address", length = 200, nullable = false)
    private String address;

    @Column(name = "size_in_m2", nullable = false, precision = 10, scale = 2)
    private BigDecimal sizeInM2;

    @Column(name = "coord_lat", precision = 8, scale = 6)
    private BigDecimal coordLat;

    @Column(name = "coord_lon", precision = 9, scale = 6)
    private BigDecimal coordLng;

    @CreationTimestamp
    @EqualsAndHashCode.Exclude
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @EqualsAndHashCode.Exclude
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "lender_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Lender owner;

    @OneToMany(mappedBy = "owningLocation")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Timeslot> timeslots;

    @OneToOne
    @JoinColumn(name = "reputation_id", referencedColumnName = "id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ReputationLocation reputation;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "location_to_location_tag",
        joinColumns = @JoinColumn(name = "location_id"),
        inverseJoinColumns = @JoinColumn(name = "location_tag_id")
    )
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<LocationTag> tags;

    @OneToOne
    @JoinColumn(name = "primary_image_id", referencedColumnName = "id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Image primaryImage;

    @OneToMany(mappedBy = "location")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Image> images;

}
