package com.marketplace.payment.provider;

import com.marketplace.common.exception.BadRequestException;
import com.marketplace.payment.entity.PaymentProviderType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentProviderFactory {

    private final Map<PaymentProviderType, PaymentProvider> providers;

    public PaymentProviderFactory(List<PaymentProvider> providerList) {
        this.providers = new EnumMap<>(PaymentProviderType.class);
        for (PaymentProvider provider : providerList) {
            providers.put(provider.getProviderType(), provider);
        }
    }

    public PaymentProvider getProvider(PaymentProviderType type) {
        PaymentProvider provider = providers.get(type);
        if (provider == null) {
            throw new BadRequestException("Payment provider not available: " + type);
        }
        return provider;
    }
}