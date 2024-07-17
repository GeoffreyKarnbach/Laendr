package at.ac.tuwien.sepm.groupphase.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationFilterDistanceDto {

    private BigDecimal distance;
    private CoordinateDto coord;

}
