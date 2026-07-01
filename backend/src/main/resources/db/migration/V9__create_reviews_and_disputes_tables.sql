-- Reviews and disputes

CREATE TABLE reviews (
    id              UUID PRIMARY KEY,
    booking_id      UUID NOT NULL REFERENCES bookings (id) ON DELETE CASCADE,
    reviewer_id     UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    reviewee_id     UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    rating          INT NOT NULL,
    comment         TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_reviews_rating CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT uq_reviews_booking_reviewer_reviewee UNIQUE (booking_id, reviewer_id, reviewee_id)
);

CREATE TABLE disputes (
    id                      UUID PRIMARY KEY,
    booking_id              UUID NOT NULL REFERENCES bookings (id) ON DELETE CASCADE,
    raised_by_user_id       UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    reason                  VARCHAR(50) NOT NULL,
    description             TEXT NOT NULL,
    status                  VARCHAR(30) NOT NULL,
    resolved_by_admin_id    UUID REFERENCES users (id) ON DELETE SET NULL,
    resolution_note         TEXT,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resolved_at             TIMESTAMPTZ,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version                 BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_disputes_reason CHECK (reason IN (
        'ITEM_NOT_DELIVERED', 'WRONG_ITEM', 'DAMAGED_ITEM', 'LATE_DELIVERY',
        'PAYMENT_ISSUE', 'FRAUD_SUSPICION', 'BEHAVIOUR_ISSUE', 'OTHER'
    )),
    CONSTRAINT chk_disputes_status CHECK (status IN ('OPEN', 'UNDER_REVIEW', 'RESOLVED', 'REJECTED'))
);

CREATE INDEX idx_reviews_booking_id ON reviews (booking_id);
CREATE INDEX idx_reviews_reviewer_id ON reviews (reviewer_id);
CREATE INDEX idx_reviews_reviewee_id ON reviews (reviewee_id);
CREATE INDEX idx_reviews_rating ON reviews (rating);

CREATE INDEX idx_disputes_booking_id ON disputes (booking_id);
CREATE INDEX idx_disputes_raised_by_user_id ON disputes (raised_by_user_id);
CREATE INDEX idx_disputes_status ON disputes (status);
CREATE INDEX idx_disputes_resolved_by_admin_id ON disputes (resolved_by_admin_id);
CREATE INDEX idx_disputes_created_at ON disputes (created_at DESC);

-- Extend notification types for review and dispute events
ALTER TABLE notifications DROP CONSTRAINT chk_notifications_type;
ALTER TABLE notifications ADD CONSTRAINT chk_notifications_type CHECK (notification_type IN (
    'KYC_SUBMITTED', 'KYC_APPROVED', 'KYC_REJECTED', 'MATCH_FOUND', 'OFFER_SENT',
    'OFFER_ACCEPTED', 'OFFER_REJECTED', 'OFFER_CANCELLED', 'BOOKING_CREATED',
    'PAYMENT_HELD', 'DELIVERY_CODE_GENERATED', 'DELIVERY_VERIFIED', 'PAYMENT_RELEASED',
    'PAYMENT_REFUNDED', 'DISPUTE_CREATED', 'DISPUTE_UNDER_REVIEW', 'DISPUTE_RESOLVED',
    'DISPUTE_REJECTED', 'REVIEW_RECEIVED', 'SYSTEM_ALERT'));
