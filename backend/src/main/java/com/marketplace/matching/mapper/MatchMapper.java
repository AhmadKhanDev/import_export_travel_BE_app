package com.marketplace.matching.mapper;

import com.marketplace.listing.entity.BuyerRequest;
import com.marketplace.listing.entity.TravellerTrip;
import com.marketplace.matching.dto.AdminMatchResponse;
import com.marketplace.matching.dto.MatchResponse;
import com.marketplace.matching.entity.Match;
import org.springframework.stereotype.Component;

@Component
public class MatchMapper {

    public MatchResponse toResponse(Match match) {
        BuyerRequest buyerRequest = match.getBuyerRequest();
        TravellerTrip travellerTrip = match.getTravellerTrip();

        return MatchResponse.builder()
                .id(match.getId())
                .buyerRequestId(buyerRequest.getId())
                .buyerRequestTitle(buyerRequest.getTitle())
                .buyerId(buyerRequest.getBuyer().getId())
                .buyerName(buyerRequest.getBuyer().getFullName())
                .travellerTripId(travellerTrip.getId())
                .travellerId(travellerTrip.getTraveller().getId())
                .travellerName(travellerTrip.getTraveller().getFullName())
                .sourceCountry(buyerRequest.getSourceCountry())
                .sourceCity(buyerRequest.getSourceCity())
                .destinationCountry(buyerRequest.getDestinationCountry())
                .destinationCity(buyerRequest.getDestinationCity())
                .itemCategory(buyerRequest.getItemCategory())
                .neededBefore(buyerRequest.getNeededBefore())
                .travelDate(travellerTrip.getTravelDate())
                .matchScore(match.getMatchScore())
                .status(match.getStatus())
                .createdAt(match.getCreatedAt())
                .updatedAt(match.getUpdatedAt())
                .build();
    }

    public AdminMatchResponse toAdminResponse(Match match) {
        BuyerRequest buyerRequest = match.getBuyerRequest();
        TravellerTrip travellerTrip = match.getTravellerTrip();

        return AdminMatchResponse.builder()
                .id(match.getId())
                .buyerRequestId(buyerRequest.getId())
                .buyerRequestTitle(buyerRequest.getTitle())
                .buyerId(buyerRequest.getBuyer().getId())
                .buyerName(buyerRequest.getBuyer().getFullName())
                .buyerEmail(buyerRequest.getBuyer().getEmail())
                .travellerTripId(travellerTrip.getId())
                .travellerId(travellerTrip.getTraveller().getId())
                .travellerName(travellerTrip.getTraveller().getFullName())
                .travellerEmail(travellerTrip.getTraveller().getEmail())
                .sourceCountry(buyerRequest.getSourceCountry())
                .sourceCity(buyerRequest.getSourceCity())
                .destinationCountry(buyerRequest.getDestinationCountry())
                .destinationCity(buyerRequest.getDestinationCity())
                .itemCategory(buyerRequest.getItemCategory())
                .neededBefore(buyerRequest.getNeededBefore())
                .travelDate(travellerTrip.getTravelDate())
                .matchScore(match.getMatchScore())
                .status(match.getStatus())
                .createdAt(match.getCreatedAt())
                .updatedAt(match.getUpdatedAt())
                .build();
    }
}