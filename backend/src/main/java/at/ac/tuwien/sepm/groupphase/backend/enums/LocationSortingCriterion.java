package at.ac.tuwien.sepm.groupphase.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Sort;

@AllArgsConstructor
@Getter
public enum LocationSortingCriterion {
    RECOMMENDED_DESC("reputation.karma", Sort.Direction.DESC),
    CREATION_DATE_DESC("createdAt", Sort.Direction.DESC),
    CREATION_DATE_ASC("createdAt", Sort.Direction.ASC),
    RATING_DESC("reputation.averageRating", Sort.Direction.DESC),
    RATING_ASC("reputation.averageRating", Sort.Direction.ASC),
    SIZE_DESC("sizeInM2", Sort.Direction.DESC),
    SIZE_ASC("sizeInM2", Sort.Direction.ASC),
    AMOUNT_OF_RATINGS_DESC("reputation.ratings", Sort.Direction.DESC),
    AMOUNT_OF_RATINGS_ASC("reputation.ratings", Sort.Direction.ASC),
    // Those below will be handled manually because of the relational complexity
    MIN_PRICE_DESC("price.min", Sort.Direction.DESC),
    MIN_PRICE_ASC("price.min", Sort.Direction.ASC),
    MAX_PRICE_DESC("price.max", Sort.Direction.DESC),
    MAX_PRICE_ASC("price.max", Sort.Direction.ASC),
    AVG_PRICE_DESC("price.avg", Sort.Direction.DESC),
    AVG_PRICE_ASC("price.avg", Sort.Direction.ASC),
    ;

    private String attribute;
    private Sort.Direction direction;
}
