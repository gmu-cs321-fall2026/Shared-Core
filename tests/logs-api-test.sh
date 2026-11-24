#!/usr/bin/env bash
# Tests for the Logs Query API (GET /logs, GET /logs/stats).
# Needs: the database running (docker compose up -d) AND the service running (./mvnw spring-boot:run).
# Run from the repo folder:  bash tests/logs-api-test.sh
#
# The API's numbers are compared with COUNT(*) run straight on the database,
# so the tests still work if the seed data changes. Nothing is written to the database.

BASE="${BASE_URL:-http://localhost:8080}"
PASSWORD='ChangeMe123!'
JDOE='00000000-0000-0000-0000-000000000003'

# Same database command as tests/logs-test.sh
PSQL="${PSQL:-docker compose exec -T db sh -c 'psql -U \"\$POSTGRES_USER\" -d \"\$POSTGRES_DB\" -X -q -t -A'}"
db() { echo "$1" | eval "$PSQL" | tr -d '[:space:]'; }

pass=0; fail=0
ok()  { echo "PASS  $1"; pass=$((pass+1)); }
bad() { echo "FAIL  $1"; echo "      expected: $2"; echo "      got:      $3"; fail=$((fail+1)); }

# call URL [token] -> sets $STATUS and $BODY
call() {
  local out
  if [ -n "$2" ]; then out=$(curl -s -w '\n%{http_code}' -H "Authorization: Bearer $2" "$BASE$1")
  else                 out=$(curl -s -w '\n%{http_code}' "$BASE$1"); fi
  STATUS=$(echo "$out" | tail -n 1)
  BODY=$(echo "$out" | sed '$d')
}
total() { echo "$BODY" | grep -o '"total":[0-9]*' | cut -d: -f2; }
items() { echo "$BODY" | grep -o '"information":' | wc -l | tr -d ' '; }
code()  { echo "$BODY" | grep -o '"code":"[A-Z_]*"' | cut -d'"' -f4; }

expect_status() {  # name, url, token, status, [code]
  call "$2" "$3"
  local got="$STATUS $(code)"; local want="$4 ${5:-}"
  if [ "$STATUS" = "$4" ] && { [ -z "$5" ] || [ "$(code)" = "$5" ]; }; then ok "$1"; else bad "$1" "$want" "$got"; fi
}
expect_total() {   # name, url, sql
  call "$2" "$ADMIN"; local want; want=$(db "$3")
  if [ "$STATUS" = "200" ] && [ "$(total)" = "$want" ]; then ok "$1"; else bad "$1" "200, total $want" "$STATUS, total $(total)"; fi
}

login() {
  curl -s -H 'Content-Type: application/json' \
       -d "{\"email\":\"$1\",\"password\":\"$PASSWORD\"}" "$BASE/auth/login" |
    sed -n 's/.*"token":"\([^"]*\)".*/\1/p'
}

echo "== Setup =="
ADMIN=$(login admin@gmu.edu)
STUDENT=$(login jdoe@gmu.edu)
if [ -z "$ADMIN" ] || [ -z "$STUDENT" ]; then
  echo "Could not log in. Is the service running on $BASE and are the test users seeded?"; exit 1
fi
if [ -z "$(db 'SELECT COUNT(*) FROM logs')" ]; then
  echo "Could not reach the database. Is 'docker compose up -d' running?"; exit 1
fi
echo "Logged in as admin and as a student"

echo "== Who can see logs =="
expect_status "no token -> 401"                 "/logs" ""            401 UNAUTHORIZED
expect_status "fake token -> 401"               "/logs" "not.a.token" 401 UNAUTHORIZED
expect_status "student -> 403"                  "/logs" "$STUDENT"    403 FORBIDDEN
expect_status "student on stats -> 403"         "/logs/stats" "$STUDENT" 403 FORBIDDEN
expect_status "admin -> 200"                    "/logs" "$ADMIN"      200

echo "== GET /logs filters =="
expect_total "no filter: total matches the table"  "/logs" \
  "SELECT COUNT(*) FROM logs"
expect_total "category filter"                     "/logs?category=auth" \
  "SELECT COUNT(*) FROM logs WHERE log_category = 'auth'"
expect_total "userId filter"                       "/logs?userId=$JDOE" \
  "SELECT COUNT(*) FROM logs WHERE user_id = '$JDOE'"
expect_total "from/to filter"                      "/logs?from=2026-01-01T00:00:00Z&to=2027-01-01T00:00:00Z" \
  "SELECT COUNT(*) FROM logs WHERE occurred_at >= '2026-01-01Z' AND occurred_at < '2027-01-01Z'"
