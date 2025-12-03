-- Persist refresh tokens (rotation & revocation friendly)
CREATE TABLE IF NOT EXISTS refresh_tokens (
    token_id     uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      uuid NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    jwt_id       text NOT NULL,                -- JTI claim
    expires_at   timestamptz NOT NULL,
    revoked      boolean NOT NULL DEFAULT false,
    created_at   timestamptz NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at   timestamptz NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    UNIQUE (jwt_id)
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires ON refresh_tokens(expires_at);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_revoked ON refresh_tokens(revoked);

-- generic updated_at trigger (reuse from V2 if present)
CREATE OR REPLACE FUNCTION set_updated_at_generic()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = (now() AT TIME ZONE 'utc');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_refresh_tokens_set_updated_at ON refresh_tokens;
CREATE TRIGGER trg_refresh_tokens_set_updated_at
    BEFORE UPDATE ON refresh_tokens
    FOR EACH ROW EXECUTE FUNCTION set_updated_at_generic();
