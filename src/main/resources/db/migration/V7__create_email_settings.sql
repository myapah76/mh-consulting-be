CREATE TABLE IF NOT EXISTS email_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    singleton_key BOOLEAN NOT NULL DEFAULT TRUE UNIQUE CHECK (singleton_key),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    from_email VARCHAR(320) NOT NULL,
    from_name VARCHAR(200) NOT NULL,
    consultation_recipient_email VARCHAR(320) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO email_settings (
    singleton_key,
    enabled,
    from_email,
    from_name,
    consultation_recipient_email
)
SELECT
    TRUE,
    TRUE,
    'info@mhconsulting.vn',
    'MH Consulting',
    'myapah7605@gmail.com'
WHERE NOT EXISTS (SELECT 1 FROM email_settings);
