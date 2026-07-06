CREATE TABLE IF NOT EXISTS booking_tracking_sessions (
    id                  UUID PRIMARY KEY,
    booking_id          UUID NOT NULL UNIQUE REFERENCES bookings (id) ON DELETE CASCADE,
    traveller_id        UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    active              BOOLEAN NOT NULL DEFAULT FALSE,
    latitude            DECIMAL(9, 6),
    longitude           DECIMAL(9, 6),
    accuracy_meters     DECIMAL(8, 2),
    heading_degrees     DECIMAL(6, 2),
    speed_kph           DECIMAL(8, 2),
    started_at          TIMESTAMPTZ,
    stopped_at          TIMESTAMPTZ,
    last_location_at    TIMESTAMPTZ,
    stop_reason         VARCHAR(100),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_booking_tracking_sessions_booking_id
    ON booking_tracking_sessions (booking_id);

CREATE INDEX IF NOT EXISTS idx_booking_tracking_sessions_traveller_id
    ON booking_tracking_sessions (traveller_id);

CREATE INDEX IF NOT EXISTS idx_booking_tracking_sessions_active
    ON booking_tracking_sessions (active);
