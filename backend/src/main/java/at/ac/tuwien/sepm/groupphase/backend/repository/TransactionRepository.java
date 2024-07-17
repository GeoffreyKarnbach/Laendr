package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.Transaction;
import at.ac.tuwien.sepm.groupphase.backend.enums.AppRole;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Finds all transactions where the given user is involved in the given role and that are neither
     * cancelled nor completed.
     *
     * @param userEmail required user
     * @param role      required role
     * @param pageable  pagination information
     * @return found page
     */
    @Query("select t from Transaction t where (t.cancelled is null or t.cancelled = false) and t.completedAt is null and t.timeslot.used = :isAccepted "
        + "and ((:role = 'ROLE_LENDER' and :userEmail = t.timeslot.owningLocation.owner.owner.email) or (:role = 'ROLE_RENTER' and :userEmail = t.renter.owner.email)) "
        + "order by t.createdAt desc")
    Page<Transaction> findAllActiveForUserByRole(@Param("userEmail") String userEmail, @Param("role") String role, @Param("isAccepted") boolean isAccepted, Pageable pageable);

    /**
     * Finds all transactions where the given renter is involved that are neither
     * cancelled nor completed.
     *
     * @param userEmail required user
     * @return found stream
     */
    @Query("select t from Transaction t where (t.cancelled is null or t.cancelled = false) and t.completedAt is null "
        + "and :userEmail = t.renter.owner.email "
        + "order by t.createdAt desc")
    Stream<Transaction> findAllActiveForRenter(@Param("userEmail") String userEmail);

    /**
     * Finds all transactions where the given user is involved in the given role and that are cancelled.
     *
     * @param userEmail required user
     * @param role      required role
     * @param pageable  pagination information
     * @return found page
     */
    @Query("select t from Transaction t where t.cancelled = true "
        + "and ((:role = 'ROLE_LENDER' and :userEmail = t.timeslot.owningLocation.owner.owner.email) or (:role = 'ROLE_RENTER' and :userEmail = t.renter.owner.email)) "
        + "order by t.createdAt desc")
    Page<Transaction> findAllCancelledForUserByRole(@Param("userEmail") String userEmail, @Param("role") String role, Pageable pageable);

    /**
     * Finds all transactions where the given user is involved in the given role and that are completed,
     * but have not yet been given a required review depending on the role.
     *
     * @param userEmail required user
     * @param role      required role
     * @param pageable  pagination information
     * @return found page
     */
    @Query("select t from Transaction t where t.completedAt is not null "
        + "and ((:role = 'ROLE_LENDER' and :userEmail = t.timeslot.owningLocation.owner.owner.email and t.reviewRenter is null) "
        + "or (:role = 'ROLE_RENTER' and :userEmail = t.renter.owner.email and t.reviewLocation is null)) "
        + "order by t.createdAt desc")
    Page<Transaction> findAllCompletedForUserByRole(@Param("userEmail") String userEmail, @Param("role") String role, Pageable pageable);

    /**
     * Finds all transactions where the given user is involved in the given role and that are completed and have been
     * given the required review depending on the role.
     *
     * @param userEmail required user
     * @param role      required role
     * @param pageable  pagination information
     * @return found page
     */
    @Query("select t from Transaction t where t.completedAt is not null "
        + "and ((:role = 'ROLE_LENDER' and :userEmail = t.timeslot.owningLocation.owner.owner.email and t.reviewRenter is not null) "
        + "or (:role = 'ROLE_RENTER' and :userEmail = t.renter.owner.email and t.reviewLocation is not null)) "
        + "order by t.createdAt desc")
    Page<Transaction> findAllReviewedForUserByRole(@Param("userEmail") String userEmail, @Param("role") String role, Pageable pageable);

    /**
     * Finds the ids of all cancelled transactions that were cancelled by the transaction partner and for which
     * the given user has not been notified yet of the cancellation.
     *
     * @param userEmail required user
     * @param roleEnum  required role as enum for field comparison
     * @param role      required role as string for string comparison
     * @return found ids
     */
    @Query("select t.id from Transaction t where t.cancelled = true and t.cancelByRole <> :roleEnum and t.cancelNotified = false and "
        + "((:role = 'ROLE_LENDER' and :userEmail = t.timeslot.owningLocation.owner.owner.email) or (:role = 'ROLE_RENTER' and :userEmail = t.renter.owner.email))")
    List<Long> findAllIdsForCancelledAndNotNotified(@Param("userEmail") String userEmail, @Param("roleEnum") AppRole roleEnum, @Param("role") String role);

    /**
     * Counts all the completed transactions that have not yet been reviewed for the given user and role.
     *
     * @param userEmail required user
     * @param role      required role
     * @return count of transactions
     */
    @Query("select count(t) from Transaction t where t.completedAt is not null "
        + "and ((:role = 'ROLE_LENDER' and :userEmail = t.timeslot.owningLocation.owner.owner.email and t.reviewRenter is null) "
        + "or (:role = 'ROLE_RENTER' and :userEmail = t.renter.owner.email and t.reviewLocation is null))")
    int countAllNotReviewed(@Param("userEmail") String userEmail, @Param("role") String role);

    /**
     * Counts all completed transactions of a given user, both as lender and renter.
     *
     * @param email of the user
     * @return amount of completed transactions
     */
    @Query("SELECT count(t) from Transaction t JOIN ApplicationUser a ON a.id = t.renter.owner.id "
        + "WHERE t.completedAt is not null "
        + "and ((:email = t.timeslot.owningLocation.owner.owner.email)"
        + "or (:email = a.email))")
    long countAllCompletedByEmail(@Param("email") String email);

    /**
     * Finds all ongoing transactions associated with a given timeslot.
     * Ordered descending by the reputation of the renter.
     *
     * @param timeslotId id of the timeslot for which transactions should be found
     * @param pageable   pagination information
     * @return page of found transactions
     */
    @Query("select t from Transaction t where t.timeslot.id = :timeslotId and (t.cancelled is null or t.cancelled = false)"
        + " order by t.renter.reputation.karma desc")
    Page<Transaction> findAllOngoingByTimeslotId(@Param("timeslotId") Long timeslotId, Pageable pageable);

    /**
     * Cancel all transactions for a timeslot where the given user is the renter in the transaction.
     * NativeQuery because update does not work for enum columns with JPQL
     *
     * @param timeslotId timeslot for which transactions should be cancelled
     * @param userEmail  renter for which transactions should be cancelled
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "update TRANSACTION t set t.IS_CANCELLED = true, t.IS_CANCEL_NOTIFIED = false, "
        + "t.CANCEL_BY_ROLE = 'ROLE_RENTER', t.CANCEL_REASON = 'NO_INTEREST' where t.TIMESLOT_ID = :timeslotId "
        + "and t.RENTER_ID in (select u.id from APP_USER u where u.EMAIL = :userEmail)")
    void cancelTransactionsForTimeslotAsRenter(@Param("timeslotId") Long timeslotId, @Param("userEmail") String userEmail);

    /**
     * Returns all transactions for a given timeslot.
     *
     * @param timeslotId id of the timeslot for which transactions should be found
     * @return list of found transactions
     */
    @Query("select t from Transaction t where t.timeslot.id = :timeslotId")
    List<Transaction> findByTimeslotId(@Param("timeslotId") Long timeslotId);
}
