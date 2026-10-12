# Shared conventions (all teams)

> Every subsystem follows these rules. Required on the project wiki by the end of Sprint 1.

## 1. Error format

Every error response, from every subsystem, uses this JSON shape:

```json
{
  "error": "Graduation year is invalid",
  "code": "VALIDATION_ERROR",
  "detail": "gradYear must be between 1960 and 2035"
}
```

- `error`: short message a person can read
- `code`: fixed label from the table below, always UPPER_SNAKE_CASE
- `detail`: more specifics. For bad input, may be an object listing each field: `{"email": "required", "gradYear": "must be a number"}`

| HTTP status | `code` | When |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Input missing or wrong |
| 401 | `UNAUTHORIZED` | Not logged in, bad or expired token |
| 403 | `FORBIDDEN` | Logged in, but not allowed |
| 403 | `ACCOUNT_SUSPENDED` | Account has been suspended by an admin |
| 404 | `NOT_FOUND` | Resource doesn't exist |
| 409 | `CONFLICT` | Duplicate, e.g. email already registered |
| 429 | `RATE_LIMITED` | Too many requests |
| 500 | `INTERNAL_ERROR` | Unexpected server error (never show stack traces) |
| 503 | `SERVICE_DOWN` | Gateway can't reach a subsystem |
| 504 | `TIMEOUT` | Subsystem took too long |

Teams may add their own codes for their own situations (e.g. `EVENT_FULL`), but must use the ones above where they apply.

## 2. Pagination

Every list endpoint is paginated. No endpoint returns an unlimited list.

**Request:** `GET /events?page=2&pageSize=20`

- `page` starts at **1**, default 1
- `pageSize` default **20**, maximum **50**. Values above 50 return 400 `VALIDATION_ERROR`.

**Response:**

```json
{
  "data": [ ... ],
  "page": 2,
  "pageSize": 20,
  "total": 134
}
```

SQL: `LIMIT pageSize OFFSET (page - 1) * pageSize`, plus a `COUNT(*)` query for `total`.

## 3. Logging

- One log line per event, as JSON
- Fields: `time` (UTC), `service`, `level`, `requestId`, `message`
- Levels: `ERROR` failures · `WARN` suspicious · `INFO` normal events · `DEBUG` development only
- The gateway sets an `X-Request-Id` header on every request. Pass it along and log it.
- **Never log** passwords, tokens, or password hashes.

```json
{"time": "2026-10-05T14:03:22Z", "service": "mentor-matching", "level": "ERROR", "requestId": "a91c...", "message": "Failed to load mentor list"}
```

## 4. Validation

Check input before using it. Shared rules:

| Field type | Rule |
|---|---|
| Email | Valid email format, max 255 characters, compared case-insensitively |
| Password | At least 10 characters |
| IDs | Must be a valid UUID, otherwise 400 |
| Dates/times | ISO 8601 in UTC, e.g. `2026-10-05T14:03:22Z` |
| Graduation year | Integer between 1960 and 2035 |

## 5. Naming

- **JSON fields:** camelCase (`gradYear`, `fullName`)
- **Database columns:** snake_case (`grad_year`, `full_name`)
- **URLs:** lowercase, plural nouns (`/users`, `/events/{id}`)
- **IDs:** UUIDs everywhere

## 6. Auth headers

- Clients send: `Authorization: Bearer <token>`
- After the gateway validates a token, it forwards `X-User-Id` and `X-User-Roles` to the subsystem. *(Final design pending the tech stack decision.)*
