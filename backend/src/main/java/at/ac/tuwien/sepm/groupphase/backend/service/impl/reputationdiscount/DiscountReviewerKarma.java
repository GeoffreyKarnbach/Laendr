package at.ac.tuwien.sepm.groupphase.backend.service.impl.reputationdiscount;

import at.ac.tuwien.sepm.groupphase.backend.dto.ReputationDiscountDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Review;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class DiscountReviewerKarma<T, U extends Review, V> extends AbstractReputationDiscount<T, U, V> {

    private final double base;

    @Override
    public BigDecimal calculateDiscount(T subject, U review, V reviewer) {
        return DiscountUtil.inverseExponentialDiscount(base, review.getReviewerKarmaAtReview().doubleValue());
    }

    @Override
    public ReputationDiscountDto documentDiscount(T subject, U review, V reviewer) {
        return ReputationDiscountDto.builder()
            .name(this.getClass().getSimpleName())
            .discount(calculateDiscount(subject, review, reviewer))
            .parameters("karma=%s".formatted(review.getReviewerKarmaAtReview()))
            .build();
    }
}
