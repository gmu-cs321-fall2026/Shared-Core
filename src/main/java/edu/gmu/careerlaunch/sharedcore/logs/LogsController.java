package edu.gmu.careerlaunch.sharedcore.logs;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.gmu.careerlaunch.sharedcore.auth.AuthRepository;
import edu.gmu.careerlaunch.sharedcore.auth.AuthUser;
import edu.gmu.careerlaunch.sharedcore.auth.JwtService;
import edu.gmu.careerlaunch.sharedcore.common.ApiException;
import io.jsonwebtoken.JwtException;

/**
 * GET /logs        — search log entries (debugging), paginated, newest first
 * GET /logs/stats  — totals by category or by week (analytics dashboard), never individual users
 * Both are admin only. Contract: docs/api/logs.md
 */
@RestController
public class LogsController {

    /** Must match the CHECK constraint in db/migrations/004_create_logs.sql. */
    static final Set<String> CATEGORIES = Set.of(
            "auth", "registration", "profile", "application", "connection",
            "event", "forum", "notification", "admin", "error");

    private static final int MAX_PAGE_SIZE = 50;   // docs/conventions.md, section 2

    private final LogsRepository logs;
    private final AuthRepository users;
    private final JwtService jwt;

    public LogsController(LogsRepository logs, AuthRepository users, JwtService jwt) {
        this.logs = logs;
        this.users = users;
        this.jwt = jwt;
    }

    @GetMapping("/logs")
    public LogPage list(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String pageSize) {

        requireAdmin(authorization);

        // Parameters come in as text, so a bad value gives our own 400 instead of a Spring error.
        Map<String, String> errors = new LinkedHashMap<>();
        String cat = parseCategory(category, errors);
        UUID user = parseUuid("userId", userId, errors);
        OffsetDateTime start = parseTime("from", from, errors);
        OffsetDateTime end = parseTime("to", to, errors);
        checkOrder(start, end, errors);
        int p = parseInt("page", page, 1, 1, Integer.MAX_VALUE, errors);
        int size = parseInt("pageSize", pageSize, 20, 1, MAX_PAGE_SIZE, errors);
        throwIfAny(errors);

        List<LogEntry> data = logs.find(cat, user, start, end, p, size);
        long total = logs.count(cat, user, start, end);
        return new LogPage(data, p, size, total);
    }

    @GetMapping("/logs/stats")
    public Map<String, Object> stats(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) String groupBy,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        requireAdmin(authorization);

        Map<String, String> errors = new LinkedHashMap<>();
        String group = (groupBy == null || groupBy.isBlank()) ? "category" : groupBy.trim();
        if (!group.equals("category") && !group.equals("week")) {
            errors.put("groupBy", "must be category or week");
        }
        // No userId filter here on purpose: analytics only ever shows totals.
        String cat = parseCategory(category, errors);
        OffsetDateTime start = parseTime("from", from, errors);
        OffsetDateTime end = parseTime("to", to, errors);
        checkOrder(start, end, errors);
        throwIfAny(errors);

        // {"groupBy": "week", "data": [{"key": "2026-11-02", "count": 3, "activeUsers": 2}, ...]}
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("groupBy", group);
        body.put("data", logs.stats(group.equals("week"), cat, start, end));
        return body;
    }

    // ---------- checks ----------

    /**
     * 401 if there's no valid token, 403 if the user isn't an admin.
     * Same steps as GET /auth/validate. If the team's shared role-check helper (SHCOR-25)
     * exists, replace this method with a call to it.
     */
    private AuthUser requireAdmin(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw unauthorized("Missing or malformed Authorization header");
        }
        String token = authorization.substring("Bearer ".length()).trim();

        UUID id;
        try {
            id = UUID.fromString(jwt.parseToken(token).getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw unauthorized("Invalid or expired token");
        }

        // Look the user up now, so a suspension or role change counts immediately
        AuthUser user = users.findById(id).orElseThrow(() -> unauthorized("Invalid or expired token"));
        if (user.isSuspended()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_SUSPENDED",
                    "This account has been suspended", null);
        }
        if (!user.roles().contains("admin")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                    "Not allowed", "Only admins can view logs");
        }
        return user;
    }

    private static String parseCategory(String value, Map<String, String> errors) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String cat = value.trim().toLowerCase();
        if (!CATEGORIES.contains(cat)) {
            errors.put("category", "must be one of " + String.join(", ", CATEGORIES.stream().sorted().toList()));
        }
        return cat;
    }

    private static UUID parseUuid(String name, String value, Map<String, String> errors) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException e) {
            errors.put(name, "must be a valid UUID");
            return null;
        }
    }

    private static void checkOrder(OffsetDateTime from, OffsetDateTime to, Map<String, String> errors) {
        if (from != null && to != null && !from.isBefore(to)) {
            errors.put("to", "must be later than from");
        }
    }

    /** ISO 8601 with a time zone, e.g. 2026-11-01T00:00:00Z (docs/conventions.md, section 4). */
    private static OffsetDateTime parseTime(String name, String value, Map<String, String> errors) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value.trim());
        } catch (DateTimeParseException e) {
            errors.put(name, "must be an ISO 8601 time like 2026-11-01T00:00:00Z");
            return null;
        }
    }

    private static int parseInt(String name, String value, int fallback, int min, int max,
                                Map<String, String> errors) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            int n = Integer.parseInt(value.trim());
            if (n < min || n > max) {
                errors.put(name, max == Integer.MAX_VALUE
                        ? "must be at least " + min
                        : "must be between " + min + " and " + max);
                return fallback;
            }
            return n;
        } catch (NumberFormatException e) {
            errors.put(name, "must be a whole number");
            return fallback;
        }
    }

    private static void throwIfAny(Map<String, String> errors) {
        if (!errors.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Invalid input", errors);
        }
    }

    private static ApiException unauthorized(String detail) {
        return new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Unauthorized", detail);
    }
}
