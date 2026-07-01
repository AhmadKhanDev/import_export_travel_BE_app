package com.marketplace.verification.dto;

import com.marketplace.booking.entity.BookingStatus;
import com.marketplace.payment.entity.PaymentStatus;
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
public class DeliveryVerificationResultResponse {

    private UUID bookingId;
    private UUID paymentId;
    private DeliveryCodeStatus codeStatus;
    private BookingStatus bookingStatus;
    private PaymentStatus paymentStatus;
    private Instant verifiedAt;
    private Instant paymentReleasedAt;
    private String message;
}
