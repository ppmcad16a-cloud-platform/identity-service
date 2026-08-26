package com.mernecommerce.identity.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-must-be-at-least-32-bytes-long-1234567890";

    @Test
    void generateAndExtractRoundTrip() {
        JwtService jwtService = new JwtService(SECRET, 604800000L);

        String token = jwtService.generateToken("user-123");

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUserId(token)).isEqualTo("user-123");
    }

    @Test
    void extractUserIdThrowsForMalformedToken() {
        JwtService jwtService = new JwtService(SECRET, 604800000L);

        assertThatThrownBy(() -> jwtService.extractUserId("not-a-real-token"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void extractUserIdThrowsForTokenSignedWithDifferentSecret() {
        JwtService issuer = new JwtService(SECRET, 604800000L);
        JwtService verifier = new JwtService("a-completely-different-secret-that-is-also-32-bytes", 604800000L);

        String token = issuer.generateToken("user-123");

        assertThatThrownBy(() -> verifier.extractUserId(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void extractUserIdThrowsForExpiredToken() throws InterruptedException {
        JwtService jwtService = new JwtService(SECRET, 1L);
        String token = jwtService.generateToken("user-123");

        Thread.sleep(50);

        assertThatThrownBy(() -> jwtService.extractUserId(token))
                .isInstanceOf(JwtException.class);
    }
}
