package at.ac.tuwien.sepm.groupphase.backend.service.impl.reputationdiscount;

import at.ac.tuwien.sepm.groupphase.backend.dto.ReputationDiscountDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Review;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Function;

@RequiredArgsConstructor
public class DiscountRevieweeAge<T, U extends Review, V> extends AbstractReputationDiscount<T, U, V> {

    private final double base;
    private final Function<T, LocalDateTime> subjectCreationGetter;

    @Override
    public BigDecimal calculateDiscount(T subject, U review, V reviewer) {
        return DiscountUtil.inverseExponentialDaysDiscount(base, subjectCreationGetter.apply(subject), review.getCreatedAt());
    }

    @Override
    public ReputationDiscountDto documentDiscount(T subject, U review, V reviewer) {
        return ReputationDiscountDto.builder()
            .name(this.getClass().getSimpleName())
            .discount(calculateDiscount(subject, review, reviewer))
            .parameters("base=%f, deltaT[d]=%f".formatted(
                base,
                DiscountUtil.timeDeltaDays(subjectCreationGetter.apply(subject), review.getCreatedAt())
            ))
            .build();
    }
}
