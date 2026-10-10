package edu.gmu.careerlaunch.sharedcore.auth;

import java.util.List;
import java.util.UUID;

/** The account info login and validation need. Never includes the password hash. */
public record AuthUser(UUID id, List<String> roles, String status) {

    public boolean isSuspended() {
        return "suspended".equals(status);
    }
}
