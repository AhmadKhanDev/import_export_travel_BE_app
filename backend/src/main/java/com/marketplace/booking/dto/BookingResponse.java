package com.marketplace.booking.dto;

import com.marketplace.booking.entity.BookingStatus;
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
public class BookingResponse {

    private UUID id;
    private UUID offerId;
    private UUID buyerId;
    private String buyerName;
    private UUID travellerId;
    private String travellerName;
    private UUID buyerRequestId;
    private String buyerRequestTitle;
    private UUID travellerTripId;
    private String sourceCountry;
    private String sourceCity;
    private String destinationCountry;
    private String destinationCity;
    private BigDecimal itemPrice;
    private BigDecimal travellerFee;
    private BigDecimal platformFee;
    private BigDecimal totalAmount;
    private String currency;
    private BookingStatus status;
    private Instant acceptedAt;
    private Instant deliveredAt;
    private Instant completedAt;
    private Instant cancelledAt;
    private Instant createdAt;
    private Instant updatedAt;
}