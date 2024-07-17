import { LatLngLiteral } from 'leaflet';
import { AustriaState } from '../enum';
import { Plz } from './plz';
import { Reputation } from './reputation';

export interface UserDto {
  id?: number;
  email: string;
  locked: boolean;
  loginAttempts: number;
  name?: string;
  state?: AustriaState;
  plz?: Plz;
  renterPhone?: string;
  renterEmail?: string;
  lender: boolean;
  lenderDescription?: string;
  lenderPhone?: string;
  lenderEmail?: string;
  reputation?: Reputation;
  createdAt: Date;
  coordinates?: LatLngLiteral;
}

export interface UserPasswordChangeDto {
  currentPassword: string;
  newPassword: string;
  repeatedPassword: string;
}
