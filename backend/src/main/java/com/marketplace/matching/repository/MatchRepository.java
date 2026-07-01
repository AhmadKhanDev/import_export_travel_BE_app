package com.marketplace.matching.repository;

import com.marketplace.matching.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID>, JpaSpecificationExecutor<Match> {

    Optional<Match> findByBuyerRequest_IdAndTravellerTrip_Id(UUID buyerRequestId, UUID travellerTripId);
}