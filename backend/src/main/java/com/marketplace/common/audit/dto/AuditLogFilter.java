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
public class AuditLogFilter {

    private UUID actorUserId;
    private String action;
    private String entityType;
    private UUID entityId;
    private Instant createdFrom;
    private Instant createdTo;
}
