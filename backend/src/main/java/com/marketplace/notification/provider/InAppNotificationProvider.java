package com.marketplace.notification.provider;

import com.marketplace.notification.dto.NotificationSendRequest;
import com.marketplace.notification.dto.NotificationSendResult;
import com.marketplace.notification.entity.NotificationChannel;
import com.marketplace.notification.entity.NotificationStatus;
import org.springframework.stereotype.Component;

@Component
public class InAppNotificationProvider implements NotificationProvider {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.IN_APP;
    }

    @Override
    public NotificationSendResult send(NotificationSendRequest request) {
        return NotificationSendResult.builder()
                .status(NotificationStatus.SENT)
                .build();
    }
}