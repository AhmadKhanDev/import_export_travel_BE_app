package com.marketplace.dispute.repository;

import com.marketplace.dispute.dto.DisputeFilter;
import com.marketplace.dispute.entity.Dispute;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class DisputeSpecification {

    private DisputeSpecification() {
    }

    public static Specification<Dispute> withFilter(DisputeFilter filter) {
        return Specification.where(byStatus(filter.getStatus()))
                .and(byRaisedByUserId(filter.getRaisedByUserId()))
                .and(byBookingId(filter.getBookingId()))
                .and(byReason(filter.getReason()));
    }

    public static Specification<Dispute> forParticipant(UUID userId, DisputeFilter filter) {
        Specification<Dispute> participantSpec = (root, query, cb) -> cb.or(
                cb.equal(root.get("raisedByUser").get("id"), userId),
                cb.equal(root.get("booking").get("buyer").get("id"), userId),
                cb.equal(root.get("booking").get("traveller").get("id"), userId)
        );
        return participantSpec.and(withFilter(filter));
    }

    private static Specification<Dispute> byStatus(com.marketplace.dispute.entity.DisputeStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private static Specification<Dispute> byRaisedByUserId(UUID raisedByUserId) {
        if (raisedByUserId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("raisedByUser").get("id"), raisedByUserId);
    }

    private static Specification<Dispute> byBookingId(UUID bookingId) {
        if (bookingId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("booking").get("id"), bookingId);
    }

    private static Specification<Dispute> byReason(com.marketplace.dispute.entity.DisputeReason reason) {
        if (reason == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("reason"), reason);
    }
}
