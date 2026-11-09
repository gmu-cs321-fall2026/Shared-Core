-- logs_queries.sql
-- The SQL other code uses with the logs table. $1, $2... are values the app code fills in.
-- To practice in psql, replace $1 with a real value.

-- 1. Write a log entry (any service)
--    $1 = log_category, $2 = user_id (or NULL), $3 = information
INSERT INTO logs (log_category, user_id, information)
VALUES ($1, $2, $3)
RETURNING id, occurred_at;

-- 2. Newest logs, one page at a time (admin view; pagination per docs/conventions.md)
--    $1 = pageSize (max 50), $2 = page (starts at 1)
SELECT id, log_category, user_id, information, occurred_at
FROM logs
ORDER BY occurred_at DESC
LIMIT $1 OFFSET ($2 - 1) * $1;

SELECT COUNT(*) AS total FROM logs;

-- 3. Everything one user did, newest first (debugging)
--    $1 = user_id
SELECT log_category, information, occurred_at
FROM logs
WHERE user_id = $1
ORDER BY occurred_at DESC
LIMIT 50;

-- 4. Logs of one category between two times
--    $1 = log_category, $2 = start time, $3 = end time
SELECT id, user_id, information, occurred_at
FROM logs
WHERE log_category = $1
  AND occurred_at >= $2 AND occurred_at < $3
ORDER BY occurred_at DESC
LIMIT 50;

-- ===== Analytics (totals only, never individual users) =====

-- 5. New registrations by week
SELECT date_trunc('week', occurred_at) AS week_start,
       COUNT(*)                        AS new_registrations
FROM logs
WHERE log_category = 'registration'
GROUP BY week_start
ORDER BY week_start;

-- 6. Active users by subsystem (category) per week
SELECT date_trunc('week', occurred_at) AS week_start,
       log_category,
       COUNT(DISTINCT user_id)         AS active_users
FROM logs
WHERE user_id IS NOT NULL
GROUP BY week_start, log_category
ORDER BY week_start, log_category;
