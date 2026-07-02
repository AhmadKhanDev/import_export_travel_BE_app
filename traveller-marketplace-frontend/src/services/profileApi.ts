import axiosClient from "@/api/axiosClient";
import type { ApiResponse } from "@/types/common";
import type { UpdateProfileRequest, UserProfile } from "@/types/user";

export const profileApi = {
  get: () =>
    axiosClient.get<ApiResponse<UserProfile>>("/profile/me"),

  update: (data: UpdateProfileRequest) =>
    axiosClient.put<ApiResponse<UserProfile>>("/profile/me", data),
};
