import axiosClient from "@/api/axiosClient";
import type { ApiResponse } from "@/types/common";
import type { AdminKycResponse, KycDocument, KycSubmitRequest } from "@/types/user";

export const kycApi = {
  // Traveller: submit KYC
  submit: (data: KycSubmitRequest) =>
    axiosClient.post<ApiResponse<KycDocument>>("/kyc/submit", data),

  // Traveller: get own KYC status
  getMyStatus: () =>
    axiosClient.get<ApiResponse<KycDocument>>("/kyc/my"),

  // Admin
  listPending: (page = 0, size = 20) =>
    axiosClient.get<ApiResponse<{ content: AdminKycResponse[]; totalElements: number }>>(
      `/admin/kyc/pending?page=${page}&size=${size}`,
    ),

  listAll: (status?: string, page = 0, size = 20) =>
    axiosClient.get<ApiResponse<{ content: AdminKycResponse[]; totalElements: number }>>(
      `/admin/kyc?${status ? `status=${status}&` : ""}page=${page}&size=${size}`,
    ),

  getById: (id: string) =>
    axiosClient.get<ApiResponse<AdminKycResponse>>(`/admin/kyc/${id}`),

  approve: (id: string) =>
    axiosClient.post<ApiResponse<AdminKycResponse>>(`/admin/kyc/${id}/approve`),

  reject: (id: string, reason: string) =>
    axiosClient.post<ApiResponse<AdminKycResponse>>(`/admin/kyc/${id}/reject`, { reason }),
};
