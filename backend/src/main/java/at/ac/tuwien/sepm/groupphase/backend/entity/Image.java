package at.ac.tuwien.sepm.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "product_image")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_product_image")
    @SequenceGenerator(name = "seq_product_image", allocationSize = 5)
    @EqualsAndHashCode.Exclude
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "url", nullable = false, length = 200)
    private String url;

    @Column(name = "position", nullable = false)
    private int position;

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
    private Location location;
}
