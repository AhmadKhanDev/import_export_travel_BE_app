-- Payments and escrow records
CREATE TABLE payments (
    id                  UUID PRIMARY KEY,
    booking_id          UUID NOT NULL UNIQUE REFERENCES bookings (id) ON DELETE CASCADE,
    buyer_id            UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    traveller_id        UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    amount              DECIMAL(12, 2) NOT NULL,
    platform_fee        DECIMAL(12, 2) NOT NULL,
    traveller_payout    DECIMAL(12, 2) NOT NULL,
    currency            VARCHAR(10) NOT NULL,
    payment_provider    VARCHAR(50) NOT NULL,
    provider_payment_id VARCHAR(255),
    idempotency_key     VARCHAR(255),
    status              VARCHAR(50) NOT NULL,
    failure_reason      TEXT,
    paid_at             TIMESTAMPTZ,
    released_at         TIMESTAMPTZ,
    refunded_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_payments_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT chk_payments_status CHECK (
        status IN ('PENDING', 'AUTHORIZED', 'HELD', 'RELEASED', 'REFUNDED', 'FAILED')
    ),
    CONSTRAINT chk_payments_provider CHECK (
        payment_provider IN ('FAKE', 'STRIPE', 'PAYPAL', 'LOCAL_BANK')
    )
);

CREATE INDEX idx_payments_booking_id ON payments (booking_id);
CREATE INDEX idx_payments_buyer_id ON payments (buyer_id);
CREATE INDEX idx_payments_traveller_id ON payments (traveller_id);
CREATE INDEX idx_payments_status ON payments (status);
CREATE INDEX idx_payments_provider_payment_id ON payments (provider_payment_id);
CREATE INDEX idx_payments_idempotency_key ON payments (idempotency_key);