package at.ac.tuwien.sepm.groupphase.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeslotSearchDto {

    @NotNull(message = "LocationId must not be null")
    private Long locationId;

    @NotNull(message = "Day must not be null")
    private LocalDate day;

    private boolean callerIsLocationOwner = false;
}
