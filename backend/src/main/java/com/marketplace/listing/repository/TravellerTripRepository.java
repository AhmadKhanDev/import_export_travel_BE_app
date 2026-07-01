package com.marketplace.listing.repository;

import com.marketplace.listing.entity.TravellerTrip;
import com.marketplace.listing.entity.TravellerTripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface TravellerTripRepository extends JpaRepository<TravellerTrip, UUID>, JpaSpecificationExecutor<TravellerTrip> {

    List<TravellerTrip> findByStatusAndSourceCountryIgnoreCaseAndDestinationCountryIgnoreCase(
            TravellerTripStatus status, String sourceCountry, String destinationCountry);
}