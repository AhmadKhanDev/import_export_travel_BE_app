export type NotificationStatus = "UNREAD" | "READ";
export type NotificationChannel = "IN_APP" | "EMAIL" | "SMS";
export type NotificationType =
  | "BOOKING_CREATED"
  | "BOOKING_CANCELLED"
  | "BOOKING_COMPLETED"
  | "PAYMENT_RECEIVED"
  | "PAYMENT_RELEASED"
  | "OFFER_RECEIVED"
  | "OFFER_ACCEPTED"
  | "OFFER_REJECTED"
  | "KYC_APPROVED"
  | "KYC_REJECTED"
  | "MATCH_FOUND"
  | "DISPUTE_CREATED"
  | "DISPUTE_RESOLVED"
  | "SYSTEM";

export interface NotificationResponse {
  id: string;
  userId: string;
  title: string;
  message: string;
  type: NotificationType;
  channel: NotificationChannel;
  status: NotificationStatus;
  referenceId?: string;
  referenceType?: string;
  createdAt: string;
  readAt?: string;
}

export interface UnreadCountResponse {
  unreadCount: number;
}
