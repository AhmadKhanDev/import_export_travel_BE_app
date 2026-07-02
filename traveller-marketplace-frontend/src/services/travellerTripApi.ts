import axiosClient from "@/api/axiosClient";
import type { ApiResponse, PagedResponse } from "@/types/common";
import type {
  CreateTravellerTripRequest,
  TravellerTripResponse,
  UpdateTravellerTripRequest,
} from "@/types/listing";

interface SearchParams {
  sourceCountry?: string;
  sourceCity?: string;
  destinationCountry?: string;
  destinationCity?: string;
  status?: string;
  travelDateFrom?: string;
  travelDateTo?: string;
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

export const travellerTripApi = {
  create: (data: CreateTravellerTripRequest) =>
    axiosClient.post<ApiResponse<TravellerTripResponse>>("/traveller-trips", data),

  search: (params: SearchParams = {}) =>
    axiosClient.get<ApiResponse<PagedResponse<TravellerTripResponse>>>(
      `/traveller-trips${buildQuery({ ...params, page: params.page ?? 0, size: params.size ?? 20 })}`,
    ),

  my: (params: SearchParams = {}) =>
    axiosClient.get<ApiResponse<PagedResponse<TravellerTripResponse>>>(
      `/traveller-trips/my${buildQuery({ ...params, page: params.page ?? 0, size: params.size ?? 20 })}`,
    ),

  getById: (id: string) =>
    axiosClient.get<ApiResponse<TravellerTripResponse>>(`/traveller-trips/${id}`),

  update: (id: string, data: UpdateTravellerTripRequest) =>
    axiosClient.put<ApiResponse<TravellerTripResponse>>(`/traveller-trips/${id}`, data),

  delete: (id: string) =>
    axiosClient.delete<ApiResponse<null>>(`/traveller-trips/${id}`),

  publish: (id: string) =>
    axiosClient.post<ApiResponse<TravellerTripResponse>>(`/traveller-trips/${id}/publish`),

  cancel: (id: string) =>
    axiosClient.post<ApiResponse<TravellerTripResponse>>(`/traveller-trips/${id}/cancel`),
};
