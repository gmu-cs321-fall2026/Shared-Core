# Technology stack decision

> **Status: PARTLY DECIDED.** Post the final version to the project wiki (required in Sprint 1).

| Part | Our choice | Why |
|---|---|---|
| Database | PostgreSQL 16 | Recommended by the SOW; supports UUIDs, JSONB, and arrays that our schema uses |
| Language | Java 21 | Team's chosen language. 21 is a long-term support version, and newer Java (e.g. 25) can run it too |
| Framework | Spring Boot | The most common Java web framework; handles the web server, settings, and database connection |
| Build tool | Maven (with the `mvnw` wrapper) | Teammates don't need to install Maven; `./mvnw` downloads it |
| Database access | Spring JdbcTemplate | We write the SQL ourselves, so queries can be tested directly in `psql` first |
| Connection pool | HikariCP | Built into Spring Boot; reuses database connections |
| Migration tool | Plain numbered SQL files in `db/migrations/` | Simple; run automatically by Docker. *(Could move to Flyway later)* |
| Password hashing | bcrypt | Seed data already uses bcrypt hashes |
| Login tokens | *JWT or sessions — to decide* | |
| JWT library | *to decide (e.g. jjwt or Spring Security's JWT support)* | |
| API gateway | *to decide (e.g. Spring Cloud Gateway, Nginx)* | |

**Everyone on the team uses Java 21 or newer.**
