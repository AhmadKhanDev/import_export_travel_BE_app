package com.marketplace.booking.repository;

import com.marketplace.booking.dto.BookingFilter;
import com.marketplace.booking.entity.Booking;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class BookingSpecification {

    private BookingSpecification() {
    }

    public static Specification<Booking> withFilter(BookingFilter filter) {
        return Specification.where(byBuyerId(filter.getBuyerId()))
                .and(byTravellerId(filter.getTravellerId()))
                .and(byStatus(filter.getStatus()));
    }

    public static Specification<Booking> forParticipant(UUID userId, BookingFilter filter) {
        Specification<Booking> participantSpec = (root, query, cb) -> cb.or(
                cb.equal(root.get("buyer").get("id"), userId),
                cb.equal(root.get("traveller").get("id"), userId)
        );
        return participantSpec.and(withFilter(filter));
    }

    private static Specification<Booking> byBuyerId(UUID buyerId) {
        if (buyerId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("buyer").get("id"), buyerId);
    }

    private static Specification<Booking> byTravellerId(UUID travellerId) {
        if (travellerId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("traveller").get("id"), travellerId);
    }

    private static Specification<Booking> byStatus(com.marketplace.booking.entity.BookingStatus status) {
        if (status == null) return null;
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}