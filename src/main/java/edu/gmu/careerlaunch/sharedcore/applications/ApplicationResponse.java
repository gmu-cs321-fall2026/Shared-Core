package edu.gmu.careerlaunch.sharedcore.applications;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ApplicationResponse(
        Long applicationId,
        Integer jobId,
        UUID userId,
        ApplicationStatus status,
        String resumeUrl,
        OffsetDateTime updatedAt,
        OffsetDateTime submittedAt) {
}
