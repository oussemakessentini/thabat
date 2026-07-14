package com.thabat.identity.auth;

import com.thabat.identity.auth.dto.AuthResponse;
import com.thabat.identity.auth.dto.LoginRequest;
import com.thabat.identity.auth.dto.RegisterRequest;
import com.thabat.identity.auth.dto.RegisterResponse;
import com.thabat.identity.auth.mapper.AuthMapper;
import com.thabat.identity.common.exception.AccountDisabledException;
import com.thabat.identity.common.exception.AccountLockedException;
import com.thabat.identity.common.exception.EmailAlreadyExistsException;
import com.thabat.identity.common.exception.InvalidCredentialsException;
import com.thabat.identity.common.exception.ResourceNotFoundException;
import com.thabat.identity.security.CustomUserDetails;
import com.thabat.identity.security.JwtService;
import com.thabat.identity.user.Role;
import com.thabat.identity.user.RoleName;
import com.thabat.identity.user.RoleRepository;
import com.thabat.identity.user.User;
import com.thabat.identity.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;

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

        User user = authMapper.toUser(
                request,
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                userRole
        );

        User savedUser = userRepository.save(user);

        return authMapper.toRegisterResponse(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String deviceInfo) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            normalizedEmail,
                            request.password()
                    )
            );
        } catch (BadCredentialsException exception) {
            throw new InvalidCredentialsException();
        } catch (DisabledException exception) {
            throw new AccountDisabledException();
        } catch (LockedException exception) {
            throw new AccountLockedException();
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException();
        }

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByIdWithRoles(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found"));

        return issueTokens(user, deviceInfo);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        RefreshTokenService.IssuedRefreshToken rotated =
                refreshTokenService.rotate(rawRefreshToken);

        User user = rotated.refreshToken().getUser();
        if (!user.isEnabled()) {
            throw new AccountDisabledException();
        }
        if (user.isAccountLocked()) {
            throw new AccountLockedException();
        }

        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .map(Enum::name)
                .collect(java.util.stream.Collectors.toSet());

        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                roles
        );

        return new AuthResponse(
                accessToken,
                rotated.rawToken(),
                "Bearer",
                jwtService.getAccessTokenExpirationMs(),
                userMapper.toUserResponse(user)
        );
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }

    @Transactional(readOnly = true)
    public User getCurrentUser(UUID userId) {
        return userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User was not found"));
    }

    private AuthResponse issueTokens(User user, String deviceInfo) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .map(Enum::name)
                .collect(java.util.stream.Collectors.toSet());

        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                roles
        );

        RefreshTokenService.IssuedRefreshToken refreshToken =
                refreshTokenService.create(user, deviceInfo);

        return new AuthResponse(
                accessToken,
                refreshToken.rawToken(),
                "Bearer",
                jwtService.getAccessTokenExpirationMs(),
                userMapper.toUserResponse(user)
        );
    }
}
