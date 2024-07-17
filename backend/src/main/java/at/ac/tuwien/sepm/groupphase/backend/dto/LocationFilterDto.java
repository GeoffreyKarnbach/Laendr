package at.ac.tuwien.sepm.groupphase.backend.dto;

import at.ac.tuwien.sepm.groupphase.backend.enums.AustriaState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationFilterDto {
    private String searchString;

    private PlzDto plz;

    private AustriaState state;

    private String address;

    private LocalDate timeFrom;

    private LocalDate timeTo;

    private BigDecimal priceFrom;

    private BigDecimal priceTo;

    private LocationFilterDistanceDto position;

    private String[] tags;
}
