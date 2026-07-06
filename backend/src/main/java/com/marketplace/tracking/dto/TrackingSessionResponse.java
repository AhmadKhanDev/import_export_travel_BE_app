package com.marketplace.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingSessionResponse {

    private UUID bookingId;
    private boolean active;
    private boolean shareable;
    private boolean hasLocation;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal accuracyMeters;
    private BigDecimal headingDegrees;
    private BigDecimal speedKph;
    private Instant startedAt;
    private Instant stoppedAt;
    private Instant lastLocationAt;
    private String stopReason;
}
