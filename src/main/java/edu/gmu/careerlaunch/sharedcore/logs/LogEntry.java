package edu.gmu.careerlaunch.sharedcore.logs;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * One row of the logs table, as JSON:
 * {"id": "...", "category": "auth", "userId": "..." or null, "information": "...", "occurredAt": "2026-11-02T10:00:00Z"}
 */
public record LogEntry(UUID id, String category, UUID userId, String information, OffsetDateTime occurredAt) {
}
