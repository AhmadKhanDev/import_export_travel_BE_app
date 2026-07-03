export type PaymentStatus = "PENDING" | "HELD" | "RELEASED" | "REFUNDED" | "FAILED";

export interface PaymentResponse {
  id: string;
  bookingId: string;
  buyerId: string;
  travellerId: string;
  amount: number;
  currency: string;
  status: PaymentStatus;
  idempotencyKey?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PayRequest {
  currency?: string;
}

export interface GenerateDeliveryCodeResponse {
  code: string;
  bookingId: string;
  expiresAt?: string;
}

export interface DeliveryVerificationResultResponse {
  bookingId: string;
  paymentId?: string;
  codeStatus?: DeliveryCodeStatus;
  bookingStatus?: string;
  paymentStatus?: string;
  verifiedAt?: string;
  paymentReleasedAt?: string;
  message?: string;
}

export type DeliveryCodeStatus = "ACTIVE" | "USED" | "EXPIRED";

export interface DeliveryCodeStatusResponse {
  bookingId: string;
  hasActiveCode: boolean;
  status?: DeliveryCodeStatus;
  expiresAt?: string;
  verifiedAt?: string;
  createdAt?: string;
}
