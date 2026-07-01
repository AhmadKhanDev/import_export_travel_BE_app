package com.marketplace.listing.dto;

import com.marketplace.listing.entity.BuyerRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerRequestFilter {

    private String sourceCountry;
    private String sourceCity;
    private String destinationCountry;
    private String destinationCity;
    private String itemCategory;
    private BuyerRequestStatus status;
}