import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Review } from 'src/app/dtos';
import { ToastService, ReviewService } from 'src/app/services';

@Component({
  selector: 'app-review-view-location',
  templateUrl: './review-view-location.component.html',
  styleUrls: ['./review-view-location.component.scss'],
})
export class ReviewViewLocationComponent implements OnInit {
  locationId: number;
  page = 0;
  pageSize = 5;
  reviews: Review[] = [];
  totalAmount = 0;

  constructor(
    private route: ActivatedRoute,
    private reviewService: ReviewService,
    private toastSerivce: ToastService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.locationId = params.id;
      this.loadReviews();
    });
  }

  loadReviews() {
    this.reviewService.loadLocationReviewsPages(this.locationId, this.page, this.pageSize).subscribe({
      next: res => {
        console.log(res);
        this.reviews = res.result;
        this.totalAmount = res.totalResults;
      },
      error: e => {
        this.toastSerivce.showErrorResponse(e);
        console.error(e);
      },
    });
  }

  onPageChange(event: [page: number, pageSize: number]) {
    this.pageSize = event[1];
    this.page = event[0] - 1;
    this.loadReviews();
  }
}
