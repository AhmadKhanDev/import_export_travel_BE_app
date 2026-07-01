package com.marketplace.offer.dto;

import com.marketplace.offer.entity.OfferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferFilter {

    private UUID buyerId;
    private UUID travellerId;
    private UUID buyerRequestId;
    private UUID travellerTripId;
    private OfferStatus status;
}