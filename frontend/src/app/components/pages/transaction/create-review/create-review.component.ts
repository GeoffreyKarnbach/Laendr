import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Review } from 'src/app/dtos';

@Component({
  selector: 'app-create-review',
  templateUrl: './create-review.component.html',
  styleUrls: ['./create-review.component.scss'],
})
export class CreateReviewComponent {
  @Input() transactionId: number;

  @Output() reviewSubmitted = new EventEmitter<Review>();

  review: Review = {
    rating: 2,
    comment: '',
    createdAt: null,
  };

  submit() {
    this.reviewSubmitted.emit(this.review);
  }
}
