package at.ac.tuwien.sepm.groupphase.backend.dto;

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
public class ReputationSummaryDto {

    private Long subjectId;

    private String subject;

    private BigDecimal karma;

    private BigDecimal averageRating;

    private int ratings;

    private LocalDateTime lastChange;

}
