import axiosClient from "@/api/axiosClient";
import type { ApiResponse } from "@/types/common";
import type {
  DeliveryCodeStatusResponse,
  DeliveryVerificationResultResponse,
  GenerateDeliveryCodeResponse,
} from "@/types/payment";

export const deliveryApi = {
  generate: (bookingId: string) =>
    axiosClient.post<ApiResponse<GenerateDeliveryCodeResponse>>(
      `/delivery-codes/${bookingId}/generate`,
    ),

  verify: (bookingId: string, code: string) =>
    axiosClient.post<ApiResponse<DeliveryVerificationResultResponse>>(
      `/delivery-codes/${bookingId}/verify`,
      { code },
    ),

  getStatus: (bookingId: string) =>
    axiosClient.get<ApiResponse<DeliveryCodeStatusResponse>>(
      `/delivery-codes/${bookingId}/status`,
    ),
};
