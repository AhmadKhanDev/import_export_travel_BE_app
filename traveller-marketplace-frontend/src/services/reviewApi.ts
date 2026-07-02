import axiosClient from "@/api/axiosClient";
import type { ApiResponse, PagedResponse } from "@/types/common";
import type {
  CreateReviewRequest,
  RatingSummaryResponse,
  ReviewResponse,
} from "@/types/review";

export const reviewApi = {
  create: (data: CreateReviewRequest) =>
    axiosClient.post<ApiResponse<ReviewResponse>>("/reviews", data),

  getByUser: (userId: string, page = 0, size = 20) =>
    axiosClient.get<ApiResponse<PagedResponse<ReviewResponse>>>(
      `/reviews/user/${userId}?page=${page}&size=${size}`,
    ),

  getByBooking: (bookingId: string) =>
    axiosClient.get<ApiResponse<ReviewResponse[]>>(`/reviews/booking/${bookingId}`),

  myReceived: (page = 0, size = 20) =>
    axiosClient.get<ApiResponse<PagedResponse<ReviewResponse>>>(
      `/reviews/my-received?page=${page}&size=${size}`,
    ),

  myGiven: (page = 0, size = 20) =>
    axiosClient.get<ApiResponse<PagedResponse<ReviewResponse>>>(
      `/reviews/my-given?page=${page}&size=${size}`,
    ),

  ratingSummary: (userId: string) =>
    axiosClient.get<ApiResponse<RatingSummaryResponse>>(
      `/reviews/user/${userId}/summary`,
    ),
};
