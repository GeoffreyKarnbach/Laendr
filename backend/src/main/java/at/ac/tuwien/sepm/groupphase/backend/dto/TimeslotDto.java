package at.ac.tuwien.sepm.groupphase.backend.dto;

import jakarta.validation.constraints.NotNull;
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
public class TimeslotDto {

    private Long id;

    @NotNull(message = "Startzeitpunkt muss angegeben werden")
    private LocalDateTime start;

    @NotNull(message = "Endzeitpunkt muss angegeben werden")
    private LocalDateTime end;

    @NotNull(message = "Preis muss angegeben werden")
    private BigDecimal price;

    private BigDecimal priceHourly;

    private Boolean isUsed;

    private Boolean isRequested;

    private Long locationId;

    private Boolean isRequestedByCallingUser;
}
