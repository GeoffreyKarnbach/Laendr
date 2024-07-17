package at.ac.tuwien.sepm.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class Reputation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_reputation")
    @SequenceGenerator(name = "seq_reputation", allocationSize = 5)
    @EqualsAndHashCode.Exclude
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "average_rating", precision = 7, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "ratings", nullable = false)
    private int ratings;

    @Column(name = "karma", nullable = false, precision = 6, scale = 5)
    private BigDecimal karma;

    @Column(name = "weight_positive", nullable = false, precision = 8, scale = 5)
    private BigDecimal weightPositive;

    @Column(name = "weight_negative", nullable = false, precision = 8, scale = 5)
    private BigDecimal weightNegative;

    @CreationTimestamp
    @EqualsAndHashCode.Exclude
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @EqualsAndHashCode.Exclude
    private LocalDateTime updatedAt;

}
