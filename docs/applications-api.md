# Applications table and API

This feature adds an `applications` PostgreSQL table and REST endpoints to the existing Spring Boot service. It uses the repository's existing `JdbcTemplate` approach and the shared error response format.

## Implementation process

1. **Fix and define the migration.** PostgreSQL enum and table declarations require parentheses, not braces. The migration defines the five supported statuses, an identity-generated application ID, a restrictive foreign key to `users(id)`, timestamps, a one-application-per-user/job constraint, lookup indexes, and a trigger that refreshes `updated_at` on status changes.
2. **Handle the jobs boundary.** This repository currently has no `jobs` table or migration. The `job_id` column is required, but is not given a foreign key that would make this migration fail on a fresh database. Add a real foreign key once the job subsystem's schema, ownership, and migration ordering are agreed on.
3. **Implement the database layer.** `ApplicationRepository` uses parameterized SQL for creation, user-scoped reads, pagination/counting, and status updates. User scoping means an application belonging to another user is returned as not found.
4. **Expose the REST API.** `ApplicationController` accepts the authenticated user's UUID from `X-User-Id`; it never accepts a user ID in the request body. The API supports create, list/filter, read-one, and status update. The list is paginated (default 20, max 50).
5. **Validate and serialize.** Jakarta validation rejects missing job IDs, oversized resume URLs, invalid page values, and missing statuses. Java enum values correspond to the PostgreSQL enum values. JSON follows camelCase, while SQL stays snake_case.
6. **Apply the migration and exercise the endpoints.** The repository's `db/init.sh` applies SQL files alphabetically when the database is first initialized. Start with `cp .env.example .env`, edit the local secrets, then run `docker compose up -d db`. On an existing persistent volume, initialization scripts do not run again; apply the migration explicitly with `psql` (or reset the development volume only if its data can safely be discarded). Start the Spring Boot service with `./mvnw spring-boot:run`.

## API contract

All requests must include `X-User-Id: <UUID>` supplied by the trusted gateway after authentication. Configure the gateway to strip client-provided copies of this header and set it itself; do not expose this service directly to untrusted callers without equivalent authentication.

- `POST /applications` — body `{"jobId": 123, "resumeUrl": "https://example.test/resume.pdf"}`; returns `201 Created` and the saved record.
- `GET /applications?page=1&pageSize=20&status=submitted` — lists only the caller's applications. `status` is optional; page starts at 1 and pageSize is 1–50.
- `GET /applications/{applicationId}` — fetches one of the caller's applications.
- `PUT /applications/{applicationId}/status` — body `{"status":"under_review"}`; updates status and `updatedAt`.

A successful application representation has `applicationId`, `jobId`, `userId`, `status`, `resumeUrl`, `updatedAt`, and `submittedAt`. Errors use the shared `{error, code, detail}` shape. A duplicate application for the same user and job is rejected by the database uniqueness constraint.

## Sample request

```sh
curl -X POST http://localhost:8080/applications \
  -H 'Content-Type: application/json' \
  -H 'X-User-Id: 00000000-0000-0000-0000-000000000001' \
  -d '{"jobId":123,"resumeUrl":"https://example.test/resume.pdf"}'
```
