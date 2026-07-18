ALTER TABLE email_settings
    ADD COLUMN IF NOT EXISTS smtp_username VARCHAR(320),
    ADD COLUMN IF NOT EXISTS smtp_password_encrypted VARCHAR(1000);
