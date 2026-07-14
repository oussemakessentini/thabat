package com.thabat.journey.security;

import com.thabat.journey.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        String secret = jwtProperties.secret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET is missing. Set it in services/journey-service/.env "
                            + "(must match Identity Service, at least 32 characters)."
            );
        }
        System.out.println("JWT_SECRET length = " + secret.length());
        System.out.println(secret);

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must be at least 32 characters for HS256. Current length: "
                            + keyBytes.length
                            + ". A truncated JWT_SECRET in the IDE/OS environment overrides "
                            + "services/journey-service/.env — remove it or set the full secret "
                            + "(must match Identity Service)."
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        LoggerFactory.getLogger(JwtService.class).info(
                "JWT access-token validation configured (secret length={})",
                keyBytes.length
        );
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object roles = parseClaims(token).get("roles");
        if (roles instanceof Collection<?> collection) {
            return collection.stream().map(String::valueOf).toList();
        }
        return List.of();
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
}
