export enum CancelReason {
  NO_INTEREST = 'NO_INTEREST',
  LOCATION_REMOVED = 'LOCATION_REMOVED',
  SCAM = 'SCAM',
}

export const reasonToDisplayText = {
  NO_INTEREST: 'Kein Interesse',
  LOCATION_REMOVED: 'Location entfernt',
  SCAM: 'Scam',
};
