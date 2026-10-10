package edu.gmu.careerlaunch.sharedcore.auth;

/** What POST /auth/login sends back: {"token": "...", "expiresIn": 3600} */
public record LoginResponse(String token, long expiresIn) {
}
