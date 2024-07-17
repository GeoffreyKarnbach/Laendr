package at.ac.tuwien.sepm.groupphase.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReputationDetailDto {

    LocalDateTime calculationTime;
    private List<ReputationDetailReviewDto> reviews;

}
