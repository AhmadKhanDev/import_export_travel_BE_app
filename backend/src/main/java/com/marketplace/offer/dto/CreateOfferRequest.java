package com.marketplace.offer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
public class CreateOfferRequest {

    @NotNull(message = "Buyer request ID is required")
    private UUID buyerRequestId;

    @NotNull(message = "Traveller trip ID is required")
    private UUID travellerTripId;

    private UUID matchId;

    @NotNull(message = "Item price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Item price must be zero or positive")
    private BigDecimal itemPrice;

    @NotNull(message = "Traveller fee is required")
    @Positive(message = "Traveller fee must be positive")
    private BigDecimal travellerFee;

    @NotNull(message = "Platform fee is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Platform fee must be zero or positive")
    private BigDecimal platformFee;

    @Size(max = 10, message = "Currency must not exceed 10 characters")
    private String currency;

    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    private String message;

    @Future(message = "Expiry date must be in the future")
    private Instant expiresAt;
}