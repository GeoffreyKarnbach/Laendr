export interface Pageable<T> {
  totalResults: number;
  totalPages: number;
  resultCount: number;
  result: T[];
}
