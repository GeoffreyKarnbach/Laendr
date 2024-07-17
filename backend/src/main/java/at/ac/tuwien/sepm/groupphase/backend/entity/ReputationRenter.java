package at.ac.tuwien.sepm.groupphase.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Entity
@Table(name = "reputation_renter")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReputationRenter extends Reputation {

    @OneToOne(mappedBy = "reputation")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Renter renter;

    @OneToMany(mappedBy = "reputation")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<ReviewRenter> reviews;

}
