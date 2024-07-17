package at.ac.tuwien.sepm.groupphase.backend.entity;

import at.ac.tuwien.sepm.groupphase.backend.enums.AustriaState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationUser {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_user")
    @SequenceGenerator(name = "seq_user", allocationSize = 5)
    @EqualsAndHashCode.Exclude
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "password", nullable = false, length = 60)
    private String password;

    @Column(name = "name", length = 100)
    private String name;

    @ManyToOne
    @JoinColumn(name = "plz")
    private Plz plz;

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private AustriaState state;

    @Column(name = "coord_lat", precision = 8, scale = 6)
    private BigDecimal coordLat;

    @Column(name = "coord_lon", precision = 9, scale = 6)
    private BigDecimal coordLng;

    @Column(name = "is_locked", nullable = false)
    private boolean isLocked;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "login_attempts", nullable = false)
    private int loginAttempts;

    @CreationTimestamp
    @EqualsAndHashCode.Exclude
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @EqualsAndHashCode.Exclude
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "owner")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Admin admin;

    @OneToOne(mappedBy = "owner", optional = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Renter renter;

    @OneToOne(mappedBy = "owner")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Lender lender;
}
