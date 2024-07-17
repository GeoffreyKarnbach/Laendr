package at.ac.tuwien.sepm.groupphase.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Sort;

@AllArgsConstructor
@Getter
public enum SortDirectionDto {
    ASCENDING(Sort.Direction.ASC),
    DESCENDING(Sort.Direction.DESC);

    private Sort.Direction direction;
}
