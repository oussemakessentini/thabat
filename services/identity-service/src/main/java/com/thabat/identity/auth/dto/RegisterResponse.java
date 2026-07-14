package com.thabat.identity.auth.dto;

import java.time.LocalDate;
import java.util.UUID;

public record RegisterResponse(
        UUID userId,
        String email,
        LocalDate dateOfBirth,
        String message
) {
}
