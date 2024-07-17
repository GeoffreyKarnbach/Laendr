package at.ac.tuwien.sepm.groupphase.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
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
import java.util.Set;

@Entity
@Table(name = "renter")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Renter {

    @Id
    @Column(name = "user_id", updatable = false)
    private Long id;

    @OneToOne(optional = false, cascade = {CascadeType.MERGE})
    @PrimaryKeyJoinColumn
    private ApplicationUser owner;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @CreationTimestamp
    @EqualsAndHashCode.Exclude
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @EqualsAndHashCode.Exclude
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "renter")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Transaction> transactions;

    @OneToOne
    @JoinColumn(name = "reputation_id", referencedColumnName = "id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ReputationRenter reputation;

}
