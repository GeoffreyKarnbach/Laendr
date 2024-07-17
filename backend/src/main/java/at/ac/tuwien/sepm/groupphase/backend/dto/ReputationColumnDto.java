package at.ac.tuwien.sepm.groupphase.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReputationColumnDto {
    SUBJECT_NAME("subject"),
    KARMA("karma"),
    AVERAGE_RATING("averageRating"),
    RATINGS("ratings"),
    LAST_CHANGE("lastChange");

    private String attribute;
}
