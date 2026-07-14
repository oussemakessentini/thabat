package com.thabat.identity.auth.dto;

import com.thabat.identity.auth.validation.ValidDateOfBirth;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegisterRequest(

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must contain between 8 and 72 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "Password must include uppercase, lowercase, and a number"
        )
        String password,

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        @ValidDateOfBirth
        LocalDate dateOfBirth,

        @Pattern(
                regexp = "^[A-Za-z]{2}$",
                message = "Country code must contain exactly two letters"
        )
        String countryCode,

        @Size(max = 100, message = "Timezone must not exceed 100 characters")
        String timezone,

        @Pattern(
                regexp = "^[A-Za-z]{2,10}$",
                message = "Preferred language must contain between 2 and 10 letters"
        )
        String preferredLanguage


) {
}