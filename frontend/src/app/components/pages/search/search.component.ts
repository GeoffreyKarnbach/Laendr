import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { LocationDto, LocationFilterDto, Pageable, Plz } from 'src/app/dtos';
import { LocationService, ToastService, PlzService, AuthService } from 'src/app/services';
import { Globals } from 'src/app/global';
import { debounceTime, Subject, switchMap } from 'rxjs';
import { NgModel } from '@angular/forms';
import { NgbDate, NgbDateParserFormatter, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { LocationSortingCriterion, sortToDisplayText } from 'src/app/enum';
import { CustomNgbDateParserFormatter, NgbDateUtil } from 'src/app/util';
import { LatLngLiteral } from 'leaflet';

enum SearchMode {
  LIST = 'LIST',
  MAP = 'MAP',
}

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.scss'],
  providers: [{ provide: NgbDateParserFormatter, useClass: CustomNgbDateParserFormatter }],
})
export class SearchComponent implements OnInit {
  locations: LocationDto[] = [];
  filter: LocationFilterDto = {
    searchString: null,
    plz: { plz: '', ort: '' },
    state: null,
    address: null,
    priceFrom: null,
    priceTo: null,
    timeFrom: null,
    timeTo: null,
    position: { coord: null, distance: null },
    tags: [],
  };

  plzInputChange = new Subject<string>();
  plzSuggestions: Plz[] = [];

  timeTo: NgbDateStruct = { year: null, month: null, day: null };
  timeFrom: NgbDateStruct = { year: null, month: null, day: null };

  totalAmount = 0;
  page = 0;
  pageSize = 5;
  filterActive = true;

  sort: LocationSortingCriterion = LocationSortingCriterion.RECOMMENDED_DESC;

  mode: SearchMode = SearchMode.LIST;

  tags: string[];
  selectedTag: string;

  firstFilterCall = true;

  readonly enumSort = LocationSortingCriterion;
  readonly enumToDisplaySort = sortToDisplayText;
  readonly enumMode = SearchMode;

