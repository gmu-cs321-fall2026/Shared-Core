#!/bin/bash
# Runs automatically the FIRST time the database container starts.
# Applies every migration in order, then loads the seed data.
set -e
for f in /db/migrations/*.sql /db/seed/*.sql; do
  echo "Running $f"
  psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -f "$f"
done
