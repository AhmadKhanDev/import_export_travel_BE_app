export type Role = "BUYER" | "TRAVELLER" | "ADMIN";
export type AccountStatus = "ACTIVE" | "DISABLED" | "SUSPENDED";

export interface UserResponse {
  id: string;
  email: string;
  fullName: string;
  phoneNumber?: string;
  role: Role;
  accountStatus: AccountStatus;
  profileCompleted: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserResponse;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  phoneNumber?: string;
  role: Role;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}
