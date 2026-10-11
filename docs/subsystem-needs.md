# What each subsystem needs from Shared Core

> Every team builds on Shared Core. This page lists what we provide, which teams use it, and when it's ready.
> **Teams: if your row is wrong or missing something, tell Shared Core.**
>
> Status: ✅ available now · 🟡 planned

## Shared Core services

| Service | How to use it | Status |
|---|---|---|
| **Log in** | `POST /auth/login` → returns a token | ✅ Available |
| **Check a token** | `GET /auth/validate` with `Authorization: Bearer <token>` → returns `userId`, `roles`, `status` | ✅ Available |
| **Register** | `POST /auth/register` | 🟡 Sprint 1 (in progress) |
| **Log out** | `POST /auth/logout` | 🟡 Sprint 2 |
| **Roles (RBAC)** | `roles` comes back from `/auth/validate`; a shared role-check helper is planned | ✅ Roles returned · 🟡 helper in Sprint 2 |
| **Get a user's profile** | `GET /users/{id}` | 🟡 Sprint 2 |
| **Update a profile** | `PUT /users/{id}` | 🟡 Sprint 2 |
| **Search users** | `GET /users/search?role=...&major=...` | 🟡 Sprint 2 |
| **API gateway** | One entry point that routes requests to each team | 🟡 Sprint 3 |
| **Conventions** | Error format, pagination, logging — see the *Shared Core – Conventions* page | ✅ Available |
| **Shared data model** | `users`, `profiles`, `notifications` tables | ✅ v1 |
| **Integration environment** | One `docker-compose` file running all 8 services | 🟡 Sprint 5 |

## Which team needs what

| Team | Check token | Roles | Get profile | Search users | Change users | Profile extension |
|---|:-:|:-:|:-:|:-:|:-:|:-:|
| 2 · Application Tracker | ✓ | | ✓ | | | ✓ `tracker` |
| 3 · Events & Resources | ✓ | ✓ | | | | |
| 4 · Mentor Matching | ✓ | ✓ | ✓ | ✓ | | ✓ `mentor_matching` |
| 5 · Community Forum | ✓ | ✓ | ✓ | | | |
| 6 · Notifications | ✓ | | ✓ | | | |
| 7 · Admin & Analytics | ✓ | ✓ | ✓ | ✓ | ✓ | |
| 8 · Mock Interview | ✓ | | ✓ | | | |
| Quality & Integration | Tests all of the above | | | | | |

**Every team** also uses: the API gateway, the shared error format, the pagination format, and a slot in the Sprint 5 integration environment.

## Notes for each team

**2 · Application Tracker**
- Store only `user_id` in your tables; get names and majors from `GET /users/{id}`.
- Extra profile fields (e.g. target roles) go in your own `tracker` section of `profiles.extensions`. Tell us the field names.

**3 · Events & Resources**
- Use `roles` from `/auth/validate` to allow only `staff` and `admin` to create events.
- ⚠️ Your endpoint `GET /users/{userId}/events/registered` uses our `/users` path. Could it be renamed to `/events/registered/{userId}`? Otherwise the gateway can't tell whose it is.

**4 · Mentor Matching**
- `GET /users/search?role=mentor` finds mentors. Industry filters will read your `mentor_matching` section of `profiles.extensions`.
- Tell us your extension field names (e.g. `industries`, `guidance_areas`) so we can index them for fast search.

**5 · Community Forum**
- `GET /users/{id}` gives post authors' names. Use `roles` to allow `staff` to pin and moderate.

**6 · Notifications**
- `GET /users/{id}` gives each user's email address.
- Shared Core defines the `notifications` table; your team creates the rows and owns `POST /notifications/dispatch`. The gateway will route it to you.

**7 · Admin & Analytics**
- You need to change roles and suspend accounts. ❓ Open question: does your `PUT /admin/users/{id}` call a Shared Core endpoint, or should Shared Core provide an admin-only endpoint for this? Let's decide together.
- A suspended user is blocked immediately: `/auth/validate` returns `403 ACCOUNT_SUSPENDED`.

**8 · Mock Interview**
- `GET /users/{id}` for practice partners' names.

**Quality & Integration**
- Test users are loaded automatically by `docker compose up -d` in the Shared Core repo (password for all: `ChangeMe123!`).
- `tests/auth-test.sh` in the Shared Core repo checks login and validation.

## How to check who is making a request (all teams)

1. The website sends the user's token in the header: `Authorization: Bearer <token>`
2. Your service calls `GET /auth/validate` with the same header
3. You get back `{"userId": "...", "roles": ["student"], "status": "active"}`
4. `401` = not logged in → reject. `403` = suspended → reject.

Full details: see the *Shared Core – Auth API* page.
