CREATE TABLE IF NOT EXISTS free_account_numbers (
    type VARCHAR(32) NOT NULL,
    account_number BIGINT NOT NULL,

    CONSTRAINT free_acc_pk PRIMARY KEY (type, account_number)
);

CREATE TABLE IF NOT EXISTS account_number_sequence (
    type VARCHAR(32) NOT NULL PRIMARY KEY,
    counter BIGINT DEFAULT 1 NOT NULL
);