package com.marketplace.listing.repository;

import com.marketplace.listing.entity.BuyerRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface BuyerRequestRepository extends JpaRepository<BuyerRequest, UUID>, JpaSpecificationExecutor<BuyerRequest> {
}