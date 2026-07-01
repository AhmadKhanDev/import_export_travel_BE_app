package com.marketplace.notification.dto;

import com.marketplace.notification.entity.NotificationChannel;
import com.marketplace.notification.entity.NotificationReferenceType;
import com.marketplace.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSendNotificationRequest {
    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Channel is required")
    private NotificationChannel channel;

    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Message is required")
    @Size(max = 5000, message = "Message must not exceed 5000 characters")
    private String message;

    private NotificationReferenceType referenceType;
    private UUID referenceId;
}