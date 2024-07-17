import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { switchMap, tap } from 'rxjs';
import { LocationDto } from 'src/app/dtos';
import { LenderDto } from 'src/app/dtos/lender';
import { AustriaState, LocationSortingCriterion, sortToDisplayText } from 'src/app/enum';
import { Globals } from 'src/app/global';
import { LocationService, ToastService, RatingsService } from 'src/app/services';
import { LenderService } from 'src/app/services/lender.service';

@Component({
  selector: 'app-lender',
  templateUrl: './lender.component.html',
  styleUrls: ['./lender.component.scss'],
})
export class LenderComponent implements OnInit {
  locations: LocationDto[] = [];

  totalAmount = 0; //for locations
  page = 0; //for locations
  pageSize = 5; //for locations

  includeRemovedLocations = false;
  sort: LocationSortingCriterion = LocationSortingCriterion.CREATION_DATE_DESC;
  readonly enumSort = LocationSortingCriterion;
  readonly enumToDisplaySort = sortToDisplayText;

  //lender will be overriden, values only here for testing purposes
  lender: LenderDto = {
    id: -1,
    name: 'test_name',
    phone: '0123456789',
    email: 'test@email.com',
    description: 'test_description',
    createdAt: null,
    state: AustriaState.W,
    callerIsThisLender: false,
    reputation: {
      ratings: 1,
      averageRating: 5,
    },
  };

  private handleRequestResult = {
    next: res => {
      console.log(res);
      this.totalAmount = res.totalResults;
      this.locations = res.result;
      for (const location of this.locations) {
        if (location.primaryImageUrl) {
          location.primaryImageUrl = this.globals.backendUri + '/images/' + location.primaryImageUrl;
        } else {
          location.primaryImageUrl = '/assets/nopic.jpg';
        }
      }
    },
    error: e => {
      console.log(e);
      this.toastService.showErrorResponse(e);
    },
  };

  constructor(
    public lenderService: LenderService,
    private route: ActivatedRoute,
    private locationService: LocationService,
    private router: Router,
    private toastService: ToastService,
    private ratingsService: RatingsService,
    private globals: Globals
  ) {}

  ngOnInit(): void {
    this.route.params
      .pipe(
        switchMap(params => this.lenderService.getOneById(params.id)),
        tap(lenderDto => (this.lender = lenderDto)),
        switchMap(lender =>
          this.locationService.searchByLender(lender.id, this.includeRemovedLocations, this.page, this.pageSize)
        )
      )
      .subscribe(this.handleRequestResult);
  }

  //Location related methods

  onChangeSort(value: LocationSortingCriterion) {
    this.sort = value;
    this.reloadLocations();
  }

  onPageChange(event: [page: number, pageSize: number]) {
    this.pageSize = event[1];
    this.page = event[0] - 1;
    this.reloadLocations();
  }

  onClickCard(id: number, isRemoved: boolean) {
    if (!isRemoved) {
      this.router.navigate(['/location', id]);
    }
  }

  reloadLocations() {
    this.loadLocations(this.lender.id, this.includeRemovedLocations, this.page, this.pageSize, this.sort);
  }

  private loadLocations(
    id: number,
    includeRemovedLocations: boolean,
    page: number,
    pageSize: number,
    sort: LocationSortingCriterion
  ) {
    this.locationService
      .searchByLender(id, includeRemovedLocations, page, pageSize, sort)
      .subscribe(this.handleRequestResult);
  }
}
