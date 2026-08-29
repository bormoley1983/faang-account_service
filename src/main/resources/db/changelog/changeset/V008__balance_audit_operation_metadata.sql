ALTER TABLE balance_audit
    ALTER COLUMN transaction_id TYPE UUID
        USING uuidv7(),
    ALTER COLUMN transaction_id SET NOT NULL;

ALTER TABLE balance_audit
    ADD COLUMN operation VARCHAR(64) NOT NULL DEFAULT 'LEGACY',
    ADD COLUMN outcome VARCHAR(16) NOT NULL DEFAULT 'LEGACY',
    ADD COLUMN failure_reason VARCHAR(255),
    ADD CONSTRAINT chk_balance_audit_outcome
        CHECK (outcome IN ('SUCCESS', 'FAILED', 'LEGACY'));
