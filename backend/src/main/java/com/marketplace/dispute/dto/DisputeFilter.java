package com.marketplace.dispute.dto;

import com.marketplace.dispute.entity.DisputeReason;
import com.marketplace.dispute.entity.DisputeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeFilter {

    private DisputeStatus status;
    private UUID raisedByUserId;
    private UUID bookingId;
    private DisputeReason reason;
}
