import { CancelReason } from '../enum';

export interface TransactionCancel {
  transactionId: number;
  cancelMessage?: string;
  cancelReason: CancelReason;
}
