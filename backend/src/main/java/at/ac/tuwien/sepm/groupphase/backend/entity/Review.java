package at.ac.tuwien.sepm.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public abstract class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_review")
    @SequenceGenerator(name = "seq_review", allocationSize = 5)
    @EqualsAndHashCode.Exclude
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "rating", nullable = false)
    @Min(0) @Max(4)
    private int rating;

    @Column(name = "comment")
    private String comment;

    @Column(name = "reviewer_karma", nullable = false, precision = 6, scale = 5)
    private BigDecimal reviewerKarmaAtReview;

    @Column(name = "reviewee_karma", nullable = false, precision = 6, scale = 5)
    private BigDecimal revieweeKarmaAtReview;

    @CreationTimestamp
    @EqualsAndHashCode.Exclude
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @EqualsAndHashCode.Exclude
    private LocalDateTime updatedAt;

}
