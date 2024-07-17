import {
  Component,
  OnDestroy,
  Input,
  Output,
  EventEmitter,
  OnChanges,
  SimpleChanges,
  ViewContainerRef,
  ViewChild,
  AfterViewInit,
} from '@angular/core';
import { Map, MapOptions, tileLayer, LatLngLiteral, marker, icon, Icon, Marker, Circle, circle } from 'leaflet';
import { LocationDto } from 'src/app/dtos';
import { MapPopupComponent } from './map-popup/map-popup.component';

@Component({
  selector: 'app-location-map',
  templateUrl: './location-map.component.html',
  styleUrls: ['./location-map.component.scss'],
})
export class LocationMapComponent implements OnChanges, OnDestroy, AfterViewInit {
  @ViewChild('hiddenReviews', { read: ViewContainerRef }) hiddenReviews: ViewContainerRef;
  @ViewChild('mapRoot', { read: ViewContainerRef }) mapRoot: ViewContainerRef;

  @Input() coord: LatLngLiteral = { lat: 48.207897, lng: 16.370336 };
  @Input() editable = false;
  @Output() coord$: EventEmitter<LatLngLiteral> = new EventEmitter();
  @Input() zoom = 15;

  @Input() radiusSize: number;

  @Input() locations: LocationDto[] = [];
  @Input() showLocationPopup = false;

  radius: Circle;
  marker: Marker;

  locationMarkers: Marker[] = [];

  options: MapOptions = {
    layers: [
      tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        opacity: 0.7,
        maxZoom: 23,
        detectRetina: true,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
      }),
    ],
    zoom: this.zoom,
    center: this.coord,
  };
  map: Map;

  observer: ResizeObserver;

  ngAfterViewInit() {
    this.observer = new ResizeObserver(e => {
      this.map.invalidateSize();
    });
    this.observer.observe(this.mapRoot.element.nativeElement);
  }

  ngOnChanges(changes: SimpleChanges): void {
    const newCoord = changes?.coord?.currentValue as LatLngLiteral;
    if (newCoord && !this.marker) {
      this.createMarker(newCoord);
    }

    const radiusSizeChange = changes?.radiusSize;
    if (radiusSizeChange?.currentValue) {
      if (this.radius) {
        this.radius.setRadius(radiusSizeChange.currentValue * 1000);
      } else {
        if (this.coord) {
          this.radius = circle(this.coord, { radius: radiusSizeChange.currentValue * 1000 });
          if (this.map) {
            this.radius.addTo(this.map);
          }
        }
      }
    } else if (radiusSizeChange && this.radius) {
      this.radius.remove();
      this.radius = undefined;
    }

    const locationChanges = changes?.locations;
    if (locationChanges?.currentValue) {
      this.locationMarkers = (locationChanges.currentValue as LocationDto[])
        .filter(location => location.coord)
        .map(location => {
          const locationMarker = marker(location.coord, {
            icon: icon({
              ...Icon.Default.prototype.options,
              iconUrl: 'assets/marker-icon.png',
              iconRetinaUrl: 'assets/marker-icon-2x.png',
              shadowUrl: 'assets/marker-shadow.png',
              className: 'green-icon',
            }),
          });

          if (this.showLocationPopup && this.hiddenReviews) {
            const component = this.hiddenReviews.createComponent(MapPopupComponent);
            component.setInput('location', location);

            locationMarker.bindPopup(component.location.nativeElement);
          }

          return locationMarker;
        });
    }
  }

  ngOnDestroy() {
    this.map.clearAllEventListeners();
    this.observer.disconnect();
  }

  onMapReady(map: Map) {
    this.map = map;
    map.setZoom(this.zoom);
    map.addEventListener('click', e => this.setMarker(e.latlng));
    if (this.coord) {
      map.panTo(this.coord);
    }
    this.map.invalidateSize();
  }

  createMarker(coord: LatLngLiteral) {
    this.marker = marker(coord, {
      icon: icon({
        ...Icon.Default.prototype.options,
        iconUrl: 'assets/marker-icon.png',
        iconRetinaUrl: 'assets/marker-icon-2x.png',
        shadowUrl: 'assets/marker-shadow.png',
      }),
      draggable: this.editable,
    }).on('dragend', e => {
      this.coord$.emit(e.target._latlng);
      if (this.radius) {
        this.radius.setLatLng(e.target._latlng);
      }
    });

    if (this.radiusSize && !this.radius) {
      this.radius = circle(coord, { radius: this.radiusSize * 1000 });
      if (this.map) {
        this.radius.addTo(this.map);
      }
    } else if (this.radius) {
      this.radius.setLatLng(coord);
    }

    if (this.editable) {
      this.marker.bindPopup('<button type="button" class="btn btn-primary">Löschen</button>').on('popupopen', e =>
        e.target
          .getPopup()
          .getElement()
          .querySelector('.btn')
          .addEventListener('click', () => {
            this.marker.remove();
            this.marker = undefined;
            this.radius?.remove();
            this.radius = undefined;
            this.coord$.emit(undefined);
          })
      );
    }

    if (this.map) {
      this.marker.addTo(this.map);
    }
  }

  setMarker(coord: LatLngLiteral) {
    if (!this.editable) return;

    if (this.marker) {
      this.marker.setLatLng(coord);
      if (this.radius) {
        this.radius.setLatLng(coord);
      }
    } else {
      this.createMarker(coord);
    }
    this.coord$.emit(coord);
  }
}
