package at.ac.tuwien.sepm.groupphase.backend.entity;

import at.ac.tuwien.sepm.groupphase.backend.enums.AppRole;
import at.ac.tuwien.sepm.groupphase.backend.enums.CancelReason;
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
@Table(name = "transaction")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_transaction")
    @SequenceGenerator(name = "seq_transaction", allocationSize = 5)
    @EqualsAndHashCode.Exclude
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "initial_message", nullable = false)
    private String initialMessage;

    @Column(name = "amount_paid")
    private BigDecimal amountPaid;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "is_cancelled")
    private Boolean cancelled;

    @Column(name = "cancel_by_role")
    @Enumerated(EnumType.STRING)
    private AppRole cancelByRole;

    @Column(name = "cancel_reason")
    @Enumerated(EnumType.STRING)
    private CancelReason cancelReason;

    @Column(name = "cancel_description")
    private String cancelDescription;

    @Column(name = "is_cancel_notified")
    private Boolean cancelNotified;

    @CreationTimestamp
    @EqualsAndHashCode.Exclude
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @EqualsAndHashCode.Exclude
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "timeslot_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Timeslot timeslot;

    @ManyToOne
    @JoinColumn(name = "renter_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Renter renter;

    @OneToOne
    @JoinColumn(name = "review_renter_id", referencedColumnName = "id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ReviewRenter reviewRenter;

    @OneToOne
    @JoinColumn(name = "review_location_id", referencedColumnName = "id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ReviewLocation reviewLocation;

}
