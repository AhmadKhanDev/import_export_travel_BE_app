import axiosClient from "@/api/axiosClient";
import type { ApiResponse, PagedResponse } from "@/types/common";

export interface ChatRoomResponse {
  id: string;
  bookingId: string;
  buyerId: string;
  buyerName: string;
  travellerId: string;
  travellerName: string;
  status: string;
  unreadCount?: number;
  lastMessageAt?: string;
  createdAt: string;
}

export type MessageType = "TEXT" | "IMAGE" | "SYSTEM";

export interface ChatMessageResponse {
  id: string;
  chatRoomId: string;
  senderId: string;
  senderName: string;
  message: string;
  messageType: MessageType;
  attachmentUrl?: string;
  readAt?: string;
  sentAt: string;
}

export interface SendMessageRequest {
  message: string;
  messageType: MessageType;
  attachmentUrl?: string;
}

export const chatApi = {
  createOrGetRoom: (bookingId: string) =>
    axiosClient.post<ApiResponse<ChatRoomResponse>>(`/chat/rooms/${bookingId}`),

  myRooms: (page = 0, size = 20) =>
    axiosClient.get<ApiResponse<PagedResponse<ChatRoomResponse>>>(
      `/chat/rooms/my?page=${page}&size=${size}`,
    ),

  getRoom: (roomId: string) =>
    axiosClient.get<ApiResponse<ChatRoomResponse>>(`/chat/rooms/${roomId}`),

  getMessages: (roomId: string, page = 0, size = 50) =>
    axiosClient.get<ApiResponse<PagedResponse<ChatMessageResponse>>>(
      `/chat/rooms/${roomId}/messages?page=${page}&size=${size}`,
    ),

  sendMessage: (roomId: string, data: SendMessageRequest) =>
    axiosClient.post<ApiResponse<ChatMessageResponse>>(
      `/chat/rooms/${roomId}/messages`,
      data,
    ),

  markRead: (roomId: string) =>
    axiosClient.post<ApiResponse<{ markedAsRead: number }>>(
      `/chat/rooms/${roomId}/read`,
    ),
};
