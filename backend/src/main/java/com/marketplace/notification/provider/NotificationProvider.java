package com.marketplace.notification.provider;

import com.marketplace.notification.dto.NotificationSendRequest;
import com.marketplace.notification.dto.NotificationSendResult;
import com.marketplace.notification.entity.NotificationChannel;

public interface NotificationProvider {

    NotificationChannel getChannel();

    NotificationSendResult send(NotificationSendRequest request);
}