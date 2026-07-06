package com.marketplace.realtime.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealtimeClientMessage {

    private String type;
    private String channel;
    private String bookingId;
    private String roomId;
}