expect_total "all filters together"                "/logs?category=auth&userId=$JDOE&from=2026-01-01T00:00:00Z" \
  "SELECT COUNT(*) FROM logs WHERE log_category = 'auth' AND user_id = '$JDOE' AND occurred_at >= '2026-01-01Z'"
expect_total "category is case-insensitive"        "/logs?category=AUTH" \
  "SELECT COUNT(*) FROM logs WHERE log_category = 'auth'"

call "/logs" "$ADMIN"
want=$(db "SELECT information FROM logs ORDER BY occurred_at DESC, id DESC LIMIT 1")
got=$(echo "$BODY" | sed -n 's/^{"data":\[{[^}]*"information":"\([^"]*\)".*/\1/p' | tr -d '[:space:]')
[ "$got" = "$want" ] && ok "newest entry comes first" || bad "newest entry comes first" "$want" "$got"

echo "== Pagination =="
call "/logs" "$ADMIN"
echo "$BODY" | grep -q '"page":1,"pageSize":20' && ok "defaults are page 1, pageSize 20" \
  || bad "defaults are page 1, pageSize 20" '"page":1,"pageSize":20' "$BODY"

call "/logs?pageSize=3" "$ADMIN"
want=$(db "SELECT LEAST(COUNT(*), 3) FROM logs")
[ "$(items)" = "$want" ] && ok "pageSize=3 returns at most 3 entries" || bad "pageSize=3 returns at most 3 entries" "$want" "$(items)"

call "/logs?pageSize=3&page=2" "$ADMIN"
want=$(db "SELECT GREATEST(LEAST(COUNT(*) - 3, 3), 0) FROM logs")
[ "$(items)" = "$want" ] && ok "page 2 has the next entries" || bad "page 2 has the next entries" "$want" "$(items)"

call "/logs?page=9999" "$ADMIN"
[ "$STATUS" = "200" ] && [ "$(items)" = "0" ] && ok "a page past the end is empty, not an error" \
  || bad "a page past the end is empty, not an error" "200, 0 entries" "$STATUS, $(items) entries"

echo "== Bad input -> 400 VALIDATION_ERROR =="
expect_status "pageSize over 50"         "/logs?pageSize=51"            "$ADMIN" 400 VALIDATION_ERROR
expect_status "page 0"                   "/logs?page=0"                 "$ADMIN" 400 VALIDATION_ERROR
expect_status "page not a number"        "/logs?page=abc"               "$ADMIN" 400 VALIDATION_ERROR
expect_status "unknown category"         "/logs?category=banana"        "$ADMIN" 400 VALIDATION_ERROR
expect_status "bad userId"               "/logs?userId=123"             "$ADMIN" 400 VALIDATION_ERROR
expect_status "bad date"                 "/logs?from=yesterday"         "$ADMIN" 400 VALIDATION_ERROR
expect_status "from later than to"       "/logs?from=2026-12-01T00:00:00Z&to=2026-11-01T00:00:00Z" "$ADMIN" 400 VALIDATION_ERROR
expect_status "SQL in a filter is rejected, not run" "/logs?category=auth%27%20OR%201=1--" "$ADMIN" 400 VALIDATION_ERROR

echo "== GET /logs/stats =="
call "/logs/stats?groupBy=category" "$ADMIN"
want=$(db "SELECT COUNT(*) FROM logs WHERE log_category = 'auth'")
got=$(echo "$BODY" | grep -o '"key":"auth","count":[0-9]*' | cut -d: -f3)
[ "$STATUS" = "200" ] && [ "$got" = "$want" ] && ok "count by category matches the table" \
  || bad "count by category matches the table" "200, auth=$want" "$STATUS, auth=$got"

call "/logs/stats?groupBy=week" "$ADMIN"
want=$(db "SELECT COUNT(DISTINCT date_trunc('week', occurred_at AT TIME ZONE 'UTC')) FROM logs")
got=$(echo "$BODY" | grep -o '"key":"' | wc -l | tr -d ' ')
[ "$STATUS" = "200" ] && [ "$got" = "$want" ] && ok "one row per week" \
  || bad "one row per week" "200, $want weeks" "$STATUS, $got weeks"

call "/logs/stats" "$ADMIN"
echo "$BODY" | grep -q '"groupBy":"category"' && ok "groupBy defaults to category" \
  || bad "groupBy defaults to category" '"groupBy":"category"' "$BODY"

echo "$BODY" | grep -qiE 'userId|information|@' && bad "stats never show users or log text" "no user data" "$BODY" \
  || ok "stats never show users or log text"

expect_status "unknown groupBy -> 400"   "/logs/stats?groupBy=month"  "$ADMIN" 400 VALIDATION_ERROR

echo
echo "$pass passed, $fail failed"
[ "$fail" -eq 0 ]
