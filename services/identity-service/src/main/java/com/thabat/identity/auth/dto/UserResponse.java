package com.thabat.identity.auth.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        LocalDate dateOfBirth,
        List<String> roles
) {
}
