package com.marketplace.infrastructure.kafka.topics;

public final class EventType {

    private EventType() {}

    // User events
    public static final String USER_REGISTERED = "USER_REGISTERED";

    // KYC events
    public static final String KYC_SUBMITTED = "KYC_SUBMITTED";
    public static final String KYC_APPROVED = "KYC_APPROVED";
    public static final String KYC_REJECTED = "KYC_REJECTED";

    // Offer events
    public static final String OFFER_SENT = "OFFER_SENT";
    public static final String OFFER_ACCEPTED = "OFFER_ACCEPTED";
    public static final String OFFER_REJECTED = "OFFER_REJECTED";

    // Booking events
    public static final String BOOKING_CREATED = "BOOKING_CREATED";
    public static final String BOOKING_CANCELLED = "BOOKING_CANCELLED";
    public static final String BOOKING_COMPLETED = "BOOKING_COMPLETED";

    // Payment events
    public static final String PAYMENT_HELD = "PAYMENT_HELD";
    public static final String PAYMENT_RELEASED = "PAYMENT_RELEASED";
    public static final String PAYMENT_REFUNDED = "PAYMENT_REFUNDED";

    // Delivery events
    public static final String DELIVERY_CODE_GENERATED = "DELIVERY_CODE_GENERATED";
    public static final String DELIVERY_VERIFIED = "DELIVERY_VERIFIED";

    // Dispute events
    public static final String DISPUTE_CREATED = "DISPUTE_CREATED";
    public static final String DISPUTE_RESOLVED = "DISPUTE_RESOLVED";
    public static final String DISPUTE_REJECTED = "DISPUTE_REJECTED";
}
