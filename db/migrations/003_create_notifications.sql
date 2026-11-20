-- 003_create_notifications.sql
-- Shared Core defines this table's shape.
-- The Notifications team (Subsystem 6) creates the rows and delivers them.

--Enum types for delivery status and notification channels
CREATE TYPE delivery_status       AS ENUM ('pending', 'sent', 'delivered', 'failed');
CREATE TYPE notification_channel  AS ENUM ('in_app', 'email');

CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE notifications (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type       VARCHAR(50)  NOT NULL,   -- e.g. 'interview_reminder'
    subject    VARCHAR(255) NOT NULL,   -- title of the notification
    message    TEXT         NOT NULL,
    link       VARCHAR(500),            -- optional link into the app
    is_read    BOOLEAN      NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- The inbox query: "my notifications, newest first"
CREATE INDEX notifications_inbox_idx ON notifications (user_id, created_at DESC);

CREATE FUNCTION update_timestamp() RETURNS TRIGGER AS --updates the data in the updated_at column 
$$ BEGIN
    NEW.updated_at := now();
    RETURN NEW;
END $$
LANGUAGE plpgsql;

CREATE TRIGGER notifications_updated_at
    BEFORE INSERT OR UPDATE ON notifications
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

--Table to track delivery attempts for each notification and channel
CREATE TABLE notification_deliveries (
    id              BIGSERIAL PRIMARY KEY,
    notification_id BIGINT NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
    channel         notification_channel NOT NULL,
    status          delivery_status NOT NULL DEFAULT 'pending',
    attempts        SMALLINT NOT NULL DEFAULT 0,
    last_error      TEXT,
    sent_at         TIMESTAMPTZ,
    delivered_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (notification_id, channel)
);

-- Worker queries: find pending/failed deliveries to process
CREATE INDEX idx_deliveries_pending
    ON notification_deliveries (created_at)
    WHERE status IN ('pending', 'failed');

-- Mark a notification as read for a specific user
CREATE OR REPLACE FUNCTION mark_notification_read(p_notification_id BIGINT, p_user_id BIGINT)
RETURNS VOID AS $$
BEGIN
    UPDATE notifications
       SET is_read = true
     WHERE id = p_notification_id
       AND user_id = p_user_id
       AND is_read = false;
END;
$$ LANGUAGE plpgsql;