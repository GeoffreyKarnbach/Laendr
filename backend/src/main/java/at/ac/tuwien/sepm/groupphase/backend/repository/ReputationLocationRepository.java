package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.ReputationLocation;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReputationSummary;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

public interface ReputationLocationRepository extends ReputationRepository<ReputationLocation> {

    /**
     * Finds the reputation mapping for a location with the given ID, if it exists.
     *
     * @param locationId ID of the location
     * @return Optional with the locations reputation present if it exists, empty otherwise
     */
    @Query("SELECT r FROM ReputationLocation r WHERE r.location.id = :locationId")
    Optional<ReputationLocation> findByLocationId(@Param("locationId") Long locationId);

    /**
     * Finds all location reputations that were last updated before the given date.
     *
     * @param date the cutoff date
     * @return a stream of all matching location reputations
     */
    @Query("SELECT r FROM ReputationLocation r WHERE r.updatedAt < :date")
    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "50"))
    Stream<ReputationLocation> findLastUpdatedBeforeDateStream(@Param("date") LocalDateTime date);

    /**
     * Looks up summaries of reputations for all locations whose name contains the given search string.
     *
     * @param search string to find in location name
     * @param pageable page request
     * @return requested page of summaries
     */
    @Query("SELECT r.location.id AS subjectId, r.location.name AS subject, r.karma AS karma, "
        + "r.averageRating AS averageRating, r.ratings AS ratings, COALESCE(r.updatedAt, r.createdAt) AS lastChange "
        + "FROM ReputationLocation r "
        + "WHERE UPPER(r.location.name) LIKE CONCAT('%', UPPER(:search), '%')")
    Page<ReputationSummary> summarizeAllByLocationNameContaining(@Param("search") String search, Pageable pageable);

}
