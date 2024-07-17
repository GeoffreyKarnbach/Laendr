import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { LocationDto } from 'src/app/dtos';
import { LocationSortingCriterion } from 'src/app/enum';
import { Globals } from 'src/app/global';
import { AuthService, LocationService, ToastService } from 'src/app/services';

@Component({
  selector: 'app-home-location-display',
  templateUrl: './home-location-display.component.html',
  styleUrls: ['./home-location-display.component.scss'],
})
export class HomeLocationDisplayComponent implements OnInit {
  locations: LocationDto[] = [];

  constructor(
    private locationService: LocationService,
    private router: Router,
    private toastService: ToastService,
    private globals: Globals,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.search();
  }

  onClickCard(id: number) {
    this.router.navigate(['/location', id]);
  }

  onClickToSearch() {
    this.router.navigate(['/search'], {
      queryParams: {
        q: '',
      },
    });
  }

  isLoggedIn() {
    return this.authService.isLoggedIn();
  }

  private search() {
    this.locationService.filter({ searchString: '' }, 0, 31, LocationSortingCriterion.RECOMMENDED_DESC).subscribe({
      next: res => {
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
    });
  }
}
