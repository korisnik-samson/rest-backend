-- Enable case-insensitive text for email
CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE IF NOT EXISTS users (
    user_id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    username        varchar(50)  NOT NULL,
    email           citext       NOT NULL, -- case-insensitive
    hashed_password varchar(72)  NOT NULL, -- bcrypt fits in 60 chars, leave headroom
    created_at      timestamptz  NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at      timestamptz  NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    version         bigint       NOT NULL DEFAULT 0,
    roles           varchar(5)  NOT NULL DEFAULT 'USER'
);

-- Uniqueness and lookup indexes
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint WHERE conname = 'uk_users_email'
        ) THEN
            ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint WHERE conname = 'uk_users_username'
        ) THEN
            ALTER TABLE users ADD CONSTRAINT uk_users_username UNIQUE (username);
        END IF;
    END $$;


CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);

-- Trigger to auto-update updated_at on row change (optional but handy)
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = (now() AT TIME ZONE 'utc');
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_users_set_updated_at ON users;
CREATE TRIGGER trg_users_set_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION set_updated_at();

SELECT column_name FROM information_schema.columns
WHERE table_name = 'users';
