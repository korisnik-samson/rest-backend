-- If you didn’t add pgcrypto in V1, uncomment the next line
-- CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1) Role enum + column on users table
DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
            CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');
        END IF;
    END$$;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS role user_role NOT NULL DEFAULT 'USER';

CREATE INDEX IF NOT EXISTS idx_users_role ON users (role);

CREATE INDEX IF NOT EXISTS idx_users_role ON users (role);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users (created_at);

-- 2) Idempotency table for safe retries
CREATE TABLE IF NOT EXISTS idempotency_keys (
    idem_key       text PRIMARY KEY,
    request_hash   text NOT NULL,
    status         text NOT NULL CHECK (status IN ('IN_PROGRESS','SUCCEEDED','FAILED')),
    resource_id    uuid,
    response_code  int,
    created_at     timestamptz NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at     timestamptz NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    expires_at     timestamptz NOT NULL DEFAULT ((now() AT TIME ZONE 'utc') + interval '24 hours')
);

CREATE INDEX IF NOT EXISTS idx_idem_keys_expires_at ON idempotency_keys (expires_at);

-- auto-update updated_at
CREATE OR REPLACE FUNCTION set_updated_at_generic()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = (now() AT TIME ZONE 'utc');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_idem_set_updated_at ON idempotency_keys;
CREATE TRIGGER trg_idem_set_updated_at
    BEFORE UPDATE ON idempotency_keys
    FOR EACH ROW EXECUTE FUNCTION set_updated_at_generic();
