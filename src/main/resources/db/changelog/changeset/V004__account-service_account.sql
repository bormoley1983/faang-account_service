CREATE TABLE IF NOT EXISTS account_number_sequence (
    type VARCHAR(32) NOT NULL PRIMARY KEY,
    counter BIGINT NOT NULL DEFAULT 0
);

INSERT INTO account_number_sequence (type)
VALUES ('DEBIT'),
       ('SAVINGS'),
       ('CHECKING'),
       ('CURRENCY')
ON CONFLICT (type) DO NOTHING;

CREATE TABLE IF NOT EXISTS free_account_numbers (
    type VARCHAR(32) NOT NULL,
    account_number BIGINT NOT NULL,

    CONSTRAINT free_acc_pk PRIMARY KEY (type, account_number)
);