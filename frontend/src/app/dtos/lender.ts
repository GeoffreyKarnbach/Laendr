import { AustriaState } from '../enum';
import { Reputation } from './reputation';

export interface LenderDto {
  id: number;
  name?: string;
  phone?: string;
  email?: string;
  description?: string;
  createdAt: Date;
  state?: AustriaState;
  callerIsThisLender: boolean;
  reputation?: Reputation;
}

export interface LightLender {
  id: number;
  name?: string;
  phone?: string;
  email?: string;
  reputation?: Reputation;
}
