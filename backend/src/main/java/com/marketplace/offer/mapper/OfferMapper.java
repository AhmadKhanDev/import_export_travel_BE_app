package com.marketplace.offer.mapper;

import com.marketplace.offer.dto.OfferResponse;
import com.marketplace.offer.entity.Offer;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OfferMapper {

    public OfferResponse toResponse(Offer offer) {
        UUID matchId = offer.getMatch() != null ? offer.getMatch().getId() : null;
        return OfferResponse.builder()
                .id(offer.getId())
                .buyerRequestId(offer.getBuyerRequest().getId())
                .buyerRequestTitle(offer.getBuyerRequest().getTitle())
                .travellerTripId(offer.getTravellerTrip().getId())
                .matchId(matchId)
                .buyerId(offer.getBuyer().getId())
                .buyerName(offer.getBuyer().getFullName())
                .travellerId(offer.getTraveller().getId())
                .travellerName(offer.getTraveller().getFullName())
                .itemPrice(offer.getItemPrice())
                .travellerFee(offer.getTravellerFee())
                .platformFee(offer.getPlatformFee())
                .totalAmount(offer.getTotalAmount())
                .currency(offer.getCurrency())
                .message(offer.getMessage())
                .status(offer.getStatus())
                .expiresAt(offer.getExpiresAt())
                .createdAt(offer.getCreatedAt())
                .updatedAt(offer.getUpdatedAt())
                .build();
    }
}