import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-ratings-display',
  templateUrl: './ratings-display.component.html',
  styleUrls: ['./ratings-display.component.scss'],
})
export class RatingsDisplayComponent {
  @Input() stars = -1; // unit: half-stars; 0: no stars, 10: 5 stars, negative: no reviews
  @Input() count = 0;
  @Input() small = false;
  @Input() showCount = true;
  @Input() textClass = '';

  iconClassForIndex(index: number, stars: number): string {
    if (stars < 0) {
      return 'bi-star opacity-25';
    }

    if (index + 1 <= stars) {
      return 'bi-star-fill';
    } else if (index <= stars) {
      return 'bi-star-half';
    } else {
      return 'bi-star';
    }
  }
}
