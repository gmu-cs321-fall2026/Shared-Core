package edu.gmu.careerlaunch.sharedcore.auth;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * All the SQL for login and validation lives here.
 * These are the same queries you can test by hand in psql.
 */
@Repository
public class AuthRepository {

    private final JdbcTemplate jdbc;

    public AuthRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Turns one database row into an AuthUser. */
    private static final RowMapper<AuthUser> ROW = (ResultSet rs, int rowNum) -> new AuthUser(
            rs.getObject("id", UUID.class),
            toList(rs.getArray("roles")),
            rs.getString("status"));

    /**
     * Login check: finds the user ONLY if the email exists AND the password is right.
     * crypt() hashes the typed password the same way the stored hash was made,
     * so the two match only when the password is correct.
     */
    public Optional<AuthUser> findByEmailAndPassword(String email, String password) {
        List<AuthUser> rows = jdbc.query("""
                SELECT id, roles, status
                FROM users
                WHERE lower(email) = lower(?)
                  AND password_hash = crypt(?, password_hash)
                """, ROW, email, password);
        return rows.stream().findFirst();
    }

    /** Validation: looks up the user's CURRENT roles and status (they may have changed since login). */
    public Optional<AuthUser> findById(UUID id) {
        List<AuthUser> rows = jdbc.query(
                "SELECT id, roles, status FROM users WHERE id = ?", ROW, id);
        return rows.stream().findFirst();
    }

    /** PostgreSQL TEXT[] arrays come back as java.sql.Array; turn them into a normal List. */
    private static List<String> toList(java.sql.Array array) throws SQLException {
        if (array == null) {
            return List.of();
        }
        return Arrays.asList((String[]) array.getArray());
    }
}
