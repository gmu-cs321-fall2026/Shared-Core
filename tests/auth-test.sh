#!/bin/bash
# Checks every acceptance criterion for POST /auth/login and GET /auth/validate.
# Run from the repo root, with the database AND the service running:
#   bash tests/auth-test.sh

URL="${URL:-http://localhost:8080}"
PASS=0; FAIL=0

# check "description" expected_status actual_status
check() {
  if [ "$2" = "$3" ]; then
    echo "PASS  $1 (got $3)"; PASS=$((PASS+1))
  else
    echo "FAIL  $1 (expected $2, got $3)"; echo "      response: $(cat /tmp/auth-test-body)"; FAIL=$((FAIL+1))
  fi
}
# login '{"email":...}'  -> prints HTTP status, saves body to /tmp/auth-test-body
login() {
  curl -s -o /tmp/auth-test-body -w "%{http_code}" -X POST "$URL/auth/login" \
       -H "Content-Type: application/json" -d "$1"
}
# validate "Authorization header value" (empty = no header)
validate() {
  if [ -z "$1" ]; then
    curl -s -o /tmp/auth-test-body -w "%{http_code}" "$URL/auth/validate"
  else
    curl -s -o /tmp/auth-test-body -w "%{http_code}" "$URL/auth/validate" -H "Authorization: $1"
  fi
}
psql_run() {
  docker compose exec -T db psql -q -U "${POSTGRES_USER:-sharedcore}" -d "${POSTGRES_DB:-careerlaunch}" -c "$1" > /dev/null
}

echo "== POST /auth/login"
check "right email + password"         200 "$(login '{"email":"jdoe@gmu.edu","password":"ChangeMe123!"}')"
TOKEN=$(sed -E 's/.*"token" *: *"([^"]+)".*/\1/' /tmp/auth-test-body)
grep -q '"expiresIn"' /tmp/auth-test-body && check "response has expiresIn" yes yes || check "response has expiresIn" yes no
grep -qi 'password' /tmp/auth-test-body && check "response hides password hash" no yes || check "response hides password hash" no no
check "capital letters in email"       200 "$(login '{"email":"JDoe@GMU.edu","password":"ChangeMe123!"}')"
check "wrong password"                 401 "$(login '{"email":"jdoe@gmu.edu","password":"wrong123"}')"
MSG_WRONG_PW=$(cat /tmp/auth-test-body)
check "email that doesn't exist"       401 "$(login '{"email":"nobody@gmu.edu","password":"ChangeMe123!"}')"
MSG_NO_USER=$(cat /tmp/auth-test-body)
[ "$MSG_WRONG_PW" = "$MSG_NO_USER" ] && check "same message for both 401s" yes yes || check "same message for both 401s" yes no
check "missing password"               400 "$(login '{"email":"jdoe@gmu.edu"}')"
check "not valid JSON"                 400 "$(login 'hello')"

echo "== GET /auth/validate"
check "valid token"                    200 "$(validate "Bearer $TOKEN")"
grep -q '"userId"' /tmp/auth-test-body && grep -q '"roles"' /tmp/auth-test-body && grep -q '"status"' /tmp/auth-test-body \
  && check "response has userId, roles, status" yes yes || check "response has userId, roles, status" yes no
check "no Authorization header"        401 "$(validate "")"
check "changed token"                  401 "$(validate "Bearer ${TOKEN}x")"
check "not a token"                    401 "$(validate "Bearer hello")"

echo "== Suspended account"
psql_run "UPDATE users SET status='suspended' WHERE email='jdoe@gmu.edu'"
check "validate with suspended user"   403 "$(validate "Bearer $TOKEN")"
check "login with suspended user"      403 "$(login '{"email":"jdoe@gmu.edu","password":"ChangeMe123!"}')"
psql_run "UPDATE users SET status='active' WHERE email='jdoe@gmu.edu'"
check "works again after un-suspending" 200 "$(validate "Bearer $TOKEN")"

echo
echo "$PASS passed, $FAIL failed"
[ "$FAIL" = 0 ]
