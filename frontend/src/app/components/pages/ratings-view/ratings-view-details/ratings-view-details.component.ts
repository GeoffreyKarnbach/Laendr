import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { of, switchMap } from 'rxjs';
import { ReputationDetailDto } from 'src/app/dtos';
import { RatingsService, ToastService } from 'src/app/services';

export enum RatingsViewDetailsModes {
  LENDER = 'lender',
  LOCATION = 'location',
  RENTER = 'renter',
}

@Component({
  selector: 'app-ratings-view-details',
  templateUrl: './ratings-view-details.component.html',
  styleUrls: ['./ratings-view-details.component.scss'],
})
export class RatingsViewDetailsComponent implements OnInit {
  mode: RatingsViewDetailsModes;
  details?: ReputationDetailDto = null;
  subjectId: number;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private ratingsService: RatingsService,
    private toastSerivce: ToastService
  ) {}

  ngOnInit() {
    this.route.data
      .pipe(
        switchMap(data => {
          return this.route.params.pipe(
            switchMap(params => {
              const id = Number.parseInt(params.id, 10);
              if (Number.isNaN(id)) {
                throw new Error('Invalid ID');
              }
              return of([data.mode, id]);
            })
          );
        })
      )
      .subscribe({
        next: ([mode, id]) => {
          this.mode = mode;
          this.subjectId = id;
          this.updateDetails();
        },
        error: e => {
          console.error(e);
          this.toastSerivce.showErrorResponse(e);
          this.router.navigate(['']);
        },
      });
  }

  updateDetails() {
    this.ratingsService.getDetails(this.subjectId, this.mode).subscribe({
      next: details => {
        console.log(details);
        this.details = details;
      },
      error: e => {
        console.error(e);
        this.toastSerivce.showErrorResponse(e);
        this.router.navigate(['']);
      },
    });
  }

  getReviewerKind() {
    switch (this.mode) {
      case RatingsViewDetailsModes.LENDER:
      case RatingsViewDetailsModes.LOCATION:
        return 'Mieter';
      case RatingsViewDetailsModes.RENTER:
        return 'Vermieter';
      default:
        return 'Reviewer';
    }
  }
}
