import axiosClient from "@/api/axiosClient";
import type { ApiResponse, PagedResponse } from "@/types/common";
import type {
  BuyerRequestResponse,
  CreateBuyerRequestRequest,
  UpdateBuyerRequestRequest,
} from "@/types/listing";

interface SearchParams {
  sourceCountry?: string;
  sourceCity?: string;
  destinationCountry?: string;
  destinationCity?: string;
  itemCategory?: string;
  status?: string;
  page?: number;
  size?: number;
}

function buildQuery(params: Record<string, string | number | undefined>): string {
  const q = Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== "")
    .map(([k, v]) => `${k}=${encodeURIComponent(String(v))}`)
    .join("&");
  return q ? `?${q}` : "";
}

export const buyerRequestApi = {
  create: (data: CreateBuyerRequestRequest) =>
    axiosClient.post<ApiResponse<BuyerRequestResponse>>("/buyer-requests", data),

  search: (params: SearchParams = {}) =>
    axiosClient.get<ApiResponse<PagedResponse<BuyerRequestResponse>>>(
      `/buyer-requests${buildQuery({ ...params, page: params.page ?? 0, size: params.size ?? 20 })}`,
    ),

  my: (params: SearchParams = {}) =>
    axiosClient.get<ApiResponse<PagedResponse<BuyerRequestResponse>>>(
      `/buyer-requests/my${buildQuery({ ...params, page: params.page ?? 0, size: params.size ?? 20 })}`,
    ),

  getById: (id: string) =>
    axiosClient.get<ApiResponse<BuyerRequestResponse>>(`/buyer-requests/${id}`),

  update: (id: string, data: UpdateBuyerRequestRequest) =>
    axiosClient.put<ApiResponse<BuyerRequestResponse>>(`/buyer-requests/${id}`, data),

  delete: (id: string) =>
    axiosClient.delete<ApiResponse<null>>(`/buyer-requests/${id}`),

  publish: (id: string) =>
    axiosClient.post<ApiResponse<BuyerRequestResponse>>(`/buyer-requests/${id}/publish`),

  cancel: (id: string) =>
    axiosClient.post<ApiResponse<BuyerRequestResponse>>(`/buyer-requests/${id}/cancel`),
};
