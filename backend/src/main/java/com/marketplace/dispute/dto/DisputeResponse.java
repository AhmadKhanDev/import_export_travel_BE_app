package com.marketplace.dispute.dto;

import com.marketplace.dispute.entity.DisputeReason;
import com.marketplace.dispute.entity.DisputeStatus;
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
public class DisputeResponse {

    private UUID id;
    private UUID bookingId;
    private UUID raisedByUserId;
    private String raisedByUserName;
    private UUID buyerId;
    private String buyerName;
    private UUID travellerId;
    private String travellerName;
    private DisputeReason reason;
    private String description;
    private DisputeStatus status;
    private UUID resolvedByAdminId;
    private String resolvedByAdminName;
    private String resolutionNote;
    private Instant createdAt;
    private Instant resolvedAt;
    private Instant updatedAt;
}
