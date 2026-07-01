package com.marketplace.listing.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
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
public class CreateBuyerRequestRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotBlank(message = "Item category is required")
    @Size(max = 100, message = "Item category must not exceed 100 characters")
    private String itemCategory;

    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private String brand;

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

    @Positive(message = "Estimated item price must be positive")
    private BigDecimal estimatedItemPrice;

    @Future(message = "Needed before date must be in the future")
    private Instant neededBefore;
}