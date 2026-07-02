export interface ReviewResponse {
  id: string;
  bookingId: string;
  reviewerId: string;
  reviewerName: string;
  revieweeId: string;
  revieweeName: string;
  rating: number;
  comment?: string;
  createdAt: string;
}

export interface CreateReviewRequest {
  bookingId: string;
  rating: number;
  comment?: string;
}

export interface RatingSummaryResponse {
  userId: string;
  totalReviews: number;
  averageRating: number;
  ratingBreakdown: Record<string, number>;
}
