package com.marketplace.realtime.service;

import java.util.UUID;

public final class RealtimeChannels {

    public static final String TRACKING_BOOKING = "tracking-booking";
    public static final String CHAT_ROOM = "chat-room";
    public static final String CHAT_INBOX = "chat-inbox";

    private RealtimeChannels() {
    }

    public static String trackingBookingKey(UUID bookingId) {
        return TRACKING_BOOKING + ":" + bookingId;
    }

    public static String chatRoomKey(UUID roomId) {
        return CHAT_ROOM + ":" + roomId;
    }

    public static String chatInboxKey(UUID userId) {
        return CHAT_INBOX + ":" + userId;
    }
}
