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
@Table(name = "reputation_location")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReputationLocation extends Reputation {

    @OneToOne(mappedBy = "reputation")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Location location;

    @OneToMany(mappedBy = "reputation")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<ReviewLocation> reviews;

}
