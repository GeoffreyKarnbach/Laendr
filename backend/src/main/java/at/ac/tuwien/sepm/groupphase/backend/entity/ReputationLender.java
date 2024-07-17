package at.ac.tuwien.sepm.groupphase.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "reputation_lender")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReputationLender extends Reputation {

    @OneToOne(mappedBy = "reputation")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Lender lender;

}
