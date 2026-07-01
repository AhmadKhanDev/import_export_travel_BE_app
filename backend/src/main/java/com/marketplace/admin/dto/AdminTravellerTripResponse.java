package com.marketplace.admin.dto;

import com.marketplace.listing.entity.TravellerTripStatus;
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
public class AdminTravellerTripResponse {

    private UUID id;
    private UUID travellerId;
    private String travellerName;
    private String travellerEmail;
    private String sourceCountry;
    private String sourceCity;
    private String destinationCountry;
    private String destinationCity;
    private Instant travelDate;
    private BigDecimal availableCapacityKg;
    private TravellerTripStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
