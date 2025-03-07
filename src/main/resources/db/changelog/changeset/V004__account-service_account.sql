CREATE TABLE IF NOT EXISTS free_account_numbers (
    type VARCHAR(32) NOT NULL,
    account_number BIGINT NOT NULL,

    CONSTRAINT free_acc_pk PRIMARY KEY (type, account_number)
);