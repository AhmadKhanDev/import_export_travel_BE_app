package com.marketplace.listing.repository;

import com.marketplace.listing.dto.BuyerRequestFilter;
import com.marketplace.listing.entity.BuyerRequest;
import com.marketplace.listing.entity.BuyerRequestStatus;
import com.marketplace.user.entity.Role;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class BuyerRequestSpecification {

    private BuyerRequestSpecification() {
    }

    public static Specification<BuyerRequest> withFilters(BuyerRequestFilter filter, UUID buyerId, Role viewerRole) {
        return Specification.where(belongsToBuyer(buyerId))
                .and(hasSourceCountry(filter.getSourceCountry()))
                .and(hasSourceCity(filter.getSourceCity()))
                .and(hasDestinationCountry(filter.getDestinationCountry()))
                .and(hasDestinationCity(filter.getDestinationCity()))
                .and(hasItemCategory(filter.getItemCategory()))
                .and(hasStatus(filter.getStatus(), viewerRole, buyerId != null));
    }

    private static Specification<BuyerRequest> belongsToBuyer(UUID buyerId) {
        if (buyerId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("buyer").get("id"), buyerId);
    }

    private static Specification<BuyerRequest> hasSourceCountry(String sourceCountry) {
        if (sourceCountry == null || sourceCountry.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get("sourceCountry")), sourceCountry.toLowerCase().trim());
    }

    private static Specification<BuyerRequest> hasSourceCity(String sourceCity) {
        if (sourceCity == null || sourceCity.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get("sourceCity")), sourceCity.toLowerCase().trim());
    }

    private static Specification<BuyerRequest> hasDestinationCountry(String destinationCountry) {
        if (destinationCountry == null || destinationCountry.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get("destinationCountry")), destinationCountry.toLowerCase().trim());
    }

    private static Specification<BuyerRequest> hasDestinationCity(String destinationCity) {
        if (destinationCity == null || destinationCity.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get("destinationCity")), destinationCity.toLowerCase().trim());
    }

    private static Specification<BuyerRequest> hasItemCategory(String itemCategory) {
        if (itemCategory == null || itemCategory.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get("itemCategory")), itemCategory.toLowerCase().trim());
    }

    private static Specification<BuyerRequest> hasStatus(BuyerRequestStatus status, Role viewerRole, boolean ownList) {
        if (status != null) {
            return (root, query, cb) -> cb.equal(root.get("status"), status);
        }
        if (ownList || viewerRole == Role.ADMIN) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), BuyerRequestStatus.PUBLISHED);
    }
}