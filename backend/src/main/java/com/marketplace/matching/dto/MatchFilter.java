package com.marketplace.matching.dto;

import com.marketplace.matching.entity.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchFilter {

    private UUID buyerRequestId;
    private UUID travellerTripId;
    private MatchStatus status;
}