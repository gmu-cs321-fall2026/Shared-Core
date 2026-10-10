package edu.gmu.careerlaunch.sharedcore.auth;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Creates and checks JWT login tokens.
 * A token = header . payload (user id, roles, expiry) . signature (made with our secret)
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expiresMinutes;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expires-minutes}") long expiresMinutes) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET in .env must be at least 32 characters long. See README.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiresMinutes = expiresMinutes;
    }

    /** Login: make a signed token for this user. */
    public String createToken(AuthUser user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.id().toString())            // who the token belongs to
                .claim("roles", user.roles())              // their roles at login time
                .id(UUID.randomUUID().toString())          // unique token id (used later for logout)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expiresMinutes * 60)))
                .signWith(key)                             // the signature
                .compact();
    }

    /**
     * Validation: check the signature and expiry.
     * Returns the token's contents, or throws JwtException if the token is fake,
     * changed, expired, or not a token at all.
     */
    public Claims parseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long expiresInSeconds() {
        return expiresMinutes * 60;
    }
}
