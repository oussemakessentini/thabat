package com.thabat.identity.security;

import com.thabat.identity.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;

        String secret = jwtProperties.secret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET is missing. Set it in services/identity-service/.env "
                            + "(see .env.example). It must be at least 32 characters."
            );
        }

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must be at least 32 characters for HS256. "
                            + "Current length: " + keyBytes.length
                            + ". Update services/identity-service/.env or your run configuration."
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        org.slf4j.LoggerFactory.getLogger(JwtService.class).info(
                "JWT signing configured (secret length={})",
                keyBytes.length
        );
    }

    public String generateAccessToken(
            UUID userId,
            String email,
            Collection<String> roles
    ) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(jwtProperties.accessTokenExpiration());

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId.toString())
                .claim("email", email)
                .claim("roles", List.copyOf(roles))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException
                 | MalformedJwtException
                 | UnsupportedJwtException
                 | SecurityException
                 | IllegalArgumentException exception) {
            return false;
        }
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public long getAccessTokenExpirationMs() {
        return jwtProperties.accessTokenExpiration();
    }
}
