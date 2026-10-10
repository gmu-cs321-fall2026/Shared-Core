package edu.gmu.careerlaunch.sharedcore.common;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * GET /health — proves the service is running AND can talk to the database.
 * If the database is down, the query throws an exception and
 * GlobalExceptionHandler turns it into a 503 error.
 */
@RestController
public class HealthController {

    private final JdbcTemplate jdbc;

    // Spring Boot creates the JdbcTemplate (already connected to the pool) and hands it in here.
    public HealthController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        // The simplest possible query. If this works, the connection works.
        jdbc.queryForObject("SELECT 1", Integer.class);
        return Map.of("status", "ok", "database", "up");
    }
}
