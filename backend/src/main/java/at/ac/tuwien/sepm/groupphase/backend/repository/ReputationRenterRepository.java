package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.ReputationRenter;
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

public interface ReputationRenterRepository extends ReputationRepository<ReputationRenter> {

    /**
     * Finds the reputation mapping for a renter with the given ID, if it exists.
     *
     * @param renterId ID of the renter
     * @return Optional with the renters reputation present if it exists, empty otherwise
     */
    @Query("SELECT r FROM ReputationRenter r WHERE r.renter.id = :renterId")
    Optional<ReputationRenter> findByRenterId(@Param("renterId") Long renterId);

    /**
     * Finds all renter reputations that were last updated before the given date.
     *
     * @param date the cutoff date
     * @return a stream of all matching renter reputations
     */
    @Query("SELECT r FROM ReputationRenter r WHERE r.updatedAt < :date")
    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "50"))
    Stream<ReputationRenter> findLastUpdatedBeforeDateStream(@Param("date") LocalDateTime date);

    /**
     * Looks up summaries of reputations for all renters whose name contains the given search string.
     *
     * @param search string to find in renter name
     * @param pageable page request
     * @return requested page of summaries
     */
    @Query("SELECT r.renter.owner.id AS subjectId, r.renter.owner.name AS subject, r.karma AS karma, "
        + "r.averageRating AS averageRating, r.ratings AS ratings, COALESCE(r.updatedAt, r.createdAt) AS lastChange "
        + "FROM ReputationRenter r "
        + "WHERE UPPER(r.renter.owner.name) LIKE CONCAT('%', UPPER(:search), '%')")
    Page<ReputationSummary> summarizeAllByRenterNameContaining(@Param("search") String search, Pageable pageable);

}
