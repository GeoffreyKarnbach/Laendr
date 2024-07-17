export enum TransactionStatus {
  ACTIVE = 'ACTIVE',
  ACCEPTED = 'ACCEPTED',
  CANCELLED = 'CANCELLED',
  COMPLETED = 'COMPLETED',
  REVIEWED = 'REVIEWED',
}

export const statusToDisplayText = {
  ACTIVE: 'Angefragt',
  ACCEPTED: 'Akzeptiert',
  CANCELLED: 'Abgebrochen',
  COMPLETED: 'Abgeschlossen',
  REVIEWED: 'Abgeschlossen & Bewertet',
};
