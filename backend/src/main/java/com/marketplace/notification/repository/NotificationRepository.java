package com.marketplace.notification.repository;

import com.marketplace.notification.entity.Notification;
import com.marketplace.notification.entity.NotificationChannel;
import com.marketplace.notification.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID>, JpaSpecificationExecutor<Notification> {

    long countByUser_IdAndStatusAndChannel(UUID userId, NotificationStatus status, NotificationChannel channel);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Notification n
            set n.status = com.marketplace.notification.entity.NotificationStatus.READ,
                n.readAt = :readAt
            where n.user.id = :userId
              and n.channel = com.marketplace.notification.entity.NotificationChannel.IN_APP
              and n.status <> com.marketplace.notification.entity.NotificationStatus.READ
            """)
    int markAllInAppAsRead(@Param("userId") UUID userId, @Param("readAt") Instant readAt);
}