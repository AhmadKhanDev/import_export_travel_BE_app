package com.marketplace.payment.dto;

import com.marketplace.payment.entity.PaymentStatus;
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
public class PaymentFilter {

    private UUID bookingId;
    private UUID buyerId;
    private UUID travellerId;
    private PaymentStatus status;
    private String providerPaymentId;
    private Instant createdFrom;
    private Instant createdTo;
}