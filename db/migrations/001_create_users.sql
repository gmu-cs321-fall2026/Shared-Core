-- 001_create_users.sql
-- Account table: login info only. Owned by Shared Core.
-- password_hash must NEVER be returned by any API endpoint.

CREATE EXTENSION IF NOT EXISTS pgcrypto;  -- for gen_random_uuid() and crypt()

CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    roles         TEXT[]       NOT NULL DEFAULT '{student}',
    status        VARCHAR(20)  NOT NULL DEFAULT 'active',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),

    -- Only the four roles from the SOW are allowed, and at least one is required
    CONSTRAINT users_roles_valid CHECK (
        roles <@ ARRAY['student','mentor','staff','admin']::TEXT[]
        AND cardinality(roles) >= 1
    ),
    CONSTRAINT users_status_valid CHECK (status IN ('active','suspended'))
);

-- Emails are unique ignoring upper/lower case (JDoe@gmu.edu = jdoe@gmu.edu)
CREATE UNIQUE INDEX users_email_unique ON users (lower(email));

-- Fast "find all mentors" style searches on the roles list
CREATE INDEX users_roles_gin ON users USING GIN (roles);
