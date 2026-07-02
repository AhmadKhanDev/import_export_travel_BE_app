import axiosClient from "@/api/axiosClient";
import type { ApiResponse, PagedResponse } from "@/types/common";
import type { BookingResponse } from "@/types/booking";

export const bookingApi = {
  my: (status?: string, page = 0, size = 20) =>
    axiosClient.get<ApiResponse<PagedResponse<BookingResponse>>>(
      `/bookings/my?${status ? `status=${status}&` : ""}page=${page}&size=${size}`,
    ),

  getById: (id: string) =>
    axiosClient.get<ApiResponse<BookingResponse>>(`/bookings/${id}`),

  cancel: (id: string) =>
    axiosClient.post<ApiResponse<BookingResponse>>(`/bookings/${id}/cancel`),

  markInTransit: (id: string) =>
    axiosClient.post<ApiResponse<BookingResponse>>(`/bookings/${id}/mark-in-transit`),

  markDelivered: (id: string) =>
    axiosClient.post<ApiResponse<BookingResponse>>(`/bookings/${id}/mark-delivered`),
};
