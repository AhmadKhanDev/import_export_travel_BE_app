package com.marketplace.common.audit.dto;

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
public class AuditLogResponse {

    private UUID id;
    private UUID actorUserId;
    private String actorName;
    private String actorEmail;
    private String action;
    private String entityType;
    private UUID entityId;
    private String metadata;
    private String ipAddress;
    private String userAgent;
    private Instant createdAt;
}
