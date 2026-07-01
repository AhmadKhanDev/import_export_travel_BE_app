package com.marketplace.listing.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTravellerTripRequest {

    @NotBlank(message = "Source country is required")
    @Size(max = 100, message = "Source country must not exceed 100 characters")
    private String sourceCountry;

    @NotBlank(message = "Source city is required")
    @Size(max = 100, message = "Source city must not exceed 100 characters")
    private String sourceCity;

    @NotBlank(message = "Destination country is required")
    @Size(max = 100, message = "Destination country must not exceed 100 characters")
    private String destinationCountry;

    @NotBlank(message = "Destination city is required")
    @Size(max = 100, message = "Destination city must not exceed 100 characters")
    private String destinationCity;

    @NotNull(message = "Travel date is required")
    @Future(message = "Travel date must be in the future")
    private Instant travelDate;

    @Positive(message = "Available capacity must be positive")
    private BigDecimal availableCapacityKg;

    @Size(max = 2000, message = "Allowed item types must not exceed 2000 characters")
    private String allowedItemTypes;
}