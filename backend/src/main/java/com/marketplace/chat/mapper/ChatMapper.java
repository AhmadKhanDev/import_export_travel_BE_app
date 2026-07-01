package com.marketplace.chat.mapper;

import com.marketplace.chat.dto.AdminChatRoomResponse;
import com.marketplace.chat.dto.ChatMessageResponse;
import com.marketplace.chat.dto.ChatRoomResponse;
import com.marketplace.chat.entity.ChatMessage;
import com.marketplace.chat.entity.ChatRoom;
import com.marketplace.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ChatMapper {

    public ChatRoomResponse toRoomResponse(ChatRoom room, String lastMessage, java.time.Instant lastMessageAt,
                                         long unreadCount) {
        return ChatRoomResponse.builder()
                .id(room.getId())
                .bookingId(room.getBooking().getId())
                .buyerId(room.getBuyer().getId())
                .buyerName(room.getBuyer().getFullName())
                .travellerId(room.getTraveller().getId())
                .travellerName(room.getTraveller().getFullName())
                .status(room.getStatus())
                .lastMessage(lastMessage)
                .lastMessageAt(lastMessageAt)
                .unreadCount(unreadCount)
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }

    public AdminChatRoomResponse toAdminRoomResponse(ChatRoom room, String lastMessage,
                                                     java.time.Instant lastMessageAt) {
        return AdminChatRoomResponse.builder()
                .id(room.getId())
                .bookingId(room.getBooking().getId())
                .buyerId(room.getBuyer().getId())
                .buyerName(room.getBuyer().getFullName())
                .buyerEmail(room.getBuyer().getEmail())
                .travellerId(room.getTraveller().getId())
                .travellerName(room.getTraveller().getFullName())
                .travellerEmail(room.getTraveller().getEmail())
                .status(room.getStatus())
                .lastMessage(lastMessage)
                .lastMessageAt(lastMessageAt)
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }

    public ChatMessageResponse toMessageResponse(ChatMessage message) {
        User sender = message.getSender();
        return ChatMessageResponse.builder()
                .id(message.getId())
                .chatRoomId(message.getChatRoom().getId())
                .senderId(sender != null ? sender.getId() : null)
                .senderName(sender != null ? sender.getFullName() : "System")
                .message(message.getMessage())
                .messageType(message.getMessageType())
                .attachmentUrl(message.getAttachmentUrl())
                .sentAt(message.getSentAt())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
