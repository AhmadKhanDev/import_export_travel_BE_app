package com.marketplace.notification.repository;

import com.marketplace.notification.dto.NotificationFilter;
import com.marketplace.notification.entity.Notification;
import com.marketplace.notification.entity.NotificationStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class NotificationSpecification {

    private NotificationSpecification() {
    }

    public static Specification<Notification> withFilter(NotificationFilter filter) {
        return Specification.where(byUserId(filter.getUserId()))
                .and(byStatus(filter.getStatus()))
                .and(byChannel(filter.getChannel()))
                .and(byType(filter.getType()))
                .and(byReferenceType(filter.getReferenceType()))
                .and(byReferenceId(filter.getReferenceId()))
                .and(unreadOnly(filter.isUnreadOnly()));
    }

    private static Specification<Notification> byUserId(UUID userId) {
        if (userId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    private static Specification<Notification> byStatus(com.marketplace.notification.entity.NotificationStatus status) {
        if (status == null) return null;
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private static Specification<Notification> byChannel(com.marketplace.notification.entity.NotificationChannel channel) {
        if (channel == null) return null;
        return (root, query, cb) -> cb.equal(root.get("channel"), channel);
    }

    private static Specification<Notification> byType(com.marketplace.notification.entity.NotificationType type) {
        if (type == null) return null;
        return (root, query, cb) -> cb.equal(root.get("notificationType"), type);
    }

    private static Specification<Notification> byReferenceType(com.marketplace.notification.entity.NotificationReferenceType referenceType) {
        if (referenceType == null) return null;
        return (root, query, cb) -> cb.equal(root.get("referenceType"), referenceType);
    }

    private static Specification<Notification> byReferenceId(UUID referenceId) {
        if (referenceId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("referenceId"), referenceId);
    }

    private static Specification<Notification> unreadOnly(boolean unreadOnly) {
        if (!unreadOnly) return null;
        return (root, query, cb) -> cb.notEqual(root.get("status"), NotificationStatus.READ);
    }
}