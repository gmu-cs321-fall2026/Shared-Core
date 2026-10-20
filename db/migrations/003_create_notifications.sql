-- 003_create_notifications.sql
-- Shared Core defines this table's shape.
-- The Notifications team (Subsystem 6) creates the rows and delivers them.

CREATE TABLE notifications (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type       VARCHAR(50)  NOT NULL,   -- e.g. 'interview_reminder'
    message    TEXT         NOT NULL,
    link       VARCHAR(500),            -- optional link into the app
    is_read    BOOLEAN      NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- The inbox query: "my notifications, newest first"
CREATE INDEX notifications_inbox_idx ON notifications (user_id, created_at DESC);
