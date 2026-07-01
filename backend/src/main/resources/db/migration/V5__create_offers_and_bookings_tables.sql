-- Offers and bookings
CREATE TABLE offers (
    id                  UUID PRIMARY KEY,
    buyer_request_id    UUID NOT NULL REFERENCES buyer_requests (id) ON DELETE CASCADE,
    traveller_trip_id   UUID NOT NULL REFERENCES traveller_trips (id) ON DELETE CASCADE,
    match_id            UUID REFERENCES matches (id) ON DELETE SET NULL,
    traveller_id        UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    buyer_id            UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    item_price          DECIMAL(12, 2) NOT NULL,
    traveller_fee       DECIMAL(12, 2) NOT NULL,
    platform_fee        DECIMAL(12, 2) NOT NULL,
    total_amount        DECIMAL(12, 2) NOT NULL,
    currency            VARCHAR(10) NOT NULL,
    message             TEXT,
    status              VARCHAR(50) NOT NULL,
    expires_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_offers_status CHECK (
        status IN ('SENT', 'ACCEPTED', 'REJECTED', 'EXPIRED', 'CANCELLED')
    )
);

CREATE TABLE bookings (
    id                  UUID PRIMARY KEY,
    offer_id            UUID NOT NULL UNIQUE REFERENCES offers (id) ON DELETE CASCADE,
    buyer_id            UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    traveller_id        UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    buyer_request_id    UUID NOT NULL REFERENCES buyer_requests (id) ON DELETE CASCADE,
    traveller_trip_id   UUID NOT NULL REFERENCES traveller_trips (id) ON DELETE CASCADE,
    status              VARCHAR(50) NOT NULL,
    accepted_at         TIMESTAMPTZ,
    delivered_at        TIMESTAMPTZ,
    completed_at        TIMESTAMPTZ,
    cancelled_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_bookings_status CHECK (
        status IN (
            'PENDING_OFFER', 'OFFER_SENT', 'ACCEPTED', 'PAYMENT_PENDING', 'PAYMENT_HELD',
            'IN_TRANSIT', 'DELIVERED_PENDING_VERIFICATION', 'DELIVERED', 'COMPLETED',
            'CANCELLED', 'DISPUTED'
        )
    )
);

CREATE INDEX idx_offers_buyer_request_id ON offers (buyer_request_id);
CREATE INDEX idx_offers_traveller_trip_id ON offers (traveller_trip_id);
CREATE INDEX idx_offers_match_id ON offers (match_id);
CREATE INDEX idx_offers_buyer_id ON offers (buyer_id);
CREATE INDEX idx_offers_traveller_id ON offers (traveller_id);
CREATE INDEX idx_offers_status ON offers (status);
CREATE INDEX idx_offers_expires_at ON offers (expires_at);

CREATE UNIQUE INDEX uq_offers_active_sent ON offers (buyer_request_id, traveller_trip_id, traveller_id)
    WHERE status = 'SENT';

CREATE INDEX idx_bookings_offer_id ON bookings (offer_id);
CREATE INDEX idx_bookings_buyer_id ON bookings (buyer_id);
CREATE INDEX idx_bookings_traveller_id ON bookings (traveller_id);
CREATE INDEX idx_bookings_buyer_request_id ON bookings (buyer_request_id);
CREATE INDEX idx_bookings_traveller_trip_id ON bookings (traveller_trip_id);
CREATE INDEX idx_bookings_status ON bookings (status);