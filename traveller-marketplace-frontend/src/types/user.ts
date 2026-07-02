export interface UserProfile {
  id: string;
  userId: string;
  bio?: string;
  city?: string;
  country?: string;
  avatarUrl?: string;
  createdAt: string;
  updatedAt: string;
}

export interface UpdateProfileRequest {
  bio?: string;
  city?: string;
  country?: string;
  phoneNumber?: string;
  fullName?: string;
}

export type KycStatus = "PENDING_REVIEW" | "APPROVED" | "REJECTED";

export interface KycDocument {
  id: string;
  userId: string;
  status: KycStatus;
}

export interface KycSubmitRequest {
  documentType: string;
  documentNumber: string;
  documentUrl?: string;
}

export interface AdminKycResponse {
  id: string;
  userId: string;
  userFullName: string;
  userEmail: string;
  status: KycStatus;
  createdAt?: string;
}
