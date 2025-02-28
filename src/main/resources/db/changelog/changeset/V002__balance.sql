CREATE TABLE balance (
    id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    account_id bigint NOT NULL,
    authorized_balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    actual_balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version integer NOT NULL DEFAULT 1,

    CONSTRAINT fk_account_id
        FOREIGN KEY (account_id)
        REFERENCES account (id)
        ON DELETE CASCADE,

    CHECK (authorized_balance >= 0),
    CHECK (actual_balance >= 0),
    CHECK (version > 0)
);

CREATE OR REPLACE FUNCTION update_balance_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER balance_updated_at_trigger
BEFORE UPDATE ON balance
FOR EACH ROW
EXECUTE FUNCTION update_balance_updated_at();