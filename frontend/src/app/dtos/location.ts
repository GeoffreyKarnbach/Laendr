import { LatLngLiteral } from 'leaflet';
import { AustriaState } from 'src/app/enum';
import { LightLender } from './lender';
import { Plz } from './plz';
import { Reputation } from './reputation';

export interface LocationDto {
  id?: number;
  name: string;
  description: string;
  isRemoved: boolean;
  plz?: Plz;
  state: AustriaState;
  address: string;
  size: number;
  createdAt: Date;
  callerIsOwner: boolean;
  lender?: LightLender;
  primaryImageUrl?: string;
  tags?: string[];
  reputation?: Reputation;
  coord?: LatLngLiteral;
}

export interface LocationTagCollectionDto {
  tags: string[];
}

export interface LocationFilterDto {
  searchString?: string;
  plz?: Plz;
  state?: AustriaState;
  address?: string;
  timeFrom?: Date;
  timeTo?: Date;
  priceFrom?: number;
  priceTo?: number;
  position?: LocationFilterDistanceDto;
  tags?: string[];
}

export interface LocationFilterDistanceDto {
  distance: number;
  coord: LatLngLiteral;
}
