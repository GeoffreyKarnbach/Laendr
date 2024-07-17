import { Pipe, PipeTransform } from '@angular/core';
import { RatingsService } from 'src/app/services';

@Pipe({
  name: 'ratingToStars',
})
export class RatingToStarsPipe implements PipeTransform {
  constructor(private ratingsService: RatingsService) {}

  transform(value: number): number {
    return value != null ? this.ratingsService.averageRatingsToStars(value) : -1;
  }
}
