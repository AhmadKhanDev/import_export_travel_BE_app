import axiosClient from "@/api/axiosClient";
import type { ApiResponse, PagedResponse } from "@/types/common";
import type {
  CreateDisputeRequest,
  DisputeResponse,
  RejectDisputeRequest,
  ResolveDisputeRequest,
} from "@/types/dispute";

export const disputeApi = {
  create: (data: CreateDisputeRequest) =>
    axiosClient.post<ApiResponse<DisputeResponse>>("/disputes", data),

  my: (status?: string, page = 0, size = 20) =>
    axiosClient.get<ApiResponse<PagedResponse<DisputeResponse>>>(
      `/disputes/my?${status ? `status=${status}&` : ""}page=${page}&size=${size}`,
    ),

  getById: (id: string) =>
    axiosClient.get<ApiResponse<DisputeResponse>>(`/disputes/${id}`),

  // Admin
  adminList: (status?: string, page = 0, size = 20) =>
    axiosClient.get<ApiResponse<PagedResponse<DisputeResponse>>>(
      `/admin/disputes?${status ? `status=${status}&` : ""}page=${page}&size=${size}`,
    ),

  adminGetById: (id: string) =>
    axiosClient.get<ApiResponse<DisputeResponse>>(`/admin/disputes/${id}`),

  markUnderReview: (id: string) =>
    axiosClient.post<ApiResponse<DisputeResponse>>(
      `/admin/disputes/${id}/mark-under-review`,
    ),

  resolve: (id: string, data: ResolveDisputeRequest) =>
    axiosClient.post<ApiResponse<DisputeResponse>>(`/admin/disputes/${id}/resolve`, data),

  reject: (id: string, data: RejectDisputeRequest) =>
    axiosClient.post<ApiResponse<DisputeResponse>>(`/admin/disputes/${id}/reject`, data),
};
