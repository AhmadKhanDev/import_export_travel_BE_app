-- Audit logs for admin and operational activity
CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY,
    actor_user_id   UUID REFERENCES users (id) ON DELETE SET NULL,
    action          VARCHAR(100) NOT NULL,
    entity_type     VARCHAR(50) NOT NULL,
    entity_id       UUID,
    metadata        TEXT,
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(512),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_actor_user_id ON audit_logs (actor_user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs (action);
CREATE INDEX idx_audit_logs_entity_type ON audit_logs (entity_type);
CREATE INDEX idx_audit_logs_entity_id ON audit_logs (entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at DESC);

-- KYC documents (minimal table for traveller verification workflow)
CREATE TABLE kyc_documents (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    status          VARCHAR(30) NOT NULL,
    CONSTRAINT chk_kyc_documents_status CHECK (
        status IN ('NOT_SUBMITTED', 'PENDING_REVIEW', 'APPROVED', 'REJECTED', 'EXPIRED')
    )
);

CREATE INDEX idx_kyc_documents_user_id ON kyc_documents (user_id);
CREATE INDEX idx_kyc_documents_status ON kyc_documents (status);
