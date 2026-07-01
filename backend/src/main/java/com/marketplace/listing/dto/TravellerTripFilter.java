package com.marketplace.listing.dto;

import com.marketplace.listing.entity.TravellerTripStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravellerTripFilter {

    private String sourceCountry;
    private String sourceCity;
    private String destinationCountry;
    private String destinationCity;
    private TravellerTripStatus status;
    private Instant travelDateFrom;
    private Instant travelDateTo;
}