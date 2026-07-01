package com.marketplace.notification.dto;

import com.marketplace.notification.entity.NotificationChannel;
import com.marketplace.notification.entity.NotificationReferenceType;
import com.marketplace.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationCommand {
    private UUID userId;
    private NotificationType notificationType;
    private String title;
    private String message;
    private List<NotificationChannel> channels;
    private NotificationReferenceType referenceType;
    private UUID referenceId;
}