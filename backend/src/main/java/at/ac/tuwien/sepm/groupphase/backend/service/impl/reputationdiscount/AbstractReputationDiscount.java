package at.ac.tuwien.sepm.groupphase.backend.service.impl.reputationdiscount;

import at.ac.tuwien.sepm.groupphase.backend.dto.ReputationDiscountDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Review;

import java.math.BigDecimal;

public abstract class AbstractReputationDiscount<T, U extends Review, V> {

    public abstract BigDecimal calculateDiscount(T subject, U review, V reviewer);

    public ReputationDiscountDto documentDiscount(T subject, U review, V reviewer) {
        return null;
    }

}
