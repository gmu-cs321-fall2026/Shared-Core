-- 002_seed_logs.sql
-- Sample log rows for local development ONLY. Runs after 001_seed_users.sql.
-- Uses the test users from 001_seed_users.sql.

INSERT INTO logs (log_category, user_id, information, occurred_at) VALUES
  ('registration', '00000000-0000-0000-0000-000000000003', 'New account created',                     '2026-11-02T14:00:00Z'),
  ('auth',         '00000000-0000-0000-0000-000000000003', 'Logged in',                               '2026-11-02T14:01:10Z'),
  ('profile',      '00000000-0000-0000-0000-000000000003', 'Profile updated: major',                  '2026-11-02T14:05:42Z'),
  ('application',  '00000000-0000-0000-0000-000000000003', 'Application added, stage: applied',       '2026-11-03T09:30:00Z'),
  ('connection',   '00000000-0000-0000-0000-000000000003', 'Mentor connection requested',             '2026-11-03T10:15:00Z'),
  ('connection',   '00000000-0000-0000-0000-000000000005', 'Mentor connection accepted',              '2026-11-04T16:20:00Z'),
  ('event',        '00000000-0000-0000-0000-000000000002', 'Event created: Fall Career Fair',         '2026-11-04T11:00:00Z'),
  ('event',        '00000000-0000-0000-0000-000000000004', 'Registered for event: Fall Career Fair',  '2026-11-05T13:45:00Z'),
  ('forum',        '00000000-0000-0000-0000-000000000004', 'Forum post created',                      '2026-11-06T18:02:00Z'),
  ('notification', '00000000-0000-0000-0000-000000000002', 'Broadcast notification sent',             '2026-11-07T08:00:00Z'),
  ('auth',         NULL,                                   'Failed log in: unknown email',            '2026-11-07T08:12:33Z'),
  ('admin',        '00000000-0000-0000-0000-000000000001', 'Resolved flagged forum post',             '2026-11-08T12:00:00Z'),
  ('error',        NULL,                                   'Gateway could not reach Events service',  '2026-11-08T12:30:00Z');
