package com.marketplace.chat.dto;

import com.marketplace.chat.entity.ChatRoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {

    private UUID id;
    private UUID bookingId;
    private UUID buyerId;
    private String buyerName;
    private UUID travellerId;
    private String travellerName;
    private ChatRoomStatus status;
    private String lastMessage;
    private Instant lastMessageAt;
    private long unreadCount;
    private Instant createdAt;
    private Instant updatedAt;
}
