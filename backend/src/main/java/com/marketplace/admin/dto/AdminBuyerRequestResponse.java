package com.marketplace.admin.dto;

import com.marketplace.listing.entity.BuyerRequestStatus;
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
public class AdminBuyerRequestResponse {

    private UUID id;
    private UUID buyerId;
    private String buyerName;
    private String buyerEmail;
    private String title;
    private String description;
    private String itemCategory;
    private String brand;
    private String sourceCountry;
    private String sourceCity;
    private String destinationCountry;
    private String destinationCity;
    private BigDecimal estimatedItemPrice;
    private BuyerRequestStatus status;
    private Instant neededBefore;
    private Instant createdAt;
    private Instant updatedAt;
}
