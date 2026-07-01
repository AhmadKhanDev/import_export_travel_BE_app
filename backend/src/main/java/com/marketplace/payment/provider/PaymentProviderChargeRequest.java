package com.marketplace.payment.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProviderChargeRequest {

    private UUID bookingId;
    private UUID buyerId;
    private UUID travellerId;
    private BigDecimal amount;
    private String currency;
    private String idempotencyKey;
}