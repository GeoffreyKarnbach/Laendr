package at.ac.tuwien.sepm.groupphase.backend.dto;

import at.ac.tuwien.sepm.groupphase.backend.enums.AustriaState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationCreationDto {

    private String name;

    private String description;

    private Boolean isRemoved;

    private PlzDto plz;

    private AustriaState state;

    private String address;

    private BigDecimal size;

    private List<String> tags;

    private CoordinateDto coord;
}
