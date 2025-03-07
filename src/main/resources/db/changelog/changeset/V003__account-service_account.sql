CREATE TABLE IF NOT EXISTS account_number_sequence (
    type VARCHAR(32) NOT NULL PRIMARY KEY,
    counter BIGINT DEFAULT 1 NOT NULL
);

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
            ADD CONSTRAINT balance_fk FOREIGN KEY (account_id) REFERENCES account(id);
    END IF;
END $$;