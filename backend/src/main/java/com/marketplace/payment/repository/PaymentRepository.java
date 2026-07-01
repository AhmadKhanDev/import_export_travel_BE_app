package com.marketplace.payment.repository;

import com.marketplace.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {

    Optional<Payment> findByBooking_Id(UUID bookingId);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    boolean existsByBooking_Id(UUID bookingId);

    long countByStatus(PaymentStatus status);
}