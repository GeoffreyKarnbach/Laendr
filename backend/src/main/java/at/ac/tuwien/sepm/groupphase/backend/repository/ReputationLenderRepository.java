package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.ReputationLender;
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

public interface ReputationLenderRepository extends ReputationRepository<ReputationLender> {

    /**
     * Finds the reputation mapping for a lender with the given ID, if it exists.
     *
     * @param lenderId ID of the lender
     * @return Optional with the lenders reputation present if it exists, empty otherwise
     */
    @Query("SELECT r FROM ReputationLender r WHERE r.lender.id = :lenderId")
    Optional<ReputationLender> findByLenderId(@Param("lenderId") Long lenderId);

    /**
     * Finds all lender reputations that were last updated before the given date.
     *
     * @param date the cutoff date
     * @return a stream of all matching lender reputations
     */
    @Query("SELECT r FROM ReputationLender r WHERE r.updatedAt < :date")
    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "50"))
    Stream<ReputationLender> findLastUpdatedBeforeDateStream(@Param("date") LocalDateTime date);

    /**
     * Looks up summaries of reputations for all lenders whose name contains the given search string.
     *
     * @param search string to find in lender name
     * @param pageable page request
     * @return requested page of summaries
     */
    @Query("SELECT r.lender.owner.id AS subjectId, r.lender.owner.name AS subject, r.karma AS karma, "
        + "r.averageRating AS averageRating, r.ratings AS ratings, COALESCE(r.updatedAt, r.createdAt) AS lastChange "
        + "FROM ReputationLender r "
        + "WHERE UPPER(r.lender.owner.name) LIKE CONCAT('%', UPPER(:search), '%')")
    Page<ReputationSummary> summarizeAllByLenderNameContaining(@Param("search") String search, Pageable pageable);

}
