import { Component, EventEmitter, Input, Output } from '@angular/core';
import { LatLngLiteral } from 'leaflet';
import { LocationDto, LocationFilterDistanceDto } from 'src/app/dtos';

@Component({
  selector: 'app-map-filter',
  templateUrl: './map-filter.component.html',
  styleUrls: ['./map-filter.component.scss'],
})
export class MapFilterComponent {
  @Input() @Output() position: LocationFilterDistanceDto = { coord: null, distance: null };
  @Output() keydownEnter: EventEmitter<void> = new EventEmitter();
  @Input() onlyDistance = false;

  @Input() locations: LocationDto[] = [];

  onChangeCoord(coord: LatLngLiteral) {
    this.position.coord = coord;
  }
}
