CREATE TABLE admin_password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_id UUID NOT NULL REFERENCES admins(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_admin_password_reset_token_hash UNIQUE (token_hash)
);

CREATE INDEX idx_admin_password_reset_tokens_admin_id
    ON admin_password_reset_tokens(admin_id);
CREATE INDEX idx_admin_password_reset_tokens_expires_at
    ON admin_password_reset_tokens(expires_at);
CREATE INDEX idx_admin_password_reset_tokens_admin_created_at
    ON admin_password_reset_tokens(admin_id, created_at DESC);
