package at.ac.tuwien.sepm.groupphase.backend.service.impl.reputationdiscount;

import at.ac.tuwien.sepm.groupphase.backend.dto.ReputationDiscountDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Review;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Function;

@RequiredArgsConstructor
public class DiscountReviewerAge<T, U extends Review, V> extends AbstractReputationDiscount<T, U, V> {

    private final double base;
    private final Function<V, LocalDateTime> reviewerCreationGetter;

    @Override
    public BigDecimal calculateDiscount(T subject, U review, V reviewer) {
        return DiscountUtil.inverseExponentialDaysDiscount(base, reviewerCreationGetter.apply(reviewer), review.getCreatedAt());
    }

    @Override
    public ReputationDiscountDto documentDiscount(T subject, U review, V reviewer) {
        return ReputationDiscountDto.builder()
            .name(this.getClass().getSimpleName())
            .discount(calculateDiscount(subject, review, reviewer))
            .parameters("base=%f, deltaT[d]=%f".formatted(
                base,
                DiscountUtil.timeDeltaDays(reviewerCreationGetter.apply(reviewer), review.getCreatedAt())
            ))
            .build();
    }
}
