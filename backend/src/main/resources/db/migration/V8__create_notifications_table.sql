CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    notification_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reference_type VARCHAR(50),
    reference_id UUID,
    failure_reason TEXT,
    sent_at TIMESTAMPTZ,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_notifications_channel CHECK (channel IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP')),
    CONSTRAINT chk_notifications_status CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'READ')),
    CONSTRAINT chk_notifications_type CHECK (notification_type IN (
        'KYC_SUBMITTED', 'KYC_APPROVED', 'KYC_REJECTED', 'MATCH_FOUND', 'OFFER_SENT',
        'OFFER_ACCEPTED', 'OFFER_REJECTED', 'OFFER_CANCELLED', 'BOOKING_CREATED',
        'PAYMENT_HELD', 'DELIVERY_CODE_GENERATED', 'DELIVERY_VERIFIED', 'PAYMENT_RELEASED',
        'PAYMENT_REFUNDED', 'DISPUTE_CREATED', 'DISPUTE_RESOLVED', 'SYSTEM_ALERT')),
    CONSTRAINT chk_notifications_reference_type CHECK (reference_type IS NULL OR reference_type IN (
        'USER', 'KYC', 'BUYER_REQUEST', 'TRAVELLER_TRIP', 'MATCH', 'OFFER', 'BOOKING', 'PAYMENT', 'DELIVERY_CODE', 'DISPUTE'))
);

CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_notifications_status ON notifications (status);
CREATE INDEX idx_notifications_channel ON notifications (channel);
CREATE INDEX idx_notifications_notification_type ON notifications (notification_type);
CREATE INDEX idx_notifications_reference_type ON notifications (reference_type);
CREATE INDEX idx_notifications_reference_id ON notifications (reference_id);
CREATE INDEX idx_notifications_created_at ON notifications (created_at DESC);