package com.marketplace.realtime.service;

import com.marketplace.chat.dto.ChatMessageResponse;
import com.marketplace.chat.dto.ChatRoomResponse;
import com.marketplace.realtime.dto.RealtimeServerEvent;
import com.marketplace.tracking.dto.TrackingSessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RealtimeEventPublisher {

    private final RealtimeSessionRegistry sessionRegistry;

    public void publishTrackingUpdate(UUID bookingId, TrackingSessionResponse payload) {
        sessionRegistry.publish(
                RealtimeChannels.trackingBookingKey(bookingId),
                RealtimeServerEvent.builder()
                        .type("TRACKING_UPDATED")
                        .channel(RealtimeChannels.TRACKING_BOOKING)
                        .bookingId(bookingId.toString())
                        .payload(payload)
                        .timestamp(Instant.now())
                        .build());
    }

    public void publishChatMessage(UUID roomId, ChatMessageResponse payload) {
        sessionRegistry.publish(
                RealtimeChannels.chatRoomKey(roomId),
                RealtimeServerEvent.builder()
                        .type("CHAT_MESSAGE_CREATED")
                        .channel(RealtimeChannels.CHAT_ROOM)
                        .roomId(roomId.toString())
                        .payload(payload)
                        .timestamp(Instant.now())
                        .build());
    }

    public void publishChatRoomUpdate(UUID userId, UUID roomId, ChatRoomResponse payload) {
        sessionRegistry.publish(
                RealtimeChannels.chatInboxKey(userId),
                RealtimeServerEvent.builder()
                        .type("CHAT_ROOM_UPDATED")
                        .channel(RealtimeChannels.CHAT_INBOX)
                        .roomId(roomId.toString())
                        .payload(payload)
                        .timestamp(Instant.now())
                        .build());
    }
}
