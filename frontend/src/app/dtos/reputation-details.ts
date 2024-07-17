export interface ReputationDiscountDto {
  name: string;
  parameters?: string;
  discount: number;
}

export interface ReputationDetailReviewDto {
  reviewerId: number;
  reviewDate: Date;
  positiveBaseWeight: number;
  negativeBaseWeight: number;
  discounts: ReputationDiscountDto[];
  timeDecay: number;
}

export interface ReputationDetailDto {
  calculationTime: Date;
  reviews: ReputationDetailReviewDto[];
}
