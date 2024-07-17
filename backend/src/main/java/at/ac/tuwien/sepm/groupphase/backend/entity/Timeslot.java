package at.ac.tuwien.sepm.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "timeslot")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Timeslot {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_timeslot")
    @SequenceGenerator(name = "seq_timeslot", allocationSize = 5)
    @EqualsAndHashCode.Exclude
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime start;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime end;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "price_hourly", precision = 10, scale = 2)
    private BigDecimal priceHourly;

    @Column(name = "is_used", nullable = false)
    private boolean used;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @CreationTimestamp
    @EqualsAndHashCode.Exclude
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @EqualsAndHashCode.Exclude
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Location owningLocation;

    @OneToMany(mappedBy = "timeslot")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Transaction> transaction;

}
