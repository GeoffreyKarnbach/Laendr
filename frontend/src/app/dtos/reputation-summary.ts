export interface ReputationSummary {
  subjectId: number;
  subject: string;
  karma?: number;
  averageRating?: number;
  ratings?: number;
  lastChange?: Date;
}
