-- Matches between buyer requests and traveller trips
CREATE TABLE matches (
    id                  UUID PRIMARY KEY,
    buyer_request_id    UUID NOT NULL REFERENCES buyer_requests (id) ON DELETE CASCADE,
    traveller_trip_id   UUID NOT NULL REFERENCES traveller_trips (id) ON DELETE CASCADE,
    match_score         DECIMAL(5, 2) NOT NULL,
    status              VARCHAR(50) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_matches_request_trip UNIQUE (buyer_request_id, traveller_trip_id),
    CONSTRAINT chk_matches_status CHECK (
        status IN ('SUGGESTED', 'VIEWED', 'OFFER_SENT', 'REJECTED')
    )
);

CREATE INDEX idx_matches_buyer_request_id ON matches (buyer_request_id);
CREATE INDEX idx_matches_traveller_trip_id ON matches (traveller_trip_id);
CREATE INDEX idx_matches_status ON matches (status);
CREATE INDEX idx_matches_score ON matches (match_score DESC);