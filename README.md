# Notice

Our documentation is undergoing changes. For the database design, you can find it in wiki/III_database_design.md

# Shared Core — Mason CareerLaunch (Subsystem 1)

Shared Core is the foundation every other team builds on. We own:

- **Authentication and roles**: sign up, log in, log out, token check, and the four roles (student, mentor, staff, admin)
- **Shared data models**: the `users`, `profiles`, and `notifications` tables
- **API gateway**: the single entry point that routes requests to each team
- **Conventions**: the error format, pagination, logging, and naming rules all teams follow

## Repository layout

| Folder | What's inside |
|---|---|
| `docs/` | Tech stack decision, conventions, data model, API contracts, open questions |
| `docs/api/` | One file per group of endpoints (`auth.md`, `users.md`) |
| `docs/ai-log/` | Prompt log for AI-assisted work (required by the SOW) |
| `db/migrations/` | SQL that builds the database, run in number order |
| `db/seed/` | Test users for local development |
| `db/queries/` | The SQL behind each endpoint, for reference and practice |
| `src/main/java/` | Java service code (Spring Boot) |
| `src/main/resources/` | `application.properties`: service and database settings |
| `tests/` | Automated tests (CI required by Sprint 4) |

## Run the database locally

You need [Docker Desktop](https://www.docker.com/products/docker-desktop/).

```bash
cp .env.example .env        # then edit the password in .env
docker compose up -d        # starts PostgreSQL and runs all migrations + seed data
docker compose exec db psql -U sharedcore -d careerlaunch
```

Inside `psql`, try:

```sql
SELECT u.email, u.roles, p.full_name FROM users u JOIN profiles p ON p.user_id = u.id;
```

To wipe the database and rebuild it from the migrations: `docker compose down -v` then `docker compose up -d`.

## Run the service

You need Java 21 or newer, and the database running (see above).

```bash
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

The first run downloads libraries, which takes a few minutes. When you see `Started SharedcoreApplication`, open a second terminal and run:

```bash
curl http://localhost:8080/health
```

You should get `{"status":"ok","database":"up"}`. Press **Ctrl+C** in the first terminal to stop the service.

The service reads the database settings from your `.env` file, so run it from the repo root folder.

## Windows notes

- **Start the service:** in PowerShell, use `.\mvnw.cmd spring-boot:run` (with `.\` in front).
- **Test /health:** use `curl.exe http://localhost:8080/health`. Plain `curl` in PowerShell hides the response.
- **Run the test script:** open a **Git Bash** terminal (VS Code terminal panel → ∨ → Git Bash), then
  `bash tests/auth-test.sh`. Typing `bash` in PowerShell runs WSL instead and fails.
- **"Failed to obtain JDBC Connection" with PostgreSQL already installed on your computer:**
  check with `Get-Service | findstr /i postgres`. If one is listed, add `DB_PORT=5433` to your `.env`,
  then run `docker compose down -v` and `docker compose up -d`.
- **After changing `.env` or the files in `db/`,** always rebuild the database:
  `docker compose down -v`, then `docker compose up -d`.

## Rules for changing the database

After Sprint 2, any change to `users`, `profiles`, or `notifications` needs **written notice to all teams and the Q&I team**. Never edit an old migration file; always add a new numbered one (e.g. `004_add_linkedin_url.sql`). Adding a column is usually safe. Renaming or deleting one breaks other teams.
