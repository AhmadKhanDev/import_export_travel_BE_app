import axiosClient from "@/api/axiosClient";
import type { ApiResponse, PagedResponse } from "@/types/common";
import type { PaymentResponse } from "@/types/payment";

export const paymentApi = {
  pay: (bookingId: string, currency?: string) =>
    axiosClient.post<ApiResponse<PaymentResponse>>(
      `/payments/${bookingId}/pay`,
      currency ? { currency } : {},
    ),

  my: (status?: string, page = 0, size = 20) =>
    axiosClient.get<ApiResponse<PagedResponse<PaymentResponse>>>(
      `/payments/my?${status ? `status=${status}&` : ""}page=${page}&size=${size}`,
    ),

  getByBookingId: (bookingId: string) =>
    axiosClient.get<ApiResponse<PaymentResponse>>(`/payments/${bookingId}`),

  // Admin
  release: (paymentId: string) =>
    axiosClient.post<ApiResponse<PaymentResponse>>(`/payments/${paymentId}/release`),

  refund: (paymentId: string) =>
    axiosClient.post<ApiResponse<PaymentResponse>>(`/payments/${paymentId}/refund`),
};
