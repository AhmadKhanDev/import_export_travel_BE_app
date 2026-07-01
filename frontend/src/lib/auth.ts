import api from "./api";
import type {
  ApiResponse,
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  User,
} from "@/types";

export async function register(data: RegisterRequest): Promise<AuthResponse> {
  const res = await api.post<ApiResponse<AuthResponse>>("/auth/register", data);
  return res.data.data;
}

export async function login(data: LoginRequest): Promise<AuthResponse> {
  const res = await api.post<ApiResponse<AuthResponse>>("/auth/login", data);
  return res.data.data;
}

export async function logout(): Promise<void> {
  const refreshToken = localStorage.getItem("refreshToken");
  try {
    await api.post("/auth/logout", refreshToken ? { refreshToken } : undefined);
  } finally {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
  }
}

export async function getCurrentUser(): Promise<User> {
  const res = await api.get<ApiResponse<User>>("/auth/me");
  return res.data.data;
}

export function saveTokens(accessToken: string, refreshToken: string) {
  localStorage.setItem("accessToken", accessToken);
  localStorage.setItem("refreshToken", refreshToken);
}

export function isAuthenticated(): boolean {
  return !!localStorage.getItem("accessToken");
}
