package com.thabat.identity.user;

import com.thabat.identity.auth.AuthService;
import com.thabat.identity.auth.UserMapper;
import com.thabat.identity.auth.dto.UserResponse;
import com.thabat.identity.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal CustomUserDetails principal) {
        User user = authService.getCurrentUser(principal.getId());
        return userMapper.toUserResponse(user);
    }
}
