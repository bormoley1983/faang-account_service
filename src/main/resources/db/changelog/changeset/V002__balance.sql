CREATE TABLE balance (
    id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    account_id bigint NOT NULL,
    authorized_balance DECIMAL(15, 2) NOT NULL,
    actual_balance DECIMAL(15, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version integer NOT NULL,

    CONSTRAINT fk_account_id FOREIGN KEY (account_id) REFERENCES account (id) ON DELETE CASCADE
);