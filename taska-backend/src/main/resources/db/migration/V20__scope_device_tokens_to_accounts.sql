ALTER TABLE device_tokens
    ADD COLUMN IF NOT EXISTS account_subject VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_device_tokens_account_subject
    ON device_tokens(account_subject);
