export interface ErrorResponse {
  message: string;
  errors: ErrorResponseItem[];
}

export interface ErrorResponseItem {
  id?: number;
  message?: string;
  errors: ErrorResponseItemDetail[];
}

export interface ErrorResponseItemDetail {
  field: string;
  message: string;
}
