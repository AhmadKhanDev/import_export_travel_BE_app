package com.marketplace.payment.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProviderResult {

    private boolean success;
    private String providerPaymentId;
    private String failureReason;
    private String rawResponse;
}