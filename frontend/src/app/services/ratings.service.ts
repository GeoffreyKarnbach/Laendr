import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Globals } from 'src/app/global';
import { Observable } from 'rxjs';
import { Pageable, ReputationDetailDto, ReputationSummary } from '../dtos';

@Injectable({
  providedIn: 'root',
})
export class RatingsService {
  private ratingsBaseUri: string = this.globals.backendUri + '/ratings';

  constructor(private httpClient: HttpClient, private globals: Globals) {}

  /**
   * Searches for reputation summaries of lenders whose name contains the given
   * query.
   * The result is given as a page of all results with the given page parameters
   * and ordering.
   *
   * @param q The query string
   * @param page The page to request (starts at 0)
   * @param pageSize The size of the requested page
   * @param sortColumn The column to sort by
   * @param sortDirection The sorting direction
   * @returns A wrapper for the found reputation summaries with page information
   */
  searchLenders(
    q: string,
    page: number,
    pageSize: number,
    sortColumn: string,
    sortDirection: string
  ): Observable<Pageable<ReputationSummary>> {
    return this.httpClient.get<Pageable<ReputationSummary>>(this.ratingsBaseUri + '/summary/lenders', {
      params: { q, page, pageSize, sortColumn, sortDirection },
    });
  }

  /**
   * Searches for location summaries of lenders whose name contains the given
   * query.
   * The result is given as a page of all results with the given page parameters
   * and ordering.
   *
   * @param q The query string
   * @param page The page to request (starts at 0)
   * @param pageSize The size of the requested page
   * @param sortColumn The column to sort by
   * @param sortDirection The sorting direction
   * @returns A wrapper for the found reputation summaries with page information
   */
  searchLocations(
    q: string,
    page: number,
    pageSize: number,
    sortColumn: string,
    sortDirection: string
  ): Observable<Pageable<ReputationSummary>> {
    return this.httpClient.get<Pageable<ReputationSummary>>(this.ratingsBaseUri + '/summary/locations', {
      params: { q, page, pageSize, sortColumn, sortDirection },
    });
  }

  /**
   * Searches for reputation summaries of renters whose name contains the given
   * query.
   * The result is given as a page of all results with the given page parameters
   * and ordering.
   *
   * @param q The query string
   * @param page The page to request (starts at 0)
   * @param pageSize The size of the requested page
   * @param sortColumn The column to sort by
   * @param sortDirection The sorting direction
   * @returns A wrapper for the found reputation summaries with page information
   */
  searchRenters(
    q: string,
    page: number,
    pageSize: number,
    sortColumn: string,
    sortDirection: string
  ): Observable<Pageable<ReputationSummary>> {
    return this.httpClient.get<Pageable<ReputationSummary>>(this.ratingsBaseUri + '/summary/renters', {
      params: { q, page, pageSize, sortColumn, sortDirection },
    });
  }

  /**
   * Applies time decay to the given lender reputation immediately.
   *
   * @param id ID of the lender to apply time decay to
   * @returns Observable with no content
   */
  updateTimeDecayLender(id: number): Observable<void> {
    return this.httpClient.patch<void>(this.ratingsBaseUri + '/lender/' + id + '/timedecay', {});
  }

  /**
   * Applies time decay to the given location reputation immediately.
   *
   * @param id ID of the location to apply time decay to
   * @returns Observable with no content
   */
  updateTimeDecayLocation(id: number): Observable<void> {
    return this.httpClient.patch<void>(this.ratingsBaseUri + '/location/' + id + '/timedecay', {});
  }

  /**
   * Applies time decay to the given renter reputation immediately.
   *
   * @param id ID of the renter to apply time decay to
   * @returns Observable with no content
   */
  updateTimeDecayRenter(id: number): Observable<void> {
    return this.httpClient.patch<void>(this.ratingsBaseUri + '/renter/' + id + '/timedecay', {});
  }

  /**
   * Queries details about the reputation calculation process for a given subject.
   *
   * @param id ID of the subject
   * @param kind kind of the subject (lender, location or renter)
   * @returns reputation calculation details about the subject
   */
  getDetails(id: number, kind: 'lender' | 'location' | 'renter'): Observable<ReputationDetailDto> {
    return this.httpClient.get<ReputationDetailDto>(this.ratingsBaseUri + '/details/' + kind + '/' + id, {});
  }

  /**
   * Maps the average rating in the range [0, 4] to between 1 and 5 stars
   *
   * @param averageRatings the rating average
   * @returns number of stars corresponding to averageRatings (unit: half stars)
   */
  averageRatingsToStars(averageRatings: number): number {
    return Math.floor((averageRatings + 0.25) * 2) + 2;
  }
}
