package com.marketplace.notification.provider;

import com.marketplace.notification.dto.NotificationSendRequest;
import com.marketplace.notification.dto.NotificationSendResult;
import com.marketplace.notification.entity.NotificationChannel;
import com.marketplace.notification.entity.NotificationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmsNotificationProvider implements NotificationProvider {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public NotificationSendResult send(NotificationSendRequest request) {
        log.info("SMS notification: userId={}, type={}, title={}",
                request.getUserId(), request.getNotificationType(), request.getTitle());
        // TODO: integrate AWS SNS or Twilio.
        return NotificationSendResult.builder().status(NotificationStatus.SENT).build();
    }
}