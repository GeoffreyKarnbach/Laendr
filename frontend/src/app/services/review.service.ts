import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Globals } from '../global';
import { Pageable, Review, ReviewCreate } from '../dtos';
import { Observable } from 'rxjs';
import { ReviewCount } from '../dtos/review-count';

@Injectable({
  providedIn: 'root',
})
export class ReviewService {
  private reviewBaseUri = this.globals.backendUri + '/reviews';

  constructor(private httpClient: HttpClient, private globals: Globals) {}

  /**
   * Gets all reviews for a location as a pageable. The paging can be configured with the page and pageSize params.
   *
   * @param id        The id of the location
   * @param page      The page to get
   * @param pageSize  The size of each page
   * @returns         The reviews in a pageable as observable
   */
  loadLocationReviewsPages(id: number, page: number, pageSize: number): Observable<Pageable<Review>> {
    console.log('load reviews of location with id:' + id + ' page:' + page + ' pageSize:' + pageSize);
    return this.httpClient.get<Pageable<Review>>(this.reviewBaseUri + '/search', {
      params: { id, page, pageSize },
    });
  }

  /**
   * Create a review for a location.
   *
   * @param reviewCreate id of associated transaction, rating and optional comment
   * @returns created review
   */
  createLocationReview(reviewCreate: ReviewCreate): Observable<Review> {
    return this.httpClient.post<Review>(this.reviewBaseUri + '/location', reviewCreate);
  }

  /**
   * Create a review for a renter.
   *
   * @param reviewCreate id of associated transaction, rating and optional comment
   * @returns created review
   */
  createRenterReview(reviewCreate: ReviewCreate): Observable<Review> {
    return this.httpClient.post<Review>(this.reviewBaseUri + '/renter', reviewCreate);
  }

  /**
   * Get amount of reviews and transactions for the user
   *
   * @returns a dto with both review count and completed transactions count
   */
  getReviewCount() {
    console.log(`get review count`);
    return this.httpClient.get<ReviewCount>(this.reviewBaseUri + `/count`);
  }
}
