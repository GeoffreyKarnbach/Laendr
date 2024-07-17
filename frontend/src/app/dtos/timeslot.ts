export interface Timeslot {
  id?: number;
  start: Date | string;
  end: Date | string;
  price: number;
  priceHourly?: number;
  isUsed?: boolean;
  isRequested?: boolean;
  locationId?: number;
  isRequestedByCallingUser?: boolean;
}
