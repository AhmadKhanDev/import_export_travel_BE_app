package com.marketplace.verification.dto;

import com.marketplace.verification.entity.DeliveryCodeStatus;
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
public class GenerateDeliveryCodeResponse {

    private UUID id;
    private UUID bookingId;
    private String code;
    private DeliveryCodeStatus status;
    private Instant expiresAt;
    private Instant createdAt;
    private String message;
}
