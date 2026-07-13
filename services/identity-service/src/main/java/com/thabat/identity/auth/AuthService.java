package com.thabat.identity.auth;

import com.thabat.identity.auth.dto.RegisterRequest;
import com.thabat.identity.auth.dto.RegisterResponse;
import com.thabat.identity.common.exception.EmailAlreadyExistsException;
import com.thabat.identity.common.exception.ResourceNotFoundException;
import com.thabat.identity.user.Role;
import com.thabat.identity.user.RoleName;
import com.thabat.identity.user.RoleRepository;
import com.thabat.identity.user.User;
import com.thabat.identity.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Default user role was not found"
                ));

        String language = request.preferredLanguage() == null
                || request.preferredLanguage().isBlank()
                ? "en"
                : request.preferredLanguage().trim().toLowerCase(Locale.ROOT);

        String countryCode = request.countryCode() == null
                || request.countryCode().isBlank()
                ? null
                : request.countryCode().trim().toUpperCase(Locale.ROOT);

        String timezone = request.timezone() == null
                || request.timezone().isBlank()
                ? null
                : request.timezone().trim();

        User user = User.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.password()))
                .enabled(true)
                .accountLocked(false)
                .countryCode(countryCode)
                .timezone(timezone)
                .preferredLanguage(language)
                .roles(Set.of(userRole))
                .build();

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                "Account created successfully"
        );
    }
}