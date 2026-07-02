import axiosClient from "@/api/axiosClient";
import type { ApiResponse, PagedResponse } from "@/types/common";
import type { MatchResponse } from "@/types/matching";

export const matchingApi = {
  generateByBuyerRequest: (buyerRequestId: string) =>
    axiosClient.post<ApiResponse<MatchResponse[]>>(
      `/matches/generate/by-request/${buyerRequestId}`,
    ),

  generateByTravellerTrip: (travellerTripId: string) =>
    axiosClient.post<ApiResponse<MatchResponse[]>>(
      `/matches/generate/by-trip/${travellerTripId}`,
    ),

  getByBuyerRequest: (buyerRequestId: string, page = 0, size = 20) =>
    axiosClient.get<ApiResponse<PagedResponse<MatchResponse>>>(
      `/matches/by-request/${buyerRequestId}?page=${page}&size=${size}`,
    ),

  getByTravellerTrip: (travellerTripId: string, page = 0, size = 20) =>
    axiosClient.get<ApiResponse<PagedResponse<MatchResponse>>>(
      `/matches/by-trip/${travellerTripId}?page=${page}&size=${size}`,
    ),

  getById: (matchId: string) =>
    axiosClient.get<ApiResponse<MatchResponse>>(`/matches/${matchId}`),

  reject: (matchId: string) =>
    axiosClient.post<ApiResponse<MatchResponse>>(`/matches/${matchId}/reject`),
};
