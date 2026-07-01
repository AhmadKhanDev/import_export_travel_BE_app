package com.marketplace.common.audit.repository;

import com.marketplace.common.audit.dto.AuditLogFilter;
import com.marketplace.common.audit.entity.AuditLog;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class AuditLogSpecification {

    private AuditLogSpecification() {
    }

    public static Specification<AuditLog> withFilter(AuditLogFilter filter) {
        return Specification.where(byActorUserId(filter.getActorUserId()))
                .and(byAction(filter.getAction()))
                .and(byEntityType(filter.getEntityType()))
                .and(byEntityId(filter.getEntityId()))
                .and(createdFrom(filter.getCreatedFrom()))
                .and(createdTo(filter.getCreatedTo()));
    }

    private static Specification<AuditLog> byActorUserId(UUID actorUserId) {
        if (actorUserId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("actorUser").get("id"), actorUserId);
    }

    private static Specification<AuditLog> byAction(String action) {
        if (action == null || action.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("action"), action.trim());
    }

    private static Specification<AuditLog> byEntityType(String entityType) {
        if (entityType == null || entityType.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("entityType"), entityType.trim());
    }

    private static Specification<AuditLog> byEntityId(UUID entityId) {
        if (entityId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("entityId"), entityId);
    }

    private static Specification<AuditLog> createdFrom(java.time.Instant createdFrom) {
        if (createdFrom == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom);
    }

    private static Specification<AuditLog> createdTo(java.time.Instant createdTo) {
        if (createdTo == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), createdTo);
    }
}
