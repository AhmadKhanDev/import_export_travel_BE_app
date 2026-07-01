package com.marketplace.dispute.repository;

import com.marketplace.dispute.entity.Dispute;
import com.marketplace.dispute.entity.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DisputeRepository extends JpaRepository<Dispute, UUID>, JpaSpecificationExecutor<Dispute> {

    List<Dispute> findByBooking_IdOrderByCreatedAtDesc(UUID bookingId);

    boolean existsByBooking_IdAndStatusIn(UUID bookingId, Collection<DisputeStatus> statuses);

    List<Dispute> findByRaisedByUser_IdOrderByCreatedAtDesc(UUID raisedByUserId);
}
