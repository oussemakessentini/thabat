package com.thabat.identity.security;

import com.thabat.identity.config.JwtProperties;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new JwtProperties(
                "test-secret-key-must-be-at-least-32-bytes-long!!",
                1000L,
                60_000L
        ));
    }

    @Test
    void generateAccessToken_includesExpectedClaims() {
        UUID userId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(
                userId,
                "user@example.com",
                List.of("ROLE_USER")
        );

        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtService.parseClaims(token).get("email", String.class))
                .isEqualTo("user@example.com");
    }

    @Test
    void expiredAccessToken_isRejected() throws InterruptedException {
        JwtService shortLived = new JwtService(new JwtProperties(
                "test-secret-key-must-be-at-least-32-bytes-long!!",
                1L,
                60_000L
        ));

        String token = shortLived.generateAccessToken(
                UUID.randomUUID(),
                "user@example.com",
                List.of("ROLE_USER")
        );

        Thread.sleep(20);

        assertThat(shortLived.isTokenValid(token)).isFalse();
        assertThatThrownBy(() -> shortLived.parseClaims(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
