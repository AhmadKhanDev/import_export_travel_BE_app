package com.marketplace.notification.dto;

import com.marketplace.notification.entity.NotificationChannel;
import com.marketplace.notification.entity.NotificationReferenceType;
import com.marketplace.notification.entity.NotificationStatus;
import com.marketplace.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private UUID id;
    private UUID userId;
    private NotificationType notificationType;
    private String title;
    private String message;
    private NotificationChannel channel;
    private NotificationStatus status;
    private NotificationReferenceType referenceType;
    private UUID referenceId;
    private String failureReason;
    private Instant sentAt;
    private Instant readAt;
    private Instant createdAt;
    private Instant updatedAt;
}