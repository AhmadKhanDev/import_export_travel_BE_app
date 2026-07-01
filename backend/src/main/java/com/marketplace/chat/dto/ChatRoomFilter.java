package com.marketplace.chat.dto;

import com.marketplace.chat.entity.ChatRoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomFilter {

    private UUID buyerId;
    private UUID travellerId;
    private UUID bookingId;
    private ChatRoomStatus status;
}
