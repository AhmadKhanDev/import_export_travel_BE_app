package com.marketplace.admin.mapper;

import com.marketplace.admin.dto.AdminBuyerRequestResponse;
import com.marketplace.admin.dto.AdminTravellerTripResponse;
import com.marketplace.admin.dto.AdminUserResponse;
import com.marketplace.listing.entity.BuyerRequest;
import com.marketplace.listing.entity.TravellerTrip;
import com.marketplace.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AdminMapper {

    public AdminUserResponse toUserResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .accountStatus(user.getAccountStatus())
                .profileCompleted(user.isProfileCompleted())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public AdminBuyerRequestResponse toBuyerRequestResponse(BuyerRequest entity) {
        return AdminBuyerRequestResponse.builder()
                .id(entity.getId())
                .buyerId(entity.getBuyer().getId())
                .buyerName(entity.getBuyer().getFullName())
                .buyerEmail(entity.getBuyer().getEmail())
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

    public AdminTravellerTripResponse toTravellerTripResponse(TravellerTrip entity) {
        return AdminTravellerTripResponse.builder()
                .id(entity.getId())
                .travellerId(entity.getTraveller().getId())
                .travellerName(entity.getTraveller().getFullName())
                .travellerEmail(entity.getTraveller().getEmail())
                .sourceCountry(entity.getSourceCountry())
                .sourceCity(entity.getSourceCity())
                .destinationCountry(entity.getDestinationCountry())
                .destinationCity(entity.getDestinationCity())
                .travelDate(entity.getTravelDate())
                .availableCapacityKg(entity.getAvailableCapacityKg())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
