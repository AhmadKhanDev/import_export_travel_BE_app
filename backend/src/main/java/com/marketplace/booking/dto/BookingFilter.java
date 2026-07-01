package com.marketplace.booking.dto;

import com.marketplace.booking.entity.BookingStatus;
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
public class BookingFilter {

    private UUID buyerId;
    private UUID travellerId;
    private UUID buyerRequestId;
    private UUID travellerTripId;
    private BookingStatus status;
    private Instant createdFrom;
    private Instant createdTo;
}