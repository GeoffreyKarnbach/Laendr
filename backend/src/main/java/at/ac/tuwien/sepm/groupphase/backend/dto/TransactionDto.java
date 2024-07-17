package at.ac.tuwien.sepm.groupphase.backend.dto;

import at.ac.tuwien.sepm.groupphase.backend.enums.AppRole;
import at.ac.tuwien.sepm.groupphase.backend.enums.CancelReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {

    private Long id;

    private String locationName;

    private Long locationId;

    private Boolean locationRemoved;

    private String partnerName;

    private ReputationDto partnerReputation;

    private String partnerEmail;

    private String partnerPhone;

    private Long lenderId;

    private AppRole ownRoleInTransaction;

    private String initialMessage;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    private BigDecimal totalPaid;

    private BigDecimal totalConcerned;

    private TimeslotDto timeslot;

    private Boolean cancelled;

    private AppRole cancelByRole;

    private CancelReason cancelReason;

    private String cancelDescription;

    private Boolean cancelNotified;

    private ReviewDto reviewRenter;

    private ReviewDto reviewLocation;
}
