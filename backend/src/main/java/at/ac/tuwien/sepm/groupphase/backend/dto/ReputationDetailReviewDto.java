package at.ac.tuwien.sepm.groupphase.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReputationDetailReviewDto {

    private Long reviewerId;
    private LocalDateTime reviewDate;
    private BigDecimal positiveBaseWeight;
    private BigDecimal negativeBaseWeight;
    private List<ReputationDiscountDto> discounts;
    private BigDecimal timeDecay;

}
