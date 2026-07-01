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
public class AdminChatRoomResponse {

    private UUID id;
    private UUID bookingId;
    private UUID buyerId;
    private String buyerName;
    private String buyerEmail;
    private UUID travellerId;
    private String travellerName;
    private String travellerEmail;
    private ChatRoomStatus status;
    private String lastMessage;
    private Instant lastMessageAt;
    private Instant createdAt;
    private Instant updatedAt;
}
