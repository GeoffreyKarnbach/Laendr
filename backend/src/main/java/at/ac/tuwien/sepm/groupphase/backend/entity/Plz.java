package at.ac.tuwien.sepm.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "plz")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plz {

    @Id
    @Column(name = "plz", length = 4, updatable = false)
    private String plz;

    @Column(name = "ort", length = 50, updatable = false)
    private String ort;

    @Column(name = "coord_lat", precision = 8, scale = 6)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private BigDecimal coordLat;

    @Column(name = "coord_lon", precision = 9, scale = 6)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private BigDecimal coordLng;


}
