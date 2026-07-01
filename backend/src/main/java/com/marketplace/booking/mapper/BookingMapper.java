package com.marketplace.booking.mapper;

import com.marketplace.booking.dto.AdminBookingResponse;
import com.marketplace.booking.dto.BookingResponse;
import com.marketplace.booking.entity.Booking;
import com.marketplace.offer.entity.Offer;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {
        Offer offer = booking.getOffer();
        return BookingResponse.builder()
                .id(booking.getId())
                .offerId(offer.getId())
                .buyerId(booking.getBuyer().getId())
                .buyerName(booking.getBuyer().getFullName())
                .travellerId(booking.getTraveller().getId())
                .travellerName(booking.getTraveller().getFullName())
                .buyerRequestId(booking.getBuyerRequest().getId())
                .buyerRequestTitle(booking.getBuyerRequest().getTitle())
                .travellerTripId(booking.getTravellerTrip().getId())
                .sourceCountry(booking.getBuyerRequest().getSourceCountry())
                .sourceCity(booking.getBuyerRequest().getSourceCity())
                .destinationCountry(booking.getBuyerRequest().getDestinationCountry())
                .destinationCity(booking.getBuyerRequest().getDestinationCity())
                .itemPrice(offer.getItemPrice())
                .travellerFee(offer.getTravellerFee())
                .platformFee(offer.getPlatformFee())
                .totalAmount(offer.getTotalAmount())
                .currency(offer.getCurrency())
                .status(booking.getStatus())
                .acceptedAt(booking.getAcceptedAt())
                .deliveredAt(booking.getDeliveredAt())
                .completedAt(booking.getCompletedAt())
                .cancelledAt(booking.getCancelledAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    public AdminBookingResponse toAdminResponse(Booking booking) {
        Offer offer = booking.getOffer();
        return AdminBookingResponse.builder()
                .id(booking.getId())
                .offerId(offer.getId())
                .buyerId(booking.getBuyer().getId())
                .buyerName(booking.getBuyer().getFullName())
                .buyerEmail(booking.getBuyer().getEmail())
                .travellerId(booking.getTraveller().getId())
                .travellerName(booking.getTraveller().getFullName())
                .travellerEmail(booking.getTraveller().getEmail())
                .buyerRequestId(booking.getBuyerRequest().getId())
                .buyerRequestTitle(booking.getBuyerRequest().getTitle())
                .travellerTripId(booking.getTravellerTrip().getId())
                .status(booking.getStatus())
                .totalAmount(offer.getTotalAmount())
                .currency(offer.getCurrency())
                .acceptedAt(booking.getAcceptedAt())
                .deliveredAt(booking.getDeliveredAt())
                .completedAt(booking.getCompletedAt())
                .cancelledAt(booking.getCancelledAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}