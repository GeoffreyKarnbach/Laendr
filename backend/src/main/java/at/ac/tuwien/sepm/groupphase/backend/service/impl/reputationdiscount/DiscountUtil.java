package at.ac.tuwien.sepm.groupphase.backend.service.impl.reputationdiscount;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DiscountUtil {

    public static final BigDecimal DECIMAL_HALF = BigDecimal.ONE.divide(BigDecimal.ONE.add(BigDecimal.ONE));
    public static final BigDecimal DECIMAL_TWO = BigDecimal.ONE.add(BigDecimal.ONE);

    /**
     * Computes the value of 1 - base^exponent.
     *
     * @param base     the base
     * @param exponent the exponent
     * @return 1 - base^exponent
     */
    public static BigDecimal inverseExponentialDiscount(double base, double exponent) {
        return BigDecimal.valueOf(1 - Math.pow(base, exponent));
    }

    public static double timeDeltaDays(LocalDateTime then, LocalDateTime now) {
        return Math.abs(((double) then.until(now, ChronoUnit.HOURS)) / 24.0);
    }

    /**
     * Applies inverseExponentialDiscount with the given base and the time difference in days.
     *
     * @param base base for inverseExponentialDiscount
     * @param then first point in time
     * @param now  second point in time
     * @return discounting factor based on the difference between then and now
     */
    public static BigDecimal inverseExponentialDaysDiscount(double base, LocalDateTime then, LocalDateTime now) {
        return inverseExponentialDiscount(base, timeDeltaDays(then, now));
    }

}
