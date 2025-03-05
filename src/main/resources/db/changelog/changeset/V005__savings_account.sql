CREATE TABLE savings_account
(
    account_id         BIGINT PRIMARY KEY,
    balance            NUMERIC(15, 2) NOT NULL DEFAULT 0,
    tariff_history     JSONB          NOT NULL DEFAULT '[]',
    last_interest_date DATE,
    version            INT            NOT NULL DEFAULT 0,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_savings_account_account FOREIGN KEY (account_id) REFERENCES account (id)
);

CREATE INDEX idx_savings_account_last_interest_date
ON savings_account (last_interest_date);

CREATE TABLE tariff
(
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    rate_history JSONB        NOT NULL DEFAULT '[]',
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);