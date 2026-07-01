package com.marketplace.notification.dto;

import com.marketplace.notification.entity.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSendResult {
    private NotificationStatus status;
    private String failureReason;
}