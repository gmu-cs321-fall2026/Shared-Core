# Shared Core diagrams

Three diagrams that explain how Shared Core fits into Mason CareerLaunch.

## 1. Architecture: where Shared Core sits

Every request from the website goes through the **API gateway**. Each subsystem checks the user's token with **Shared Core's auth service**. Only Shared Core reads and writes the shared user tables.

```mermaid
flowchart TB
    WEB["Website (frontend)"] --> GW["API gateway<br/>(Shared Core, Sprint 3)"]

    GW --> S2["2 · Application Tracker"]
    GW --> S3["3 · Events & Resources"]
    GW --> S4["4 · Mentor Matching"]
    GW --> S5["5 · Community Forum"]
    GW --> S6["6 · Notifications"]
    GW --> S7["7 · Admin & Analytics"]
    GW --> S8["8 · Mock Interview"]

    subgraph CORE["Shared Core service"]
        AUTH["Auth<br/>login · validate · register"]
        USERS["Users<br/>profiles · search"]
    end

    GW --> AUTH
    GW --> USERS
    S2 & S3 & S4 & S5 & S6 & S7 & S8 -. "check token<br/>GET /auth/validate" .-> AUTH

    AUTH --> DB[("PostgreSQL<br/>users · profiles · notifications")]
    USERS --> DB
```

## 2. Login and token check: what happens step by step

Example: Jane logs in, then writes a forum post. Login happens **once**; the token check happens on **every** request after that.

```mermaid
sequenceDiagram
    actor Jane as Jane (student)
    participant Web as Website
    participant GW as API gateway
    participant Core as Shared Core
    participant DB as PostgreSQL
    participant Forum as Forum service

    Jane->>Web: Types email and password
    Web->>GW: POST /auth/login
    GW->>Core: POST /auth/login
    Core->>DB: Find user where email and password hash match
    DB-->>Core: id, roles, status
    Core-->>Web: token (JWT), expires in 1 hour
    Note over Web: Saves the token

    Jane->>Web: Writes a forum post
    Web->>GW: POST /posts + token
    GW->>Forum: POST /posts + token
    Forum->>Core: GET /auth/validate + token
    Core->>Core: Check signature and expiry
    Core->>DB: Get current roles and status
    Core-->>Forum: userId, roles = student, status = active
    Forum-->>Web: Post saved under Jane's userId
```

If the password is wrong, login returns **401**. If the token is fake, changed, or expired, validate returns **401**. If the account is suspended, both return **403**.

## 3. Shared data model (v1)

The tables Shared Core owns. Other teams' tables store a `user_id` that points to `users.id`; they never copy names or emails.

```mermaid
erDiagram
    USERS ||--|| PROFILES : "has one"
    USERS ||--o{ NOTIFICATIONS : receives
    USERS ||--o{ OTHER_TEAMS_TABLES : "referenced by user_id"

    USERS {
        uuid id PK
        string email "unique, used to log in"
        string password_hash "bcrypt, never returned"
        text_array roles "student, mentor, staff, admin"
        string status "active or suspended"
        timestamp created_at
    }
    PROFILES {
        uuid user_id PK "also FK to users.id"
        string full_name
        int grad_year
        string major
        string employer "mentors"
        string job_title "mentors"
        jsonb extensions "one section per team"
    }
    NOTIFICATIONS {
        uuid id PK
        uuid user_id FK
        string type
        string message
        boolean is_read
        timestamp created_at
    }
    OTHER_TEAMS_TABLES {
        uuid user_id FK "e.g. applications, posts, connections"
    }
```

Source of truth for the tables: `db/migrations/` in the Shared Core repo. After Sprint 2, any change requires written notice to all teams.
