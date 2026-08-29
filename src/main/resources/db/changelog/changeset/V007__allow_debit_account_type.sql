ALTER TABLE account
    DROP CONSTRAINT account_type_check;

ALTER TABLE account
    ADD CONSTRAINT account_type_check
        CHECK (type IN ('CHECKING', 'SAVINGS', 'CURRENCY', 'DEBIT'));
