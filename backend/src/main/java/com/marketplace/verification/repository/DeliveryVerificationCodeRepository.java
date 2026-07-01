package com.marketplace.verification.repository;

import com.marketplace.verification.entity.DeliveryCodeStatus;
import com.marketplace.verification.entity.DeliveryVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryVerificationCodeRepository extends JpaRepository<DeliveryVerificationCode, UUID> {

    Optional<DeliveryVerificationCode> findByBooking_IdAndStatus(UUID bookingId, DeliveryCodeStatus status);

    Optional<DeliveryVerificationCode> findTopByBooking_IdOrderByCreatedAtDesc(UUID bookingId);

    List<DeliveryVerificationCode> findByBooking_Id(UUID bookingId);
}
