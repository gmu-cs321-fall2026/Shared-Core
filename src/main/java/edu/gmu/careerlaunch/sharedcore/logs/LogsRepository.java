package edu.gmu.careerlaunch.sharedcore.logs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * All SQL for the logs API. Same pattern as auth/AuthRepository: plain SQL through JdbcTemplate.
 *
 * Every filter can be null, meaning "don't filter on this".
 * from is inclusive (>=), to is exclusive (<).
 * Every value from the caller goes in as a "?" parameter, never glued into the SQL,
 * so nobody can inject SQL through a filter.
 */
@Repository
public class LogsRepository {

    private final JdbcTemplate jdbc;

    public LogsRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** One page of log entries, newest first. */
    public List<LogEntry> find(String category, UUID userId, OffsetDateTime from, OffsetDateTime to,
                               int page, int pageSize) {
        List<Object> params = new ArrayList<>();
        String where = where(category, userId, from, to, params);
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        // "id" breaks ties, so rows with the same time never appear on two pages
        String sql = "SELECT id, log_category, user_id, information, occurred_at FROM logs" + where
                + " ORDER BY occurred_at DESC, id DESC LIMIT ? OFFSET ?";
        return jdbc.query(sql, this::toEntry, params.toArray());
    }

    /** How many entries match, across all pages. */
    public long count(String category, UUID userId, OffsetDateTime from, OffsetDateTime to) {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT COUNT(*) FROM logs" + where(category, userId, from, to, params);
        Long total = jdbc.queryForObject(sql, Long.class, params.toArray());
        return total == null ? 0 : total;
    }

    /**
     * Totals for analytics. Each row is {"key", "count", "activeUsers"}.
     * byWeek = false: key is the category, biggest first.
     * byWeek = true:  key is the week's Monday in UTC (e.g. "2026-11-02"), oldest first.
     */
    public List<Map<String, Object>> stats(boolean byWeek, String category,
                                           OffsetDateTime from, OffsetDateTime to) {
        List<Object> params = new ArrayList<>();
        String key = byWeek
                ? "to_char(date_trunc('week', occurred_at AT TIME ZONE 'UTC'), 'YYYY-MM-DD')"
                : "log_category";
        String order = byWeek ? "key" : "count DESC, key";
        String sql = "SELECT " + key + " AS key, COUNT(*) AS count, COUNT(DISTINCT user_id) AS active_users"
                + " FROM logs" + where(category, null, from, to, params)
                + " GROUP BY key ORDER BY " + order;

        return jdbc.query(sql, (rs, n) -> {
            Map<String, Object> row = new LinkedHashMap<>();   // keeps the JSON in this order
            row.put("key", rs.getString("key"));
            row.put("count", rs.getLong("count"));
            row.put("activeUsers", rs.getLong("active_users"));
            return row;
        }, params.toArray());
    }

    /** Adds one "AND ... ?" for each filter that was given, and its value to params. */
    private static String where(String category, UUID userId, OffsetDateTime from, OffsetDateTime to,
                                List<Object> params) {
        StringBuilder sql = new StringBuilder(" WHERE 1=1");
        if (category != null) { sql.append(" AND log_category = ?"); params.add(category); }
        if (userId != null)   { sql.append(" AND user_id = ?");      params.add(userId); }
        if (from != null)     { sql.append(" AND occurred_at >= ?"); params.add(from); }
        if (to != null)       { sql.append(" AND occurred_at < ?");  params.add(to); }
        return sql.toString();
    }

    private LogEntry toEntry(ResultSet rs, int rowNum) throws SQLException {
        return new LogEntry(
                rs.getObject("id", UUID.class),
                rs.getString("log_category"),
                rs.getObject("user_id", UUID.class),       // null for system events
                rs.getString("information"),
                rs.getObject("occurred_at", OffsetDateTime.class));
    }
}
