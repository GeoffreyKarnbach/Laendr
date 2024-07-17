export interface ImageCollection {
  locationId: number;
  images: string[];
  callerIsOwner: boolean;
}

export interface ImageUpdatedCollection {
  locationId: number;
  images: string[];
}
