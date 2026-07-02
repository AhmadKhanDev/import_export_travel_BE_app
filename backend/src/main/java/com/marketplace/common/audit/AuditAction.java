package com.marketplace.common.audit;

public final class AuditAction {

    private AuditAction() {
    }

    public static final String ADMIN_USER_DISABLED = "ADMIN_USER_DISABLED";
    public static final String ADMIN_USER_ENABLED = "ADMIN_USER_ENABLED";
    public static final String ADMIN_USER_ROLE_CHANGED = "ADMIN_USER_ROLE_CHANGED";
    public static final String ADMIN_BUYER_REQUEST_CANCELLED = "ADMIN_BUYER_REQUEST_CANCELLED";
    public static final String ADMIN_TRAVELLER_TRIP_CANCELLED = "ADMIN_TRAVELLER_TRIP_CANCELLED";
    public static final String ADMIN_BOOKING_CANCELLED = "ADMIN_BOOKING_CANCELLED";
    public static final String ADMIN_PAYMENT_RELEASED = "ADMIN_PAYMENT_RELEASED";
    public static final String ADMIN_PAYMENT_REFUNDED = "ADMIN_PAYMENT_REFUNDED";
    public static final String KYC_SUBMITTED = "KYC_SUBMITTED";
    public static final String ADMIN_KYC_APPROVED = "ADMIN_KYC_APPROVED";
    public static final String ADMIN_KYC_REJECTED = "ADMIN_KYC_REJECTED";
    public static final String ADMIN_DISPUTE_UNDER_REVIEW = "ADMIN_DISPUTE_UNDER_REVIEW";
    public static final String ADMIN_DISPUTE_RESOLVED = "ADMIN_DISPUTE_RESOLVED";
    public static final String ADMIN_DISPUTE_REJECTED = "ADMIN_DISPUTE_REJECTED";
    public static final String ADMIN_CHAT_CLOSED = "ADMIN_CHAT_CLOSED";
    public static final String ADMIN_NOTIFICATION_SENT = "ADMIN_NOTIFICATION_SENT";
    public static final String ADMIN_REVIEW_DELETED = "ADMIN_REVIEW_DELETED";
}
