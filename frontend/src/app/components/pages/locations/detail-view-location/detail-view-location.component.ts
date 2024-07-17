import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { LocationDto } from 'src/app/dtos';
import { LocationService, ToastService } from 'src/app/services';
import { AustriaState } from 'src/app/enum';

enum Tabs {
  TIMESLOTS,
  REVIEWS,
}

@Component({
  selector: 'app-detail-view-location',
  templateUrl: './detail-view-location.component.html',
  styleUrls: ['./detail-view-location.component.scss'],
})
export class DetailViewLocationComponent implements OnInit {
  locationId = 1;

  location: LocationDto = {
    id: -1,
    name: 'Test Location',
    description: 'Test Description',
    isRemoved: false,
    plz: { plz: '1234', ort: '' },
    state: AustriaState.W,
    address: 'Test Address',
    size: 1234,
    createdAt: new Date(),
    callerIsOwner: true,
  };

  activeTab = Tabs.TIMESLOTS;
  readonly enumTabs = Tabs;

  constructor(
    private route: ActivatedRoute,
    private locationService: LocationService,
    private router: Router,
    private toastSerivce: ToastService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.locationId = params.id;

      this.locationService.getById(this.locationId).subscribe({
        next: location => {
          this.location = location;
          this.location.tags.sort();
        },
        error: e => {
          this.toastSerivce.showErrorResponse(e);
          this.router.navigate(['']);
        },
      });
    });
  }

  removeLocation() {
    this.locationService.remove(this.locationId).subscribe({
      next: empty => {
        this.toastSerivce.showSuccess(`Location ${this.location.name} erfolgreich gelöscht`);
        this.router.navigate(['']);
      },
      error: e => {
        this.toastSerivce.showErrorResponse(e);
        this.router.navigate(['']);
      },
    });
  }
}
