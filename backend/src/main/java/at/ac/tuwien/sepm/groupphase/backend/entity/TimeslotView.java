package at.ac.tuwien.sepm.groupphase.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TimeslotView {
    Long getId();

    LocalDateTime getStart();

    LocalDateTime getEnd();

    BigDecimal getPrice();

    BigDecimal getPriceHourly();

    boolean isUsed();

    boolean isRequested();

    boolean isRequestedByCallingUser();
}
