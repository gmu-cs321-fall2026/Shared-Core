# Shared data model v1

> Source of truth: `db/migrations/`. This page explains it in plain language.
> **After Sprint 2, changes require written notice to all teams and the Q&I team.**

## Tables Shared Core owns

### `users` — the account
| Column | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key. Other teams store this as `user_id`. |
| `email` | VARCHAR(255) | Unique, case-insensitive. Used to log in. |
| `password_hash` | VARCHAR(255) | bcrypt hash. **Never returned by any API.** |
| `roles` | TEXT[] | Any of `student`, `mentor`, `staff`, `admin`. A user can have more than one (e.g. a grad student who mentors). |
| `status` | VARCHAR(20) | `active` or `suspended` |
| `created_at`, `updated_at` | TIMESTAMPTZ | |

### `profiles` — public facts about a person (one per user)
| Column | Type | Notes |
|---|---|---|
| `user_id` | UUID | Primary key and foreign key to `users.id` |
| `full_name` | VARCHAR(100) | Required |
| `grad_year` | INT | 1960–2035 |
| `major` | VARCHAR(100) | |
| `employer`, `job_title` | VARCHAR(100) | Mainly for mentors |
| `extensions` | JSONB | Other teams' extra fields, one section per team (see below) |

### `notifications` — shape defined by Shared Core, rows created by the Notifications team
| Column | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `user_id` | UUID | Who receives it |
| `type` | VARCHAR(50) | e.g. `interview_reminder` |
| `message` | TEXT | |
| `link` | VARCHAR(500) | Optional |
| `is_read` | BOOLEAN | |
| `created_at` | TIMESTAMPTZ | |

## The `extensions` field

Each team writes **only in its own section**:

```json
{
  "mentor_matching": { "industries": ["finance"], "guidance_areas": ["resume review"] },
  "tracker": { "target_roles": ["software intern"] }
}
```

Teams: tell Shared Core your section name and fields so we can document them here.

| Section key | Owner team | Fields |
|---|---|---|
| `mentor_matching` | Subsystem 4 | *(to be confirmed)* |
| `tracker` | Subsystem 2 | *(to be confirmed)* |

## Rules for other teams

1. Store `user_id` only. **Do not copy** names, emails, or majors into your own tables; look them up through `GET /users/{id}`.
2. Do not connect to Shared Core's tables directly. Use the API.
3. Don't redefine User, Profile, or Notification in your own schema (SOW architectural constraint).
