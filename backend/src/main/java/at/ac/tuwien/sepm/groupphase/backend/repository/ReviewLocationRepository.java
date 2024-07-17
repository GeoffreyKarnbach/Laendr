package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.ReviewAverage;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReviewLocation;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface ReviewLocationRepository extends ReviewRepository<ReviewLocation> {

    /**
     * Finds all reviews pertaining to a location with the given ID.
     * <i>Also fetches the location at review.reputation.location</i>
     *
     * @param locationId ID of the location
     * @return a stream of all matching reviews
     */
    @Query("SELECT r FROM ReviewLocation r JOIN FETCH r.reputation.location JOIN FETCH r.reviewer "
        + "WHERE r.reputation.location.id = :locationId ORDER BY r.createdAt DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "50"))
    Stream<ReviewLocation> findAllByLocationIdStream(@Param("locationId") Long locationId);

    /**
     * Finds all reviews pertaining to a location with the given ID.
     * <i>Also fetches the location at review.reputation.location</i>
     *
     * @param locationId ID of the location
     * @param pageable Page request to limit work for database
     * @return a page with reviews for the location
     */
    @Query("SELECT r FROM ReviewLocation r JOIN r.reputation.location JOIN r.reviewer "
        + "WHERE r.reputation.location.id = :locationId ORDER BY r.createdAt DESC")
    Page<ReviewLocation> findAllByLocationIdPage(@Param("locationId") long locationId, Pageable pageable);

    /**
     * Returns the review average for a location with the given ID.
     * <b>Warning</b>: since this query uses database aggregate functions, it will return
     * values even if the given location ID has no reviews. It is the callers responsibility
     * to detect and handle such a case.
     *
     * @param locationId ID of the location
     * @return the average and total contained in ReviewAverage
     */
    @Query("SELECT AVG(r.rating) AS average, COUNT(*) as count FROM ReviewLocation r WHERE r.reputation.location.id = :locationId")
    ReviewAverage calculateAverageForLocationId(@Param("locationId") Long locationId);

    /**
     * Finds all reviews pertaining to a location where the owner has the given ID.
     * <i>Also fetches the location at review.reputation.location</i>
     *
     * @param lenderId ID of the lender
     * @return a stream of all matching reviews
     */
    @Query("SELECT r FROM ReviewLocation r JOIN FETCH r.reputation.location JOIN FETCH r.reviewer "
        + "WHERE r.reputation.location.owner.id = :lenderId ORDER BY r.createdAt DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "50"))
    Stream<ReviewLocation> findAllByLenderIdStream(@Param("lenderId") Long lenderId);

    /**
     * Count all reviews written for a location by the given user.
     *
     * @param email of the given user
     * @return amount of written reviews
     */
    @Query("SELECT COUNT(r) FROM ReviewLocation r JOIN ApplicationUser a ON r.reviewer.id = a.id "
        + "WHERE a.email = :email")
    long countAllByReviewerEmail(@Param("email") String email);

    /**
     * Returns the review average for a location where the owner has the given ID.
     * <b>Warning</b>: since this query uses database aggregate functions, it will return
     * values even if no locations with the given owner exist. It is the callers responsibility
     * to detect and handle such a case.
     *
     * @param lenderId ID of the lender
     * @return the average and total contained in ReviewAverage
     */
    @Query("SELECT AVG(r.rating) AS average, COUNT(*) as count FROM ReviewLocation r WHERE r.reputation.location.owner.id = :lenderId")
    ReviewAverage calculateAverageForLenderId(@Param("lenderId") Long lenderId);

}
