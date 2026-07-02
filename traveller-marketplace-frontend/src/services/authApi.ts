import axiosClient from "@/api/axiosClient";
import type { ApiResponse } from "@/types/common";
import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  UserResponse,
} from "@/types/auth";

export const authApi = {
  register: (data: RegisterRequest) =>
    axiosClient.post<ApiResponse<AuthResponse>>("/auth/register", data),

  login: (data: LoginRequest) =>
    axiosClient.post<ApiResponse<AuthResponse>>("/auth/login", data),

  refresh: (refreshToken: string) =>
    axiosClient.post<ApiResponse<AuthResponse>>("/auth/refresh", { refreshToken }),

  logout: (refreshToken?: string) =>
    axiosClient.post<ApiResponse<null>>("/auth/logout", refreshToken ? { refreshToken } : {}),

  me: () =>
    axiosClient.get<ApiResponse<UserResponse>>("/auth/me"),
};
