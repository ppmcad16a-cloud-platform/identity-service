package com.mernecommerce.identity.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

// Signs and verifies the same HS256 JWTs every other microservice in this
// project issues/verifies (identity_server is the only one that ever
// *issues* a token — see routes/api/auth.js's Node equivalent). The secret
// must be byte-identical to JWT_SECRET in every other service's .env: this
// uses the raw UTF-8 bytes of the secret string, matching how Node's
// `jsonwebtoken` library treats a string secret.
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms:604800000}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // Payload carries a single "id" claim — the same shape Node's
    // `jwt.sign({ id: user.id }, secret, ...)` produces, since
    // catalog_server/commerce_server/notification_server all read
    // `payload.id` when verifying.
    public String generateToken(String userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claim("id", userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    // Throws (JwtException subtypes) on an invalid/expired/malformed token —
    // callers treat any exception as "not authenticated", same as
    // passport-jwt's silent-401 behavior on a bad token.
    public String extractUserId(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
        return jws.getPayload().get("id", String.class);
    }
}
