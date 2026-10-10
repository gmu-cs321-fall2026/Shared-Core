package edu.gmu.careerlaunch.sharedcore.auth;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import edu.gmu.careerlaunch.sharedcore.common.ApiException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;

/**
 * POST /auth/login    — check email + password, return a token
 * GET  /auth/validate — check a token, return who it belongs to
 * Contract: docs/api/auth.md
 */
@RestController
public class AuthController {

    // One message for BOTH "no such email" and "wrong password", so attackers
    // can't use login to find out which emails have accounts.
    private static final String BAD_LOGIN = "Invalid email or password";

    private final AuthRepository users;
    private final JwtService jwt;

    public AuthController(AuthRepository users, JwtService jwt) {
        this.users = users;
        this.jwt = jwt;
    }

    @PostMapping("/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        // 1. Email + password must match (the SQL you tested in psql)
        AuthUser user = users.findByEmailAndPassword(request.email(), request.password())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", BAD_LOGIN, null));

        // 2. Suspended accounts can't log in (checked only AFTER the password is correct)
        if (user.isSuspended()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_SUSPENDED",
                    "This account has been suspended", null);
        }

        // 3. Make the token
        return new LoginResponse(jwt.createToken(user), jwt.expiresInSeconds());
    }

    @GetMapping("/auth/validate")
    public ValidateResponse validate(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        // 1. The header must look like "Bearer <token>"
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw unauthorized("Missing or malformed Authorization header");
        }
        String token = authorization.substring("Bearer ".length()).trim();

        // 2. Check the signature and expiry
        UUID userId;
        try {
            Claims claims = jwt.parseToken(token);
            userId = UUID.fromString(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw unauthorized("Invalid or expired token");
        }

        // 3. Look up the user NOW, so a suspension or role change takes effect immediately
        AuthUser user = users.findById(userId)
                .orElseThrow(() -> unauthorized("Invalid or expired token"));
        if (user.isSuspended()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_SUSPENDED",
                    "This account has been suspended", null);
        }

        // TODO (with logout): also reject tokens whose id is in the revoked-tokens table

        return new ValidateResponse(user.id(), user.roles(), user.status());
    }

    private static ApiException unauthorized(String detail) {
        return new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Unauthorized", detail);
    }
}
