-- Users table (core auth identity)
CREATE TABLE users (
    id              UUID PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    phone_number    VARCHAR(50),
    role            VARCHAR(50) NOT NULL,
    account_status  VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    profile_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('BUYER', 'TRAVELLER', 'ADMIN')),
    CONSTRAINT chk_users_account_status CHECK (account_status IN ('ACTIVE', 'DISABLED', 'PENDING'))
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_users_account_status ON users (account_status);
