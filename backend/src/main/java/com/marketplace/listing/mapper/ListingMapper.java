package com.marketplace.listing.mapper;

import com.marketplace.listing.dto.BuyerRequestResponse;
import com.marketplace.listing.dto.CreateBuyerRequestRequest;
import com.marketplace.listing.dto.CreateTravellerTripRequest;
import com.marketplace.listing.dto.TravellerTripResponse;
import com.marketplace.listing.dto.UpdateBuyerRequestRequest;
import com.marketplace.listing.dto.UpdateTravellerTripRequest;
import com.marketplace.listing.entity.BuyerRequest;
import com.marketplace.listing.entity.BuyerRequestStatus;
import com.marketplace.listing.entity.TravellerTrip;
import com.marketplace.listing.entity.TravellerTripStatus;
import com.marketplace.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ListingMapper {

    public BuyerRequest toBuyerRequest(CreateBuyerRequestRequest request, User buyer) {
        return BuyerRequest.builder()
                .id(UUID.randomUUID())
                .buyer(buyer)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .itemCategory(request.getItemCategory().trim())
                .brand(request.getBrand())
                .sourceCountry(request.getSourceCountry().trim())
                .sourceCity(request.getSourceCity().trim())
                .destinationCountry(request.getDestinationCountry().trim())
                .destinationCity(request.getDestinationCity().trim())
                .estimatedItemPrice(request.getEstimatedItemPrice())
                .neededBefore(request.getNeededBefore())
                .status(BuyerRequestStatus.DRAFT)
                .build();
    }

    public void updateBuyerRequest(BuyerRequest entity, UpdateBuyerRequestRequest request) {
        entity.setTitle(request.getTitle().trim());
        entity.setDescription(request.getDescription());
        entity.setItemCategory(request.getItemCategory().trim());
        entity.setBrand(request.getBrand());
        entity.setSourceCountry(request.getSourceCountry().trim());
        entity.setSourceCity(request.getSourceCity().trim());
        entity.setDestinationCountry(request.getDestinationCountry().trim());
        entity.setDestinationCity(request.getDestinationCity().trim());
        entity.setEstimatedItemPrice(request.getEstimatedItemPrice());
        entity.setNeededBefore(request.getNeededBefore());
    }

    public BuyerRequestResponse toBuyerRequestResponse(BuyerRequest entity) {
        return BuyerRequestResponse.builder()
                .id(entity.getId())
                .buyerId(entity.getBuyer().getId())
                .buyerName(entity.getBuyer().getFullName())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .itemCategory(entity.getItemCategory())
                .brand(entity.getBrand())
                .sourceCountry(entity.getSourceCountry())
                .sourceCity(entity.getSourceCity())
                .destinationCountry(entity.getDestinationCountry())
                .destinationCity(entity.getDestinationCity())
                .estimatedItemPrice(entity.getEstimatedItemPrice())
                .status(entity.getStatus())
                .neededBefore(entity.getNeededBefore())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public TravellerTrip toTravellerTrip(CreateTravellerTripRequest request, User traveller) {
        return TravellerTrip.builder()
                .id(UUID.randomUUID())
                .traveller(traveller)
                .sourceCountry(request.getSourceCountry().trim())
                .sourceCity(request.getSourceCity().trim())
                .destinationCountry(request.getDestinationCountry().trim())
                .destinationCity(request.getDestinationCity().trim())
                .travelDate(request.getTravelDate())
                .availableCapacityKg(request.getAvailableCapacityKg())
                .allowedItemTypes(request.getAllowedItemTypes())
                .status(TravellerTripStatus.DRAFT)
                .build();
    }

    public void updateTravellerTrip(TravellerTrip entity, UpdateTravellerTripRequest request) {
        entity.setSourceCountry(request.getSourceCountry().trim());
        entity.setSourceCity(request.getSourceCity().trim());
        entity.setDestinationCountry(request.getDestinationCountry().trim());
        entity.setDestinationCity(request.getDestinationCity().trim());
        entity.setTravelDate(request.getTravelDate());
        entity.setAvailableCapacityKg(request.getAvailableCapacityKg());
        entity.setAllowedItemTypes(request.getAllowedItemTypes());
    }

    public TravellerTripResponse toTravellerTripResponse(TravellerTrip entity) {
        return TravellerTripResponse.builder()
                .id(entity.getId())
                .travellerId(entity.getTraveller().getId())
                .travellerName(entity.getTraveller().getFullName())
                .sourceCountry(entity.getSourceCountry())
                .sourceCity(entity.getSourceCity())
                .destinationCountry(entity.getDestinationCountry())
                .destinationCity(entity.getDestinationCity())
                .travelDate(entity.getTravelDate())
                .availableCapacityKg(entity.getAvailableCapacityKg())
                .allowedItemTypes(entity.getAllowedItemTypes())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}