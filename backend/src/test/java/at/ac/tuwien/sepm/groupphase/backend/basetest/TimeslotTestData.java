package at.ac.tuwien.sepm.groupphase.backend.basetest;

import at.ac.tuwien.sepm.groupphase.backend.entity.TimeslotView;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TimeslotTestData {
    default TimeslotView testData() {
        Long ID = 1L;
        LocalDateTime START = LocalDateTime.of(2023,5,10,14,0);
        final LocalDateTime END = LocalDateTime.of(2023,5,10,16,0);
        final BigDecimal PRICE = new BigDecimal(10);
        final BigDecimal PRICE_HOURLY = new BigDecimal(5);
        final Boolean IS_USED = true;
        final Boolean IS_REQUESTED = true;
        final Boolean IS_REQUESTED_BY_CALLING_USER = false;

        return new TimeslotView() {
            @Override
            public Long getId() {
                return ID;
            }

            @Override
            public LocalDateTime getStart() {
                return START;
            }

            @Override
            public LocalDateTime getEnd() {
                return END;
            }

            @Override
            public BigDecimal getPrice() {
                return PRICE;
            }

            @Override
            public BigDecimal getPriceHourly() {
                return PRICE_HOURLY;
            }

            @Override
            public boolean isUsed() {
                return IS_USED;
            }

            @Override
            public boolean isRequested() {
                return IS_REQUESTED;
            }

            @Override
            public boolean isRequestedByCallingUser() {
                return IS_REQUESTED_BY_CALLING_USER;
            }
        };
    }
}
