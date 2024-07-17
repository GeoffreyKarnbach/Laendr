package at.ac.tuwien.sepm.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;

@Entity
@Table(name = "location_tag")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationTag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_product_tag")
    @SequenceGenerator(name = "seq_product_tag", allocationSize = 5)
    @EqualsAndHashCode.Exclude
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @ManyToMany(mappedBy = "tags")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Location> locations;

}
