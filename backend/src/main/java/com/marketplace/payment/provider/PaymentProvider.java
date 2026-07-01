package com.marketplace.payment.provider;

import com.marketplace.payment.entity.PaymentProviderType;

public interface PaymentProvider {

    PaymentProviderType getProviderType();

    PaymentProviderResult charge(PaymentProviderChargeRequest request);

    PaymentProviderResult release(PaymentProviderReleaseRequest request);

    PaymentProviderResult refund(PaymentProviderRefundRequest request);
}