import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { Pageable, ReputationSummary } from 'src/app/dtos';
import { AuthService, RatingsService, ToastService } from 'src/app/services';
import { Role } from 'src/app/enum';

@Component({
  selector: 'app-ratings-view',
  templateUrl: './ratings-view.component.html',
  styleUrls: ['./ratings-view.component.scss'],
})
export class RatingsViewComponent implements OnInit {
  result: ReputationSummary[] = [];
  which = 'LENDER';
  searchString = '';
  orderColumn = 'SUBJECT_NAME';
  orderDirection = 'ASCENDING';
  page = 0;
  pageSize = 5;
  totalResults = 0;

  constructor(
    private authService: AuthService,
    private router: Router,
    private ratingsService: RatingsService,
    private toastSerivce: ToastService
  ) {}

  ngOnInit() {
    if (!this.authService.getUserRole().includes(Role.ROLE_ADMIN)) {
      this.router.navigate(['/']);
    } else {
      this.refreshListings();
    }
  }

  onPageChange(event: [number, number]) {
    this.pageSize = event[1];
    this.page = event[0] - 1;
    this.refreshListings();
  }

  applyTimeDecay(id: number) {
    console.log('Time decay on', id);
    let observable: Observable<void>;
    switch (this.which) {
      case 'LENDER':
        observable = this.ratingsService.updateTimeDecayLender(id);
        break;
      case 'LOCATION':
        observable = this.ratingsService.updateTimeDecayLocation(id);
        break;
      case 'RENTER':
        observable = this.ratingsService.updateTimeDecayRenter(id);
        break;
      default:
        console.error('Unknown subject mode');
        return;
    }
    observable.subscribe({
      next: r => {
        console.log(r);
        this.refreshListings();
      },
      error: e => {
        console.error(e);
        this.toastSerivce.showErrorResponse(e);
        this.router.navigate(['']);
      },
    });
  }

  refreshListings() {
    let observable: Observable<Pageable<ReputationSummary>>;
    switch (this.which) {
      default:
      case 'LENDER':
        observable = this.ratingsService.searchLenders(
          this.searchString,
          this.page,
          this.pageSize,
          this.orderColumn,
          this.orderDirection
        );
        break;
      case 'LOCATION':
        observable = this.ratingsService.searchLocations(
          this.searchString,
          this.page,
          this.pageSize,
          this.orderColumn,
          this.orderDirection
        );
        break;
      case 'RENTER':
        observable = this.ratingsService.searchRenters(
          this.searchString,
          this.page,
          this.pageSize,
          this.orderColumn,
          this.orderDirection
        );
        break;
    }
    observable.subscribe({
      next: r => {
        console.log(r);
        this.result = r.result;
        this.totalResults = r.totalResults;
      },
      error: e => {
        console.log(e);
        this.toastSerivce.showErrorResponse(e);
        this.router.navigate(['']);
      },
    });
  }
}
