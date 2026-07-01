package com.marketplace.listing.repository;

import com.marketplace.listing.entity.BuyerRequest;
import com.marketplace.listing.entity.BuyerRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface BuyerRequestRepository extends JpaRepository<BuyerRequest, UUID>, JpaSpecificationExecutor<BuyerRequest> {

    List<BuyerRequest> findByStatusAndSourceCountryIgnoreCaseAndDestinationCountryIgnoreCase(
            BuyerRequestStatus status, String sourceCountry, String destinationCountry);
}