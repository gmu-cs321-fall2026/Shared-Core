#!/usr/bin/env bash
# logs-test.sh — SHCOR-45 tests for the logs table.
#
# Run from the repo folder, with the database running (docker compose up -d):
#   bash tests/logs-test.sh
#
# Every test runs inside a transaction that is rolled back,
# so the tests never change the data in your database.

PSQL="${PSQL:-docker compose exec -T db sh -c 'psql -X -q -t -A -v ON_ERROR_STOP=1 -U \"\$POSTGRES_USER\" -d \"\$POSTGRES_DB\"'}"

pass=0
fail=0

run_sql() {
  printf 'BEGIN;\n%s\nROLLBACK;\n' "$1" | eval "$PSQL" 2>&1
}

# check "name" "SQL that returns one value" "expected value"
check() {
  local result
  result=$(run_sql "$2" | tr -d '[:space:]')
  if [ "$result" = "$3" ]; then
    echo "PASS  $1"; pass=$((pass + 1))
  else
    echo "FAIL  $1 (expected '$3', got '$result')"; fail=$((fail + 1))
  fi
}

# expect_error "name" "SQL that the database must reject" "text the error must contain"
expect_error() {
  local result
  result=$(run_sql "$2")
  if echo "$result" | grep -q "$3"; then
    echo "PASS  $1"; pass=$((pass + 1))
  else
    echo "FAIL  $1 (expected an error containing '$3', got '$result')"; fail=$((fail + 1))
  fi
}

JDOE="'00000000-0000-0000-0000-000000000003'"

echo "== Table set-up =="
check "logs table exists" \
  "SELECT to_regclass('public.logs') IS NOT NULL;" "t"

check "logs table has the 5 expected columns" \
  "SELECT string_agg(column_name, ',' ORDER BY ordinal_position) FROM information_schema.columns WHERE table_name = 'logs';" \
  "id,log_category,user_id,information,occurred_at"

check "3 indexes exist (time, user, category)" \
  "SELECT COUNT(*) FROM pg_indexes WHERE tablename = 'logs' AND indexname IN ('logs_occurred_at_idx','logs_user_idx','logs_category_idx');" "3"

check "seed log rows loaded" \
  "SELECT COUNT(*) >= 13 FROM logs;" "t"

echo "== Write and read back =="
check "a log entry can be written and read back" \
  "INSERT INTO logs (log_category, user_id, information) VALUES ('event', $JDOE, 'TEST registered for event');
   SELECT information FROM logs WHERE information = 'TEST registered for event' AND user_id = $JDOE AND log_category = 'event';" \
  "TESTregisteredforevent"

check "id and occurred_at are filled in automatically" \
  "INSERT INTO logs (log_category, information) VALUES ('error', 'TEST default values');
   SELECT id IS NOT NULL AND occurred_at > now() - interval '1 minute' FROM logs WHERE information = 'TEST default values';" "t"

check "a system event with no user is allowed" \
  "INSERT INTO logs (log_category, user_id, information) VALUES ('error', NULL, 'TEST system event');
   SELECT COUNT(*) FROM logs WHERE information = 'TEST system event' AND user_id IS NULL;" "1"

check "a user's logs come back newest first" \
  "INSERT INTO logs (log_category, user_id, information, occurred_at) VALUES
     ('auth', $JDOE, 'TEST older', '2030-01-01T00:00:00Z'),
     ('auth', $JDOE, 'TEST newer', '2030-01-02T00:00:00Z');
   SELECT information FROM logs WHERE user_id = $JDOE ORDER BY occurred_at DESC LIMIT 1;" "TESTnewer"

echo "== Rules the database must enforce =="
expect_error "an unknown category is rejected" \
  "INSERT INTO logs (log_category, information) VALUES ('banana', 'TEST bad category');" \
  "logs_category_valid"

expect_error "missing information is rejected" \
  "INSERT INTO logs (log_category) VALUES ('auth');" \
  "null value"

expect_error "blank information is rejected" \
  "INSERT INTO logs (log_category, information) VALUES ('auth', '   ');" \
  "logs_information_not_blank"

expect_error "a user that doesn't exist is rejected" \
  "INSERT INTO logs (log_category, user_id, information) VALUES ('auth', '99999999-9999-9999-9999-999999999999', 'TEST ghost user');" \
  "foreign key"

check "deleting a user keeps their logs but removes the link" \
  "INSERT INTO users (id, email, password_hash) VALUES ('11111111-1111-1111-1111-111111111111', 'logtest@gmu.edu', 'x');
   INSERT INTO logs (log_category, user_id, information) VALUES ('auth', '11111111-1111-1111-1111-111111111111', 'TEST deleted user');
   DELETE FROM users WHERE id = '11111111-1111-1111-1111-111111111111';
   SELECT COUNT(*) FROM logs WHERE information = 'TEST deleted user' AND user_id IS NULL;" "1"

echo "== Analytics queries =="
check "new registrations by week can be counted" \
  "SELECT COUNT(*) FROM logs WHERE log_category = 'registration' AND date_trunc('week', occurred_at) = '2026-11-02T00:00:00Z';" "1"

check "active users by category can be counted" \
  "SELECT COUNT(DISTINCT user_id) FROM logs WHERE log_category = 'connection' AND user_id IS NOT NULL;" "2"

echo
echo "$pass passed, $fail failed"
[ "$fail" -eq 0 ]
