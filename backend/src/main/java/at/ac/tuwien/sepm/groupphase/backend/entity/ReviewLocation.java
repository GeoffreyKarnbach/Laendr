package at.ac.tuwien.sepm.groupphase.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "review_location")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewLocation extends Review {

    @OneToOne(mappedBy = "reviewLocation")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Transaction transaction;

    @ManyToOne
    @JoinColumn(name = "reputation_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ReputationLocation reputation;

    @ManyToOne
    @JoinColumn(name = "reviewer_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Renter reviewer;

}
