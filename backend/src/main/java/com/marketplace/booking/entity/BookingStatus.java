package com.marketplace.booking.entity;

public enum BookingStatus {
    PENDING_OFFER,
    OFFER_SENT,
    ACCEPTED,
    PAYMENT_PENDING,
    PAYMENT_HELD,
    IN_TRANSIT,
    DELIVERED_PENDING_VERIFICATION,
    DELIVERED,
    COMPLETED,
    CANCELLED,
    DISPUTED
}