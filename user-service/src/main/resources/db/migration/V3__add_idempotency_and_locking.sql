-- V3: Idempotency support + optimistic locking on accounts
CREATE TABLE idempotency_keys (
    id                  BIGSERIAL PRIMARY KEY,
    idempotency_key    VARCHAR(100) NOT NULL UNIQUE,
    request_hash        VARCHAR(64) NOT NULL,
    response_body       TEXT NOT NULL,
    response_status     INTEGER NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at          TIMESTAMPTZ NOT NULL DEFAULT (now() + INTERVAL '24 hours')
);

CREATE INDEX idx_idempotency_keys_expired ON idempotency_keys(expires_at);

ALTER TABLE accounts ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
