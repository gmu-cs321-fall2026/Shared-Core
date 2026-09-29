-- endpoint_queries.sql
-- The SQL behind each /users endpoint. $1, $2... are values the app code fills in.
-- Useful for practice: replace $1 with a real value and run it in psql.

-- GET /users/{id}  (note: password_hash is NOT selected)
SELECT u.id, u.email, u.roles, p.full_name, p.grad_year, p.major,
       p.employer, p.job_title, p.extensions
FROM users u
JOIN profiles p ON p.user_id = u.id
WHERE u.id = $1;

-- PUT /users/{id}  (only profile fields; roles/status/email are NOT editable here)
UPDATE profiles
SET major = $1, updated_at = now()
WHERE user_id = $2;

-- GET /users/search?role=mentor&industry=finance&page=1&pageSize=20
SELECT u.id, u.roles, p.full_name, p.employer, p.job_title
FROM users u
JOIN profiles p ON p.user_id = u.id
WHERE $1 = ANY(u.roles)
  AND p.extensions -> 'mentor_matching' -> 'industries' ? $2
  AND u.status = 'active'
ORDER BY p.full_name
LIMIT $3 OFFSET $4;          -- LIMIT pageSize OFFSET (page - 1) * pageSize

-- Total count for the same search (goes in the "total" field)
SELECT COUNT(*)
FROM users u
JOIN profiles p ON p.user_id = u.id
WHERE $1 = ANY(u.roles)
  AND p.extensions -> 'mentor_matching' -> 'industries' ? $2
  AND u.status = 'active';

-- POST /auth/login: find the account, then check the password
SELECT id, roles, status
FROM users
WHERE lower(email) = lower($1)
  AND password_hash = crypt($2, password_hash);
