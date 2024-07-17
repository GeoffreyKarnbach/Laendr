package at.ac.tuwien.sepm.groupphase.backend.dto;

import at.ac.tuwien.sepm.groupphase.backend.enums.AustriaState;
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
public class LocationDto {

    private Long id;

    private String name;

    private String description;

    private Boolean isRemoved;

    private PlzDto plz;

    private AustriaState state;

    private String address;

    private BigDecimal size;

    private LocalDateTime createdAt;

    private boolean callerIsOwner;

    private LightLenderDto lender;

    private ReputationDto reputation;

    private String primaryImageUrl;

    private List<String> tags;

    private CoordinateDto coord;
}
