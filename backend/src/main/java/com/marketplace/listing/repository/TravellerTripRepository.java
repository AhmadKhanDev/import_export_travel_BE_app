package com.marketplace.listing.repository;

import com.marketplace.listing.entity.TravellerTrip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface TravellerTripRepository extends JpaRepository<TravellerTrip, UUID>, JpaSpecificationExecutor<TravellerTrip> {
}