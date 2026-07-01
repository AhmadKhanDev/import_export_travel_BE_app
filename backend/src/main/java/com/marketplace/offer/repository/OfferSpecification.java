package com.marketplace.offer.repository;

import com.marketplace.offer.dto.OfferFilter;
import com.marketplace.offer.entity.Offer;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class OfferSpecification {

    private OfferSpecification() {
    }

    public static Specification<Offer> withFilter(OfferFilter filter) {
        return Specification.where(byBuyerId(filter.getBuyerId()))
                .and(byTravellerId(filter.getTravellerId()))
                .and(byBuyerRequestId(filter.getBuyerRequestId()))
                .and(byTravellerTripId(filter.getTravellerTripId()))
                .and(byStatus(filter.getStatus()));
    }

    public static Specification<Offer> forParticipant(UUID userId, OfferFilter filter) {
        Specification<Offer> participantSpec = (root, query, cb) -> cb.or(
                cb.equal(root.get("buyer").get("id"), userId),
                cb.equal(root.get("traveller").get("id"), userId)
        );
        return participantSpec.and(withFilter(filter));
    }

    private static Specification<Offer> byBuyerId(UUID buyerId) {
        if (buyerId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("buyer").get("id"), buyerId);
    }

    private static Specification<Offer> byTravellerId(UUID travellerId) {
        if (travellerId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("traveller").get("id"), travellerId);
    }

    private static Specification<Offer> byBuyerRequestId(UUID buyerRequestId) {
        if (buyerRequestId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("buyerRequest").get("id"), buyerRequestId);
    }

    private static Specification<Offer> byTravellerTripId(UUID travellerTripId) {
        if (travellerTripId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("travellerTrip").get("id"), travellerTripId);
    }

    private static Specification<Offer> byStatus(com.marketplace.offer.entity.OfferStatus status) {
        if (status == null) return null;
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}