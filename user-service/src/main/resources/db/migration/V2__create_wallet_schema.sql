-- V2 : double entry ledger schema

CREATE TABLE accounts (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT REFERENCES users(id),
    account_type    VARCHAR(50) NOT NULL,
    currency        VARCHAR(3) NOT NULL DEFAULT 'INR',
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_account_type CHECK (account_type IN ('USER', 'SYSTEM_DEPOSIT', 'SYSTEM_WITHDRAWAL', 'SYSTEM_FEE'))
);

CREATE INDEX idx_accounts_user_id ON accounts(user_id);

CREATE TABLE transactions (
    id              BIGSERIAL PRIMARY KEY,
    transaction_ref VARCHAR(100) NOT NULL UNIQUE,
    transaction_type VARCHAR(50) NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'COMPLETED',
    description     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER')),
    CONSTRAINT chk_transaction_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REVERSED'))
);

CREATE TABLE ledger_entries (
    id               BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL REFERENCES transactions(id),
    account_id       BIGINT NOT NULL REFERENCES accounts(id),
    entry_type       VARCHAR(10) NOT NULL,
    amount           DECIMAL(19, 4) NOT NULL,
    currency         VARCHAR(3) NOT NULL DEFAULT 'INR',
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_entry_type CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    CONSTRAINT chk_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_ledger_entries_account_id ON ledger_entries(account_id);
CREATE INDEX idx_ledger_entries_transaction_id ON ledger_entries(transaction_id);
CREATE INDEX idx_ledger_entries_created_at ON ledger_entries(created_at);