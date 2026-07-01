package com.marketplace.payment.provider;

import com.marketplace.payment.entity.PaymentProviderType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class FakePaymentProvider implements PaymentProvider {

    @Override
    public PaymentProviderType getProviderType() {
        return PaymentProviderType.FAKE;
    }

    @Override
    public PaymentProviderResult charge(PaymentProviderChargeRequest request) {
        String providerPaymentId = "FAKE-PAY-" + UUID.randomUUID();
        log.info("Fake payment charge successful: bookingId={}, amount={} {}, providerPaymentId={}",
                request.getBookingId(), request.getAmount(), request.getCurrency(), providerPaymentId);
        return PaymentProviderResult.builder()
                .success(true)
                .providerPaymentId(providerPaymentId)
                .rawResponse("{\"simulated\":true,\"action\":\"charge\"}")
                .build();
    }

    @Override
    public PaymentProviderResult release(PaymentProviderReleaseRequest request) {
        log.info("Fake payment release successful: paymentId={}, providerPaymentId={}, amount={} {}",
                request.getPaymentId(), request.getProviderPaymentId(), request.getAmount(), request.getCurrency());
        return PaymentProviderResult.builder()
                .success(true)
                .providerPaymentId(request.getProviderPaymentId())
                .rawResponse("{\"simulated\":true,\"action\":\"release\"}")
                .build();
    }

    @Override
    public PaymentProviderResult refund(PaymentProviderRefundRequest request) {
        log.info("Fake payment refund successful: paymentId={}, providerPaymentId={}, amount={} {}, reason={}",
                request.getPaymentId(), request.getProviderPaymentId(), request.getAmount(),
                request.getCurrency(), request.getReason());
        return PaymentProviderResult.builder()
                .success(true)
                .providerPaymentId(request.getProviderPaymentId())
                .rawResponse("{\"simulated\":true,\"action\":\"refund\"}")
                .build();
    }
}