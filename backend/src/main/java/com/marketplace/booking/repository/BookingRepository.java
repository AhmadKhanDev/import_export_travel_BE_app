package com.marketplace.booking.repository;

import com.marketplace.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {

    long countByStatus(com.marketplace.booking.entity.BookingStatus status);

    long countByBuyer_Id(UUID buyerId);

    long countByTraveller_Id(UUID travellerId);
}