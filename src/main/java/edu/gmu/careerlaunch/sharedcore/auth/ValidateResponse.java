package edu.gmu.careerlaunch.sharedcore.auth;

import java.util.List;
import java.util.UUID;

/** What GET /auth/validate sends back: {"userId": "...", "roles": [...], "status": "active"} */
public record ValidateResponse(UUID userId, List<String> roles, String status) {
}
