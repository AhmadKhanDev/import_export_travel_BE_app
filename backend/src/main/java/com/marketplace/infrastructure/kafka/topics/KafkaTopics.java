package com.marketplace.infrastructure.kafka.topics;

public final class KafkaTopics {

    private KafkaTopics() {}

    public static final String USER_EVENTS = "marketplace.user.events";
    public static final String KYC_EVENTS = "marketplace.kyc.events";
    public static final String OFFER_EVENTS = "marketplace.offer.events";
    public static final String BOOKING_EVENTS = "marketplace.booking.events";
    public static final String PAYMENT_EVENTS = "marketplace.payment.events";
    public static final String DELIVERY_EVENTS = "marketplace.delivery.events";
    public static final String NOTIFICATION_EVENTS = "marketplace.notification.events";
    public static final String DISPUTE_EVENTS = "marketplace.dispute.events";
}
