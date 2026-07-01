package com.marketplace.notification.dto;

import com.marketplace.notification.entity.NotificationChannel;
import com.marketplace.notification.entity.NotificationReferenceType;
import com.marketplace.notification.entity.NotificationStatus;
import com.marketplace.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationFilter {
    private UUID userId;
    private NotificationStatus status;
    private NotificationChannel channel;
    private NotificationType type;
    private NotificationReferenceType referenceType;
    private UUID referenceId;
    private boolean unreadOnly;
}