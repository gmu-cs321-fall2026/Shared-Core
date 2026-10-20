-- 002_create_profiles.sql
-- Base profile: one row per user. Owned by Shared Core.
-- Other teams store their extra fields in "extensions", each in its own section:
--   {"mentor_matching": {...}, "tracker": {...}}

CREATE TABLE profiles (
    user_id    UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    full_name  VARCHAR(100) NOT NULL,
    grad_year  INT,
    major      VARCHAR(100),
    employer   VARCHAR(100),   -- mainly for mentors
    job_title  VARCHAR(100),   -- mainly for mentors
    extensions JSONB NOT NULL DEFAULT '{}',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT profiles_grad_year_range CHECK (grad_year IS NULL OR grad_year BETWEEN 1960 AND 2035),
    CONSTRAINT profiles_extensions_is_object CHECK (jsonb_typeof(extensions) = 'object')
);

-- Common search filters
CREATE INDEX profiles_major_idx     ON profiles (major);
CREATE INDEX profiles_grad_year_idx ON profiles (grad_year);

-- Searching inside other teams' extension fields
CREATE INDEX profiles_extensions_gin ON profiles USING GIN (extensions);
