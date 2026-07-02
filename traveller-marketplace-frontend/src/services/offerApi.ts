import axiosClient from "@/api/axiosClient";
import type { ApiResponse, PagedResponse } from "@/types/common";
import type { CreateOfferRequest, OfferResponse } from "@/types/offer";
import type { BookingResponse } from "@/types/booking";

export const offerApi = {
  create: (data: CreateOfferRequest) =>
    axiosClient.post<ApiResponse<OfferResponse>>("/offers", data),

  getById: (id: string) =>
    axiosClient.get<ApiResponse<OfferResponse>>(`/offers/${id}`),

  my: (status?: string, page = 0, size = 20) =>
    axiosClient.get<ApiResponse<PagedResponse<OfferResponse>>>(
      `/offers/my?${status ? `status=${status}&` : ""}page=${page}&size=${size}`,
    ),

  byBuyerRequest: (buyerRequestId: string, status?: string, page = 0) =>
    axiosClient.get<ApiResponse<PagedResponse<OfferResponse>>>(
      `/offers/by-request/${buyerRequestId}?${status ? `status=${status}&` : ""}page=${page}&size=20`,
    ),

  byTravellerTrip: (travellerTripId: string, page = 0) =>
    axiosClient.get<ApiResponse<PagedResponse<OfferResponse>>>(
      `/offers/by-trip/${travellerTripId}?page=${page}&size=20`,
    ),

  accept: (id: string) =>
    axiosClient.post<ApiResponse<BookingResponse>>(`/offers/${id}/accept`),

  reject: (id: string) =>
    axiosClient.post<ApiResponse<OfferResponse>>(`/offers/${id}/reject`),

  cancel: (id: string) =>
    axiosClient.post<ApiResponse<OfferResponse>>(`/offers/${id}/cancel`),
};
