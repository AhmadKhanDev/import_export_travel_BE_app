-- Booking-based chat rooms and messages

CREATE TABLE chat_rooms (
    id              UUID PRIMARY KEY,
    booking_id      UUID NOT NULL UNIQUE REFERENCES bookings (id) ON DELETE CASCADE,
    buyer_id        UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    traveller_id    UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    status          VARCHAR(20) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_chat_rooms_status CHECK (status IN ('ACTIVE', 'CLOSED'))
);

CREATE TABLE chat_messages (
    id              UUID PRIMARY KEY,
    chat_room_id    UUID NOT NULL REFERENCES chat_rooms (id) ON DELETE CASCADE,
    sender_id       UUID REFERENCES users (id) ON DELETE SET NULL,
    message         TEXT NOT NULL,
    message_type    VARCHAR(20) NOT NULL,
    attachment_url  VARCHAR(2048),
    sent_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    read_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_chat_messages_type CHECK (message_type IN ('TEXT', 'IMAGE', 'SYSTEM'))
);

CREATE INDEX idx_chat_rooms_booking_id ON chat_rooms (booking_id);
CREATE INDEX idx_chat_rooms_buyer_id ON chat_rooms (buyer_id);
CREATE INDEX idx_chat_rooms_traveller_id ON chat_rooms (traveller_id);
CREATE INDEX idx_chat_rooms_status ON chat_rooms (status);

CREATE INDEX idx_chat_messages_chat_room_id ON chat_messages (chat_room_id);
CREATE INDEX idx_chat_messages_sender_id ON chat_messages (sender_id);
CREATE INDEX idx_chat_messages_sent_at ON chat_messages (sent_at);
CREATE INDEX idx_chat_messages_read_at ON chat_messages (read_at);

-- Extend notification types and reference types for chat
ALTER TABLE notifications DROP CONSTRAINT chk_notifications_type;
ALTER TABLE notifications ADD CONSTRAINT chk_notifications_type CHECK (notification_type IN (
    'KYC_SUBMITTED', 'KYC_APPROVED', 'KYC_REJECTED', 'MATCH_FOUND', 'OFFER_SENT',
    'OFFER_ACCEPTED', 'OFFER_REJECTED', 'OFFER_CANCELLED', 'BOOKING_CREATED',
    'PAYMENT_HELD', 'DELIVERY_CODE_GENERATED', 'DELIVERY_VERIFIED', 'PAYMENT_RELEASED',
    'PAYMENT_REFUNDED', 'DISPUTE_CREATED', 'DISPUTE_UNDER_REVIEW', 'DISPUTE_RESOLVED',
    'DISPUTE_REJECTED', 'REVIEW_RECEIVED', 'CHAT_MESSAGE', 'SYSTEM_ALERT'));

ALTER TABLE notifications DROP CONSTRAINT chk_notifications_reference_type;
ALTER TABLE notifications ADD CONSTRAINT chk_notifications_reference_type CHECK (reference_type IS NULL OR reference_type IN (
    'USER', 'KYC', 'BUYER_REQUEST', 'TRAVELLER_TRIP', 'MATCH', 'OFFER', 'BOOKING',
    'PAYMENT', 'DELIVERY_CODE', 'DISPUTE', 'CHAT'));
