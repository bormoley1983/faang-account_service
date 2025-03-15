CREATE TABLE balance_audit (
    id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    balance_id bigint NOT NULL,
    version INTEGER NOT NULL,
    authorized_balance DECIMAL(15, 2) NOT NULL,
    actual_balance DECIMAL(15, 2) NOT NULL,
    transaction_id INTEGER,
    created_at timestamptz DEFAULT current_timestamp,

    CONSTRAINT fk_balance_id FOREIGN KEY (balance_id) REFERENCES balance (id) ON DELETE CASCADE
);

CREATE INDEX idx_balance_audit_balance_id on balance_audit(balance_id);