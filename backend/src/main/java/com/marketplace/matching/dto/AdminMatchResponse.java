package com.marketplace.matching.dto;

import com.marketplace.matching.entity.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMatchResponse {

    private UUID id;
    private UUID buyerRequestId;
    private String buyerRequestTitle;
    private UUID buyerId;
    private String buyerName;
    private String buyerEmail;
    private UUID travellerTripId;
    private UUID travellerId;
    private String travellerName;
    private String travellerEmail;
    private String sourceCountry;
    private String sourceCity;
    private String destinationCountry;
    private String destinationCity;
    private String itemCategory;
    private Instant neededBefore;
    private Instant travelDate;
    private BigDecimal matchScore;
    private MatchStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}