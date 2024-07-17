import { LatLngLiteral } from 'leaflet';

export interface Plz {
  plz: string;
  ort: string;
  coord?: LatLngLiteral;
}
