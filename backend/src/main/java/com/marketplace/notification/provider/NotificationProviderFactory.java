package com.marketplace.notification.provider;

import com.marketplace.common.exception.BadRequestException;
import com.marketplace.notification.entity.NotificationChannel;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class NotificationProviderFactory {

    private final Map<NotificationChannel, NotificationProvider> providers;

    public NotificationProviderFactory(List<NotificationProvider> providerList) {
        this.providers = new EnumMap<>(NotificationChannel.class);
        for (NotificationProvider provider : providerList) {
            providers.put(provider.getChannel(), provider);
        }
    }

    public NotificationProvider getProvider(NotificationChannel channel) {
        NotificationProvider provider = providers.get(channel);
        if (provider == null) {
            throw new BadRequestException("Notification channel not available: " + channel);
        }
        return provider;
    }
}