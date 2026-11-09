-- 004_create_logs.sql
-- SHCOR-45: Logs table.
-- Based on the LOGS entity in the new Database Design uploaded by Ethan
--   log_category, UUID_usr (fk), information, occured_at
--
-- One row per user action or system event. Used for:
-- 1. Debugging: what happened, to whom, and when 
-- 2. Usage analytics: aggregate counts by category and by week

-- NEVER store passwords, password hashes, or login tokens in "information".

CREATE TABLE logs (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Which area of the platform the event came from (the design's log_category enum).
    -- Stored as text + CHECK, like users.status, so Java code can insert a plain string.
    log_category VARCHAR(30)  NOT NULL,

    -- The user the action belongs to (the design's UUID_usr).
    -- NULL for system events with no user, e.g. a failed job or a startup error.
    -- If a user is deleted, their logs stay (needed for totals) but lose the link.
    user_id      UUID         REFERENCES users(id) ON DELETE SET NULL,

    -- What happened, in plain words. Example: 'Registered for event Career Fair 2026'
    information  TEXT         NOT NULL,

    -- When it happened, in UTC (the design's occured_at).
    occurred_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT logs_category_valid CHECK (log_category IN ( 
    -- there is no log_category in the database design yet, I just used some dafult requirements in here
        'auth',          -- log in, log out, failed log in
        'registration',  -- new account created
        'profile',       -- profile created or edited
        'application',   -- Application Tracker: application added, stage changed
        'connection',    -- Mentor Matching: request sent, accepted, completed
        'event',         -- Events: event created, user registered
        'forum',         -- Forum: post, reply, flag
        'notification',  -- Notifications: sent, opened
        'admin',         -- admin actions: suspend user, change role, resolve flag
        'error'          -- system errors
    )),
    CONSTRAINT logs_information_not_blank CHECK (length(trim(information)) > 0)
);

-- "Newest logs first" and "logs between two dates"
CREATE INDEX logs_occurred_at_idx ON logs (occurred_at DESC);

-- "Everything this user did, newest first" (debugging, admin view)
CREATE INDEX logs_user_idx ON logs (user_id, occurred_at DESC);

-- "How many registrations / forum posts per week" (analytics dashboard)
CREATE INDEX logs_category_idx ON logs (log_category, occurred_at DESC);
