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
  verified: boolean;
  bookingId: string;
  message?: string;
}

export interface DeliveryCodeStatusResponse {
  bookingId: string;
  codeGenerated: boolean;
  verified: boolean;
  attemptsUsed?: number;
}
