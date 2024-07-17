package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.ReviewAverage;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReviewRenter;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface ReviewRenterRepository extends ReviewRepository<ReviewRenter> {

    /**
     * Finds all reviews for a renter with the given ID.
     * <i>Also fetches the renter at review.reputation.renter</i>
     *
     * @param renterId ID of the renter
     * @return a stream of all matching reviews
     */
    @Query("SELECT r FROM ReviewRenter r JOIN FETCH r.reputation.renter JOIN FETCH r.reviewer "
        + "WHERE r.reputation.renter.id = :renterId ORDER BY r.createdAt DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "50"))
    Stream<ReviewRenter> findAllByRenterIdStream(@Param("renterId") Long renterId);

    /**
     * Returns the review average for a renter with the given ID.
     * <b>Warning</b>: since this query uses database aggregate functions, it will return
     * values even if the given renter ID has no reviews. It is the callers responsibility
     * to detect and handle such a case.
     *
     * @param renterId ID of the renter
     * @return the average and total contained in ReviewAverage
     */
    @Query("SELECT AVG(r.rating) as average, COUNT(*) as count FROM ReviewRenter r WHERE r.reputation.renter.id = :renterId")
    ReviewAverage calculateAverageForRenterId(@Param("renterId") Long renterId);

    @Query("SELECT COUNT(r) FROM ReviewRenter r JOIN ApplicationUser a ON r.reviewer.id = a.id "
        + " WHERE a.email = :email")
    long countAllByReviewerEmail(@Param("email") String email);

}