  constructor(
    private locationService: LocationService,
    private route: ActivatedRoute,
    private router: Router,
    private toastService: ToastService,
    private plzService: PlzService,
    private globals: Globals,
    private changeDetectorRef: ChangeDetectorRef,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.parseParams(params);
      if (this.firstFilterCall && !this.filter.position.coord) {
        const coord = this.authService.getUserInfo()?.coordinates;
        this.filter.position.coord = coord?.lat && coord?.lng ? coord : null;
        this.firstFilterCall = false;
      }
      this.filterLocations(this.filter, this.page, this.pageSize);
    });

    this.locationService.getAllTags().subscribe(tags => {
      this.tags = tags.tags;
    });

    this.plzInputChange
      .pipe(
        debounceTime(300),
        switchMap(val => this.plzService.findSuggestions(val))
      )
      .subscribe({
        next: plzs => {
          this.plzSuggestions = plzs;
          if (plzs.length === 1 && !this.filter.position.coord) {
            this.filter.position.coord = plzs[0].coord;
          }
        },
        error: e => {
          this.toastService.showErrorResponse(e);
        },
      });
  }

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      // eslint-disable-next-line @typescript-eslint/naming-convention
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  public onChangeSort(value: LocationSortingCriterion) {
    this.sort = value;
    this.onClickSearch();
  }

  public onTagAdded(): void {
    console.log('tag added');
    if (this.selectedTag) {
      this.tags = this.tags.filter(tag => tag !== this.selectedTag);
      this.filter.tags.push(this.selectedTag);
      this.selectedTag = null;
      this.filter.tags.sort();
      this.filterLocations(this.filter, this.page, this.pageSize);
    }
  }

  deleteTag($event: number) {
    const tagID = $event - 1;

    this.tags.push(this.filter.tags.splice(tagID, 1)[0]);
    this.filter.tags.sort();
    this.filterLocations(this.filter, this.page, this.pageSize);
  }

  toggleFiltering() {
    this.filterActive = !this.filterActive;
    this.filter = {
      searchString: this.filter.searchString,
      plz: { plz: '', ort: '' },
      state: null,
      address: null,
      priceFrom: null,
      priceTo: null,
      timeFrom: null,
      timeTo: null,
      position: { coord: null, distance: null },
    };
    this.onClickSearch();
  }

  onClickSearch() {
    this.page = 0;
    this.setParams();
  }

  onPageChange(event: [page: number, pageSize: number]) {
    this.pageSize = event[1];
    this.page = event[0] - 1;
    this.setParams();
  }

  onClickCard(id: number) {
    this.router.navigate(['/location', id]);
  }

  markDisabledDays: (date: NgbDate, current?: { year: number; month: number }) => boolean = (
    date: NgbDate,
    current?: { year: number; month: number }
  ) => {
    return date.before(NgbDateUtil.todayAsNgbDateStruct());
  };

  onChangeMode(mode: SearchMode) {
    this.mode = mode;

    this.page = 0;
    if (mode === SearchMode.MAP) {
      this.pageSize = 100;
    } else {
      this.pageSize = 5;
    }

    this.setParams();
  }

  onChangeCoord(coord: LatLngLiteral) {
    this.filter.position.coord = coord;
    this.setParams();
  }

  private setParams() {
    this.filter.timeFrom = this.ngbDateToDate(this.timeFrom);
    this.filter.timeTo = this.ngbDateToDate(this.timeTo);

    const {
      searchString,
      plz: { plz },
      timeFrom,
      timeTo,
      position: { coord, distance },
      ...otherFilters
    } = this.filter;

    const queryParams: Record<string, any> = { ...otherFilters };

    if (searchString) queryParams.q = searchString;
    if (plz) queryParams.plz = plz;
    if (timeFrom && !isNaN(timeFrom.getTime())) queryParams.timeFrom = timeFrom.toISOString();
    if (timeTo && !isNaN(timeTo.getTime())) queryParams.timeTo = timeTo.toISOString();
    if (distance >= 0) queryParams.distance = distance;
    if (coord && coord.lat && coord.lng) {
      queryParams.lat = coord.lat;
      queryParams.lng = coord.lng;
    }

    queryParams.page = this.page;
    queryParams.pageSize = this.pageSize;

    queryParams.sort = this.sort;
    queryParams.mode = this.mode;

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams,
    });
  }

  private parseParams(params: Params) {
    this.filter.searchString = params.q;
    this.filter.plz = { plz: params.plz || '', ort: '' };
    this.filter.state = params.state;
    this.filter.address = params.address;
    this.filter.priceFrom = params.priceFrom != null ? parseFloat(params.priceFrom) : null;
    this.filter.priceTo = params.priceTo != null ? parseFloat(params.priceTo) : null;
    this.filter.timeFrom = new Date(params.timeFrom);
    this.timeFrom = NgbDateUtil.dateToNgbDateStruct(this.filter.timeFrom);
    this.filter.timeTo = new Date(params.timeTo);
    this.timeTo = NgbDateUtil.dateToNgbDateStruct(this.filter.timeTo);
    const coord =
      params.lat != null && params.lng != null ? { lat: parseFloat(params.lat), lng: parseFloat(params.lng) } : null;
    this.filter.position = { coord, distance: params.distance != null ? parseFloat(params.distance) : null };

    if (params.page) this.page = parseInt(params.page, 10);
    if (params.pageSize) this.pageSize = parseInt(params.pageSize, 10);

    this.sort = params.sort || LocationSortingCriterion.RECOMMENDED_DESC;
    this.mode = params.mode || SearchMode.LIST;
  }

  private ngbDateToDate(date: NgbDateStruct): Date {
    if (!date) return null;

    const month = date.month < 10 ? '0' + date.month : date.month;
    const day = date.day < 10 ? '0' + date.day : date.day;
    const fullDate = new Date(`${date.year}-${month}-${day}`);
    fullDate.setUTCHours(0);
    return fullDate;
  }

  private filterLocations(filterDto: LocationFilterDto, page: number, pageSize: number) {
    this.locationService.filter(filterDto, page, pageSize, this.sort).subscribe({
      next: res => {
        this.applyLocationResult(res);
      },
      error: e => {
        console.log(e);
        this.toastService.showErrorResponse(e);
      },
    });
  }

  private applyLocationResult(res: Pageable<LocationDto>) {
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
    this.changeDetectorRef.detectChanges();
  }
}
