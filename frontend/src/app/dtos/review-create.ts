export interface ReviewCreate {
  transactionId: number;
  rating: number;
  comment?: string;
}
