package com.marketplace.offer.repository;

import com.marketplace.offer.entity.Offer;
import com.marketplace.offer.entity.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface OfferRepository extends JpaRepository<Offer, UUID>, JpaSpecificationExecutor<Offer> {

    boolean existsByBuyerRequest_IdAndTravellerTrip_IdAndTraveller_IdAndStatus(
            UUID buyerRequestId, UUID travellerTripId, UUID travellerId, OfferStatus status);

    List<Offer> findByBuyerRequest_IdAndStatus(UUID buyerRequestId, OfferStatus status);
}