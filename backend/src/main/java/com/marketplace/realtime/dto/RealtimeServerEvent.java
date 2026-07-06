package com.marketplace.realtime.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealtimeServerEvent {

    private String type;
    private String channel;
    private String bookingId;
    private String roomId;
    private Object payload;
    private Instant timestamp;
}
