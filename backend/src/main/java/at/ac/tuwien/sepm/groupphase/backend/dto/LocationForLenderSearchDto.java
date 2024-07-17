package at.ac.tuwien.sepm.groupphase.backend.dto;

import at.ac.tuwien.sepm.groupphase.backend.enums.LocationSortingCriterion;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationForLenderSearchDto {
    long id;
    boolean includeRemovedLocations;
    @PositiveOrZero
    int page;
    @PositiveOrZero
    int pageSize;
    @NotNull
    LocationSortingCriterion sort;
}
