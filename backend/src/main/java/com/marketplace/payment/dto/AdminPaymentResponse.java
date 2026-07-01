package com.marketplace.payment.dto;

import com.marketplace.payment.entity.PaymentProviderType;
import com.marketplace.payment.entity.PaymentStatus;
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
public class AdminPaymentResponse {

    private UUID id;
    private UUID bookingId;
    private UUID buyerId;
    private String buyerName;
    private String buyerEmail;
    private UUID travellerId;
    private String travellerName;
    private String travellerEmail;
    private BigDecimal amount;
    private BigDecimal platformFee;
    private BigDecimal travellerPayout;
    private String currency;
    private PaymentProviderType paymentProvider;
    private String providerPaymentId;
    private String idempotencyKey;
    private PaymentStatus status;
    private String failureReason;
    private Instant paidAt;
    private Instant releasedAt;
    private Instant refundedAt;
    private Instant createdAt;
    private Instant updatedAt;
}