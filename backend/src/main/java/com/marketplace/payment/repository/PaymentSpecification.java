package com.marketplace.payment.repository;

import com.marketplace.payment.dto.PaymentFilter;
import com.marketplace.payment.entity.Payment;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class PaymentSpecification {

    private PaymentSpecification() {
    }

    public static Specification<Payment> withFilter(PaymentFilter filter) {
        return Specification.where(byBookingId(filter.getBookingId()))
                .and(byBuyerId(filter.getBuyerId()))
                .and(byTravellerId(filter.getTravellerId()))
                .and(byStatus(filter.getStatus()));
    }

    public static Specification<Payment> forParticipant(UUID userId, PaymentFilter filter) {
        Specification<Payment> participantSpec = (root, query, cb) -> cb.or(
                cb.equal(root.get("buyer").get("id"), userId),
                cb.equal(root.get("traveller").get("id"), userId)
        );
        return participantSpec.and(withFilter(filter));
    }

    private static Specification<Payment> byBookingId(UUID bookingId) {
        if (bookingId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("booking").get("id"), bookingId);
    }

    private static Specification<Payment> byBuyerId(UUID buyerId) {
        if (buyerId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("buyer").get("id"), buyerId);
    }

    private static Specification<Payment> byTravellerId(UUID travellerId) {
        if (travellerId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("traveller").get("id"), travellerId);
    }

    private static Specification<Payment> byStatus(com.marketplace.payment.entity.PaymentStatus status) {
        if (status == null) return null;
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}