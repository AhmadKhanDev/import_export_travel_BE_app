package com.marketplace.matching.repository;

import com.marketplace.matching.entity.Match;
import com.marketplace.matching.entity.MatchStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class MatchSpecification {

    private MatchSpecification() {
    }

    public static Specification<Match> withFilters(
            UUID buyerRequestId,
            UUID travellerTripId,
            MatchStatus status,
            boolean excludeRejected) {

        return Specification.where(byBuyerRequestId(buyerRequestId))
                .and(byTravellerTripId(travellerTripId))
                .and(byStatus(status, excludeRejected));
    }

    public static Specification<Match> byBuyerRequest(UUID buyerRequestId, MatchStatus status, boolean excludeRejected) {
        return withFilters(buyerRequestId, null, status, excludeRejected);
    }

    public static Specification<Match> byTravellerTrip(UUID travellerTripId, MatchStatus status, boolean excludeRejected) {
        return withFilters(null, travellerTripId, status, excludeRejected);
    }

    private static Specification<Match> byBuyerRequestId(UUID buyerRequestId) {
        if (buyerRequestId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("buyerRequest").get("id"), buyerRequestId);
    }

    private static Specification<Match> byTravellerTripId(UUID travellerTripId) {
        if (travellerTripId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("travellerTrip").get("id"), travellerTripId);
    }

    private static Specification<Match> byStatus(MatchStatus status, boolean excludeRejected) {
        if (status != null) {
            return (root, query, cb) -> cb.equal(root.get("status"), status);
        }
        if (excludeRejected) {
            return (root, query, cb) -> cb.notEqual(root.get("status"), MatchStatus.REJECTED);
        }
        return null;
    }
}