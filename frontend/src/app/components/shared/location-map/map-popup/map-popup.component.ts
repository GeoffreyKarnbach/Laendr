import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { LocationDto } from 'src/app/dtos';

@Component({
  selector: 'app-map-popup',
  templateUrl: './map-popup.component.html',
  styleUrls: ['./map-popup.component.scss'],
})
export class MapPopupComponent {
  @Input() location: LocationDto;

  constructor(private router: Router) {}

  onClickOpen() {
    this.router.navigate(['/location', this.location.id]);
  }
}
