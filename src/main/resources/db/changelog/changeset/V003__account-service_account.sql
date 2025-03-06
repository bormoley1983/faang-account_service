INSERT INTO account_number_sequence (type, counter)
VALUES
    ('DEBIT', 4200000000000000),
    ('SAVINGS', 5536000000000000),
    ('CHECKING', 6000000000000000),
    ('CURRENCY', 7000000000000000)
ON CONFLICT (type) DO UPDATE SET counter = EXCLUDED.counter;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'balance') THEN
        ALTER TABLE balance
            ALTER COLUMN account_number TYPE BIGINT USING account_number::BIGINT;

        ALTER TABLE balance
            ADD CONSTRAINT balance_fk FOREIGN KEY(account_number) REFERENCES account(number);
    END IF;
END $$;