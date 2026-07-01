-- Delivery verification codes
CREATE TABLE delivery_verification_codes (
    id          UUID PRIMARY KEY,
    booking_id  UUID NOT NULL REFERENCES bookings (id) ON DELETE CASCADE,
    code_hash   VARCHAR(255) NOT NULL,
    status      VARCHAR(50) NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    verified_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version     BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_delivery_verification_codes_status CHECK (
        status IN ('ACTIVE', 'USED', 'EXPIRED')
    )
);

CREATE INDEX idx_delivery_verification_codes_booking_id
    ON delivery_verification_codes (booking_id);
CREATE INDEX idx_delivery_verification_codes_status
    ON delivery_verification_codes (status);
CREATE INDEX idx_delivery_verification_codes_expires_at
    ON delivery_verification_codes (expires_at);

CREATE UNIQUE INDEX uq_delivery_verification_codes_active_booking
    ON delivery_verification_codes (booking_id)
    WHERE status = 'ACTIVE';
