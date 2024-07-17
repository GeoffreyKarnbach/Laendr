package at.ac.tuwien.sepm.groupphase.backend.service.impl.reputationdiscount;

import at.ac.tuwien.sepm.groupphase.backend.dto.ReputationDiscountDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Review;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
public class DiscountSameReviewer<T, U extends Review, V> extends AbstractReputationDiscount<T, U, V> {

    private final Map<Long, LocalDateTime> lastFromReviewer = new HashMap<>();
    private final double base;
    private final Function<V, Long> reviewerIdGetter;

    @Override
    public BigDecimal calculateDiscount(T subject, U review, V reviewer) {
        Long reviewerId = reviewerIdGetter.apply(reviewer);
        BigDecimal result = BigDecimal.ONE;
        if (lastFromReviewer.containsKey(reviewerId)) {
            result = DiscountUtil.inverseExponentialDaysDiscount(base, lastFromReviewer.get(reviewerId), review.getCreatedAt());
        }
        lastFromReviewer.put(reviewerId, review.getCreatedAt());
        return result;
    }

    @Override
    public ReputationDiscountDto documentDiscount(T subject, U review, V reviewer) {
        double deltaT = 0;
        if (lastFromReviewer.containsKey(reviewerIdGetter.apply(reviewer))) {
            deltaT = DiscountUtil.timeDeltaDays(lastFromReviewer.get(reviewerIdGetter.apply(reviewer)), review.getCreatedAt());
        }
        var discount = calculateDiscount(subject, review, reviewer);
        if (discount.equals(BigDecimal.ONE)) {
            return null;
        } else {
            return ReputationDiscountDto.builder()
                .name(this.getClass().getSimpleName())
                .discount(discount)
                .parameters("deltaT[d]=%s".formatted(deltaT))
                .build();
        }
    }
}
