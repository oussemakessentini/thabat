package com.thabat.identity.auth.mapper;

import com.thabat.identity.auth.dto.RegisterRequest;
import com.thabat.identity.auth.dto.RegisterResponse;
import com.thabat.identity.user.Role;
import com.thabat.identity.user.User;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class AuthMapper {

    public User toUser(
            RegisterRequest request,
            String normalizedEmail,
            String encodedPassword,
            Role userRole
    ) {
        return User.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(normalizedEmail)
                .password(encodedPassword)
                .enabled(true)
                .accountLocked(false)
                .countryCode(normalizeCountryCode(request.countryCode()))
                .timezone(normalizeTimezone(request.timezone()))
                .dateOfBirth(request.dateOfBirth())
                .preferredLanguage(normalizePreferredLanguage(request.preferredLanguage()))
                .roles(Set.of(userRole))
                .build();
    }

    public RegisterResponse toRegisterResponse(User user) {
        return new RegisterResponse(
                user.getId(),
                user.getEmail(),
                user.getDateOfBirth(),
                "Account created successfully"
        );
    }

    private String normalizePreferredLanguage(String preferredLanguage) {
        if (preferredLanguage == null || preferredLanguage.isBlank()) {
            return "en";
        }

        return preferredLanguage.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeCountryCode(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return null;
        }

        return countryCode.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeTimezone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return null;
        }

        return timezone.trim();
    }
}
