package com.marketplace.common.audit.service;

import com.marketplace.common.audit.dto.AuditLogFilter;
import com.marketplace.common.audit.dto.AuditLogResponse;
import com.marketplace.common.audit.entity.AuditLog;
import com.marketplace.common.audit.repository.AuditLogRepository;
import com.marketplace.common.audit.repository.AuditLogSpecification;
import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.common.security.SecurityUtils;
import com.marketplace.user.entity.User;
import com.marketplace.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void log(String action, String entityType, UUID entityId, UUID actorUserId, String metadata) {
        User actor = actorUserId != null ? userRepository.findById(actorUserId).orElse(null) : null;
        String ipAddress = resolveClientIp();
        String userAgent = resolveUserAgent();

        AuditLog auditLog = AuditLog.builder()
                .id(UUID.randomUUID())
                .actorUser(actor)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .metadata(metadata)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(Instant.now())
                .build();
        auditLogRepository.save(auditLog);
        log.debug("Audit log created: action={}, entityType={}, entityId={}", action, entityType, entityId);
    }

    @Transactional
    public void logAdminAction(String action, String entityType, UUID entityId, String metadata) {
        log(action, entityType, entityId, SecurityUtils.getCurrentUserId(), metadata);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(AuditLogFilter filter, Pageable pageable) {
        return auditLogRepository.findAll(AuditLogSpecification.withFilter(filter), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AuditLogResponse getById(UUID auditLogId) {
        return toResponse(getAuditLog(auditLogId));
    }

    private AuditLog getAuditLog(UUID auditLogId) {
        return auditLogRepository.findById(auditLogId)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log not found"));
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        User actor = auditLog.getActorUser();
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .actorUserId(actor != null ? actor.getId() : null)
                .actorName(actor != null ? actor.getFullName() : null)
                .actorEmail(actor != null ? actor.getEmail() : null)
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .metadata(auditLog.getMetadata())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }

    private String resolveClientIp() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String resolveUserAgent() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.length() > 512) {
            return userAgent.substring(0, 512);
        }
        return userAgent;
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }
}
