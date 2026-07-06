package com.marketplace.tracking.repository;

import com.marketplace.tracking.entity.BookingTrackingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BookingTrackingSessionRepository extends JpaRepository<BookingTrackingSession, UUID> {

    Optional<BookingTrackingSession> findByBooking_Id(UUID bookingId);
}
