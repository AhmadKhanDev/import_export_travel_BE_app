package com.marketplace.chat.dto;

import com.marketplace.chat.entity.MessageType;
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
public class ChatMessageResponse {

    private UUID id;
    private UUID chatRoomId;
    private UUID senderId;
    private String senderName;
    private String message;
    private MessageType messageType;
    private String attachmentUrl;
    private Instant sentAt;
    private Instant readAt;
    private Instant createdAt;
}
