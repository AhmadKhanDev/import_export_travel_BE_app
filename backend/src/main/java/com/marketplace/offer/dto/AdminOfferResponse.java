package com.marketplace.offer.dto;

import com.marketplace.offer.entity.OfferStatus;
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
public class AdminOfferResponse {

    private UUID id;
    private UUID buyerRequestId;
    private String buyerRequestTitle;
    private UUID travellerTripId;
    private UUID matchId;
    private UUID buyerId;
    private String buyerName;
    private String buyerEmail;
    private UUID travellerId;
    private String travellerName;
    private String travellerEmail;
    private BigDecimal itemPrice;
    private BigDecimal travellerFee;
    private BigDecimal platformFee;
    private BigDecimal totalAmount;
    private String currency;
    private String message;
    private OfferStatus status;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;
}