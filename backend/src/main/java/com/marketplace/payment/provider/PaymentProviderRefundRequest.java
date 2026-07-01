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
public class PaymentProviderRefundRequest {

    private UUID paymentId;
    private String providerPaymentId;
    private BigDecimal amount;
    private String currency;
    private String reason;
}