ALTER TABLE balance
    ADD CONSTRAINT uq_balance_account_id UNIQUE (account_id);
