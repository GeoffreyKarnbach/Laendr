package at.ac.tuwien.sepm.groupphase.backend.service;

import at.ac.tuwien.sepm.groupphase.backend.dto.PageableDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.TransactionCancelDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.TransactionCreateDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.TransactionDto;
import at.ac.tuwien.sepm.groupphase.backend.enums.AppRole;
import at.ac.tuwien.sepm.groupphase.backend.enums.TransactionStatus;

import at.ac.tuwien.sepm.groupphase.backend.exception.AccessForbiddenException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;


import java.math.BigDecimal;
import java.util.List;


public interface TransactionService {

    /**
     * Start a transaction for a given timeslot with an initial message by the given user.
     *
     * @param transaction       Dto containing the id of the timeslot and the initial message
     * @param user              The email of the user (renter) performing the transaction
     * @return  The transaction wrapped to a dto.
     */
    TransactionDto startTransaction(TransactionCreateDto transaction, String user);

    /**
     * Finds one transaction by the given id insofar the user is involved in the transaction.
     *
     * @param id transaction id
     * @return mapped dto
     * @throws NotFoundException        if the transaction id is not valid
     * @throws AccessForbiddenException if the user is not involved in the transaction
     */
    TransactionDto findOne(Long id);

    /**
     * Marks the cancelNotified flag of the given transaction as true insofar as the user is involved
     * in the transaction and the transaction is cancelled.
     *
     * @param id transaction id
     * @throws NotFoundException        if the transaction id is not valid
     * @throws AccessForbiddenException if the user is not involved in the transaction
     */
    void recordCancelNotified(Long id);

    /**
     * Finds a page of all transactions that have currently the given status for the calling user in the given role.
     *
     * @param status   status to find
     * @param role     role for which to find
     * @param page     requested page
     * @param pageSize requested page size
     * @return dto encapsulating the found transactions with pagination information
     */
    PageableDto<TransactionDto> findAllByStatusForRole(TransactionStatus status, AppRole role, int page, int pageSize);

    /**
     * Finds the ids of all cancelled transactions that were cancelled by the transaction partner and for which
     * the calling user has not been notified yet of the cancellation.
     *
     * @param role role for which to find
     * @return found ids
     */
    List<Long> getIdsOutstandingCancelNotificationsForRole(AppRole role);

    /**
     * Counts all the completed transactions that have not yet been reviewed for the calling user and role.
     *
     * @param role role for which to count
     * @return count of transactions
     */
    int countAllNotReviewedForRole(AppRole role);

    /**
     * Marks the transaction as completed and saves the amount paid.
     *
     * @param id transaction id
     * @param amountPaid amount paid
     */
    void completeTransaction(Long id, BigDecimal amountPaid);

    /**
     * Records the cancellation of a transaction by one of the transaction partner.
     *
     * @param transactionCancelDto dto containing the transaction id, the cancel message and the cancel reason
     * @throws NotFoundException        if the transaction id is not valid
     * @throws AccessForbiddenException if the user is not involved in the transaction
     * @throws ValidationException if the transactionCancelDto is not valid
     */
    void recordTransactionCancelation(TransactionCancelDto transactionCancelDto);

    /**
     * Finds all ongoing transactions associated with a given timeslot.
     *
     * @param timeslotId id of the timeslot for which transactions should be found
     * @param page       requested page
     * @param pageSize   requested page size
     * @return dto encapsulating the found transactions with pagination information
     * @throws NotFoundException if timeslot for the given id does not exist
     * @throws ConflictException if calling user is not owner of the location associated with the timeslot
     */
    PageableDto<TransactionDto> findAllTransactionsForTimeslot(Long timeslotId, int page, int pageSize);

    /**
     * Cancel all transactions for a timeslot where the calling user is the renter in the transaction.
     *
     * @param timeslotId timeslot for which transactions should be cancelled
     * @throws ConflictException if the given timeslot does not exist
     * @throws AccessForbiddenException if the user is authenticated
     */
    void cancelTransactionsForTimeslotAsRenter(Long timeslotId);

    /**
     * Accepts the transaction with the given id.
     * Cancels all other transactions for the timeslot.
     *
     * @param id id of the transaction to accept
     * @throws AccessForbiddenException if the user is not the location owner
     */
    void acceptTransaction(Long id);

    /**
     * Cancels all active timeslots in which a given renter is involved.
     *
     * @param email The email of the renter
     */
    void cancelAllActiveTransactionsForRenter(String email);
}
