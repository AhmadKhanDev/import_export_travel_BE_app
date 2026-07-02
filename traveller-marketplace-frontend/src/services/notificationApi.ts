import axiosClient from "@/api/axiosClient";
import type { ApiResponse, PagedResponse } from "@/types/common";
import type { NotificationResponse, UnreadCountResponse } from "@/types/notification";

export const notificationApi = {
  my: (unreadOnly = false, page = 0, size = 20) =>
    axiosClient.get<ApiResponse<PagedResponse<NotificationResponse>>>(
      `/notifications/my?unreadOnly=${unreadOnly}&page=${page}&size=${size}`,
    ),

  getById: (id: string) =>
    axiosClient.get<ApiResponse<NotificationResponse>>(`/notifications/${id}`),

  markAsRead: (id: string) =>
    axiosClient.post<ApiResponse<NotificationResponse>>(`/notifications/${id}/read`),

  markAllAsRead: () =>
    axiosClient.post<ApiResponse<{ updatedCount: number }>>("/notifications/read-all"),

  unreadCount: () =>
    axiosClient.get<ApiResponse<UnreadCountResponse>>("/notifications/unread-count"),
};
