package at.ac.tuwien.sepm.groupphase.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ReputationSummary {

    // Subject refers to either a lender, location or renter

    Long getSubjectId();

    String getSubject();

    BigDecimal getKarma();

    BigDecimal getAverageRating();

    int getRatings();

    LocalDateTime getLastChange();

}
