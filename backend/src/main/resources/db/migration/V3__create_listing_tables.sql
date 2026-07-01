-- Buyer requests and traveller trips (listing module)
CREATE TABLE buyer_requests (
    id                  UUID PRIMARY KEY,
    buyer_id            UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title               VARCHAR(255) NOT NULL,
    description         TEXT,
    item_category       VARCHAR(100) NOT NULL,
    brand               VARCHAR(100),
    source_country      VARCHAR(100) NOT NULL,
    source_city         VARCHAR(100) NOT NULL,
    destination_country VARCHAR(100) NOT NULL,
    destination_city    VARCHAR(100) NOT NULL,
    estimated_item_price DECIMAL(12, 2),
    status              VARCHAR(50) NOT NULL,
    needed_before       TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_buyer_requests_status CHECK (
        status IN ('DRAFT', 'PUBLISHED', 'MATCHED', 'BOOKED', 'COMPLETED', 'CANCELLED')
    )
);

CREATE TABLE traveller_trips (
    id                  UUID PRIMARY KEY,
    traveller_id        UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    source_country      VARCHAR(100) NOT NULL,
    source_city         VARCHAR(100) NOT NULL,
    destination_country VARCHAR(100) NOT NULL,
    destination_city    VARCHAR(100) NOT NULL,
    travel_date         TIMESTAMPTZ NOT NULL,
    available_capacity_kg DECIMAL(10, 2),
    allowed_item_types  TEXT,
    status              VARCHAR(50) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_traveller_trips_status CHECK (
        status IN ('DRAFT', 'PUBLISHED', 'MATCHED', 'CLOSED', 'CANCELLED')
    )
);

CREATE INDEX idx_buyer_requests_buyer_id ON buyer_requests (buyer_id);
CREATE INDEX idx_buyer_requests_status ON buyer_requests (status);
CREATE INDEX idx_buyer_requests_route_countries ON buyer_requests (source_country, destination_country);
CREATE INDEX idx_buyer_requests_route_cities ON buyer_requests (source_city, destination_city);
CREATE INDEX idx_buyer_requests_item_category ON buyer_requests (item_category);

CREATE INDEX idx_traveller_trips_traveller_id ON traveller_trips (traveller_id);
CREATE INDEX idx_traveller_trips_status ON traveller_trips (status);
CREATE INDEX idx_traveller_trips_route_countries ON traveller_trips (source_country, destination_country);
CREATE INDEX idx_traveller_trips_route_cities ON traveller_trips (source_city, destination_city);
CREATE INDEX idx_traveller_trips_travel_date ON traveller_trips (travel_date);