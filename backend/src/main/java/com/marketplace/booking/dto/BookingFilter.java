package com.marketplace.booking.dto;

import com.marketplace.booking.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingFilter {

    private UUID buyerId;
    private UUID travellerId;
    private BookingStatus status;
}