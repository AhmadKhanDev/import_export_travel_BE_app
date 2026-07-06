import axiosClient from "@/api/axiosClient";
import type { ApiResponse } from "@/types/common";
import type {
  TrackingLocationUpdateRequest,
  TrackingSessionResponse,
} from "@/types/tracking";

export const trackingApi = {
  getState: (bookingId: string) =>
    axiosClient.get<ApiResponse<TrackingSessionResponse>>(
      `/bookings/${bookingId}/tracking`,
    ),

  start: (bookingId: string) =>
    axiosClient.post<ApiResponse<TrackingSessionResponse>>(
      `/bookings/${bookingId}/tracking/start`,
    ),

  stop: (bookingId: string) =>
    axiosClient.post<ApiResponse<TrackingSessionResponse>>(
      `/bookings/${bookingId}/tracking/stop`,
    ),

  updateLocation: (bookingId: string, data: TrackingLocationUpdateRequest) =>
    axiosClient.post<ApiResponse<TrackingSessionResponse>>(
      `/bookings/${bookingId}/tracking/location`,
      data,
    ),
};
