import axiosClient from "@/api/axiosClient";
import type { ApiResponse, PagedResponse } from "@/types/common";
import type { NotificationResponse } from "@/types/notification";
import type { PaymentResponse } from "@/types/payment";
import type { BookingResponse } from "@/types/booking";

export interface AdminDashboardSummary {
  totalUsers: number;
  totalBuyers: number;
  totalTravellers: number;
  totalAdmins: number;
  activeUsers: number;
  disabledUsers: number;
  pendingKycCount: number;
  approvedKycCount: number;
  rejectedKycCount: number;
  publishedBuyerRequests: number;
  publishedTravellerTrips: number;
  totalMatches: number;
  totalOffers: number;
  totalBookings: number;
  paymentPendingBookings: number;
  paymentHeldBookings: number;
  completedBookings: number;
  disputedBookings: number;
  totalPayments: number;
  heldPayments: number;
  releasedPayments: number;
  refundedPayments: number;
  openDisputes: number;
  underReviewDisputes: number;
  resolvedDisputes: number;
  totalReviews: number;
  totalChatRooms: number;
}

export interface AdminUserResponse {
  id: string;
  email: string;
  fullName: string;
  phoneNumber?: string;
  role: string;
  accountStatus: string;
  profileCompleted: boolean;
  createdAt: string;
}

export interface AuditLogResponse {
  id: string;
  action: string;
  entityType: string;
  entityId?: string;
  actorUserId?: string;
  actorEmail?: string;
  details?: string;
  createdAt: string;
}

export const adminApi = {
  // Dashboard
  getDashboardSummary: () =>
    axiosClient.get<ApiResponse<AdminDashboardSummary>>("/admin/dashboard/summary"),

  getRecentActivity: (page = 0, size = 20) =>
    axiosClient.get<ApiResponse<PagedResponse<AuditLogResponse>>>(
      `/admin/dashboard/recent-activity?page=${page}&size=${size}`,
    ),

  // Users
  getUsers: (params: Record<string, string | undefined> = {}, page = 0, size = 20) => {
    const q = Object.entries({ ...params, page: String(page), size: String(size) })
      .filter(([, v]) => v)
      .map(([k, v]) => `${k}=${encodeURIComponent(v!)}`)
      .join("&");
    return axiosClient.get<ApiResponse<PagedResponse<AdminUserResponse>>>(
      `/admin/users?${q}`,
    );
  },

  getUserDetail: (userId: string) =>
    axiosClient.get<ApiResponse<AdminUserResponse>>(`/admin/users/${userId}`),

  disableUser: (userId: string, reason: string) =>
    axiosClient.post<ApiResponse<AdminUserResponse>>(
      `/admin/users/${userId}/disable`,
      { reason },
    ),

  enableUser: (userId: string) =>
    axiosClient.post<ApiResponse<AdminUserResponse>>(`/admin/users/${userId}/enable`),

  changeRole: (userId: string, role: string) =>
    axiosClient.post<ApiResponse<AdminUserResponse>>(
      `/admin/users/${userId}/change-role`,
      { role },
    ),

  // Bookings
  getBookings: (status?: string, page = 0, size = 20) =>
    axiosClient.get<ApiResponse<PagedResponse<BookingResponse>>>(
      `/admin/bookings?${status ? `status=${status}&` : ""}page=${page}&size=${size}`,
    ),

  // Payments
  getPayments: (status?: string, page = 0, size = 20) =>
    axiosClient.get<ApiResponse<PagedResponse<PaymentResponse>>>(
      `/admin/payments?${status ? `status=${status}&` : ""}page=${page}&size=${size}`,
    ),

  // Notifications
  getNotifications: (userId?: string, page = 0, size = 20) =>
    axiosClient.get<ApiResponse<PagedResponse<NotificationResponse>>>(
      `/admin/notifications?${userId ? `userId=${userId}&` : ""}page=${page}&size=${size}`,
    ),

  sendNotification: (data: {
    userId: string;
    title: string;
    message: string;
    type: string;
  }) =>
    axiosClient.post<ApiResponse<NotificationResponse>>(
      "/admin/notifications/send",
      data,
    ),

  // Audit logs
  getAuditLogs: (params: Record<string, string | undefined> = {}, page = 0, size = 20) => {
    const q = Object.entries({ ...params, page: String(page), size: String(size) })
      .filter(([, v]) => v)
      .map(([k, v]) => `${k}=${encodeURIComponent(v!)}`)
      .join("&");
    return axiosClient.get<ApiResponse<PagedResponse<AuditLogResponse>>>(
      `/admin/audit-logs?${q}`,
    );
  },
};
