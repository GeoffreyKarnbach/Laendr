import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Globals } from 'src/app/global';
import { Pageable, Transaction, TransactionCreate, TransactionCancel } from 'src/app/dtos';
import { Role, TransactionStatus } from 'src/app/enum';

@Injectable({
  providedIn: 'root',
})
export class TransactionService {
  private transactionBaseUri = this.globals.backendUri + '/transactions';

  constructor(private httpClient: HttpClient, private globals: Globals) {}

  /**
   * Starts a transaction with the values given.
   *
   * @param transaction Dto containing the timeslot id and initial message of the transaction
   * @returns The started transaction
   */
  public startTransaction(transaction: TransactionCreate): Observable<Transaction> {
    return this.httpClient.post<Transaction>(`${this.transactionBaseUri}`, transaction);
  }
  /**
   * Finds the transaction for the given transaction id.
   *
   * Will result in 403 if the calling user is not part of the transaction (or admin).
   *
   * @param id transaction id
   * @returns found transaction
   */
  find(id: number): Observable<Transaction> {
    console.log(`Find transaction with id=${id}`);
    return this.httpClient.get<Transaction>(`${this.transactionBaseUri}/${id}`);
  }

  /**
   * Marks for the given transaction that a user has been informed about the cancellation
   * initiated by the other party.
   *
   * @param id transaction id
   * @returns Observable on the request
   */
  recordCancelNotified(id: number): Observable<void> {
    console.log(`Mark transaction with id=${id} as cancel notified`);
    return this.httpClient.post<void>(`${this.transactionBaseUri}/${id}/cancel-notification`, {});
  }

  /**
   * Gets the current state of a transaction from the current user's POV.
   *
   * @param transaction transaction to get the status of
   * @returns deduced status
   */
  getStatus(transaction: Transaction): TransactionStatus {
    if (transaction.cancelled) {
      return TransactionStatus.CANCELLED;
    }
    if (transaction.completedAt) {
      if (
        (transaction.ownRoleInTransaction === Role.ROLE_LENDER && transaction.reviewRenter) ||
        (transaction.ownRoleInTransaction !== Role.ROLE_LENDER && transaction.reviewLocation)
      ) {
        return TransactionStatus.REVIEWED;
      }
      return TransactionStatus.COMPLETED;
    }
    if (transaction.timeslot.isUsed) {
      return TransactionStatus.ACCEPTED;
    }
    return TransactionStatus.ACTIVE;
  }

  /**
   * Finds a page of all transactions that have currently the given status for the user in the given role.
   *
   * @param status status to find
   * @param role role for which to find
   * @param page requested page
   * @param pageSize requested page size
   * @returns dto encapsulating the found transactions with pagination information
   */
  getByStatusForRole(
    status: TransactionStatus,
    role: Role,
    page: number,
    pageSize: number
  ): Observable<Pageable<Transaction>> {
    console.log(`Search for transactions with status=${status} and role=${role}`);
    return this.httpClient.get<Pageable<Transaction>>(`${this.transactionBaseUri}/all/${status}`, {
      params: { role, page, pageSize },
    });
  }

  /**
   * Finds the ids of all cancelled transactions that were cancelled by the transaction partner and for which
   * the user has not been notified yet of the cancellation.
   *
   * @param role role for which to find
   * @returns found ids
   */
  getIdsForOutstandingCancelNotificationsForRole(role: Role): Observable<number[]> {
    console.log(`Get ids for the not notified cancelled transactions for role=${role}`);
    return this.httpClient.get<number[]>(`${this.transactionBaseUri}/all/cancel-notifications`, { params: { role } });
  }

  /**
   * Counts all the completed transactions that have not yet been reviewed for the user and role.
   *
   * @param role role for which to count
   * @returns count of transactions
   */
  countAllNotReviewedForRole(role: Role): Observable<number> {
    console.log(`Count transactions that are not reviewed yet for role=${role}`);
    return this.httpClient.get<number>(`${this.transactionBaseUri}/all/not-reviewed`, { params: { role } });
  }

  /**
   * Records that the transaction has been completed with the given price (to be triggered by the lender)
   *
   * @param price price paid by the renter for the transaction
   * @returns Observable on the request
   */
  recordTransactionComplete(id: number, price: number): Observable<any> {
    console.log(`Record transaction completion with price=${price}`);
    return this.httpClient.post<any>(`${this.transactionBaseUri}/${id}/complete?price=${price}`, {});
  }

  /**
   * Cancels the transaction with the reason and message given.
   *
   * @param transactionCancel Dto containing the transaction id, reason and message
   * @returns Observable on the request
   */
  recordCancelTransaction(transactionCancel: TransactionCancel): Observable<void> {
    console.log(`Cancel transaction with id=${transactionCancel.transactionId}`);
    return this.httpClient.post<void>(
      `${this.transactionBaseUri}/${transactionCancel.transactionId}/cancel`,
      transactionCancel
    );
  }

  /**
   * Accepts the transaction with the given id and cancels all other transactions for the same timeslot.
   *
   * @param id transaction id
   * @returns Observable on the request
   */
  recordAcceptTransaction(id: number): Observable<void> {
    console.log(`Accept transaction with id=${id}`);
    return this.httpClient.post<void>(`${this.transactionBaseUri}/${id}/accept`, {});
  }

  /**
   * Finds all ongoing transactions for a timeslot.
   *
   * @param timeslotId timeslot for which transaction should be found
   * @param page requested page
   * @param pageSize requested page size
   * @returns dto encapsulating the found transactions with pagination information
   */
  findOngoingTransactionForTimeslot(
    timeslotId: number,
    page: number,
    pageSize: number
  ): Observable<Pageable<Transaction>> {
    return this.httpClient.get<Pageable<Transaction>>(`${this.transactionBaseUri}/timeslot/${timeslotId}`, {
      params: { page, pageSize },
    });
  }

  /**
   * Cancel all transactions for a timeslot where the calling user is the renter in the transaction.
   *
   * @param timeslotId timeslot for which transactions should be cancelled
   */
  cancelTransactionsForTimeslotAsRenter(timeslotId: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.transactionBaseUri}/timeslot/${timeslotId}`);
  }
}
