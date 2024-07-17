import { CancelReason, Role } from 'src/app/enum';
import { Reputation } from './reputation';
import { Review } from './review';
import { Timeslot } from './timeslot';

export interface Transaction {
  id: number;
  locationName: string;
  locationId: number;
  locationRemoved?: boolean;
  partnerName: string;
  partnerEmail?: string;
  partnerPhone?: string;
  partnerReputation: Reputation;
  lenderId: number;
  ownRoleInTransaction: Role;
  initialMessage: string;
  createdAt: Date;
  completedAt?: Date;
  totalPaid?: number;
  totalConcerned: number;
  timeslot: Timeslot;
  cancelled?: boolean;
  cancelByRole?: Role;
  cancelReason?: CancelReason;
  cancelDescription?: string;
  cancelNotified?: boolean;
  reviewRenter?: Review;
  reviewLocation?: Review;
}
