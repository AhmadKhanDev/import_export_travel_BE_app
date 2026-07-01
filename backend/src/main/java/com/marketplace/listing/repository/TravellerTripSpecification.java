package com.marketplace.listing.repository;

import com.marketplace.listing.dto.TravellerTripFilter;
import com.marketplace.listing.entity.TravellerTrip;
import com.marketplace.listing.entity.TravellerTripStatus;
import com.marketplace.user.entity.Role;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class TravellerTripSpecification {

    private TravellerTripSpecification() {
    }

    public static Specification<TravellerTrip> withFilters(TravellerTripFilter filter, UUID travellerId, Role viewerRole) {
        return Specification.where(belongsToTraveller(travellerId))
                .and(hasSourceCountry(filter.getSourceCountry()))
                .and(hasSourceCity(filter.getSourceCity()))
                .and(hasDestinationCountry(filter.getDestinationCountry()))
                .and(hasDestinationCity(filter.getDestinationCity()))
                .and(hasStatus(filter.getStatus(), viewerRole, travellerId != null))
                .and(travelDateFrom(filter.getTravelDateFrom()))
                .and(travelDateTo(filter.getTravelDateTo()));
    }

    private static Specification<TravellerTrip> belongsToTraveller(UUID travellerId) {
        if (travellerId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("traveller").get("id"), travellerId);
    }

    private static Specification<TravellerTrip> hasSourceCountry(String sourceCountry) {
        if (sourceCountry == null || sourceCountry.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get("sourceCountry")), sourceCountry.toLowerCase().trim());
    }

    private static Specification<TravellerTrip> hasSourceCity(String sourceCity) {
        if (sourceCity == null || sourceCity.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get("sourceCity")), sourceCity.toLowerCase().trim());
    }

    private static Specification<TravellerTrip> hasDestinationCountry(String destinationCountry) {
        if (destinationCountry == null || destinationCountry.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get("destinationCountry")), destinationCountry.toLowerCase().trim());
    }

    private static Specification<TravellerTrip> hasDestinationCity(String destinationCity) {
        if (destinationCity == null || destinationCity.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get("destinationCity")), destinationCity.toLowerCase().trim());
    }

    private static Specification<TravellerTrip> hasStatus(TravellerTripStatus status, Role viewerRole, boolean ownList) {
        if (status != null) {
            return (root, query, cb) -> cb.equal(root.get("status"), status);
        }
        if (ownList || viewerRole == Role.ADMIN) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), TravellerTripStatus.PUBLISHED);
    }

    private static Specification<TravellerTrip> travelDateFrom(java.time.Instant travelDateFrom) {
        if (travelDateFrom == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("travelDate"), travelDateFrom);
    }

    private static Specification<TravellerTrip> travelDateTo(java.time.Instant travelDateTo) {
        if (travelDateTo == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("travelDate"), travelDateTo);
    }
}