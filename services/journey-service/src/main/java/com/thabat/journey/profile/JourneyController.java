package com.thabat.journey.profile;

import com.thabat.journey.profile.dto.JourneyProfileResponse;
import com.thabat.journey.profile.dto.OnboardingRequest;
import com.thabat.journey.security.JwtUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/journey")
@RequiredArgsConstructor
public class JourneyController {

    private final JourneyProfileService journeyProfileService;

    @PostMapping("/onboarding")
    public ResponseEntity<JourneyProfileResponse> completeOnboarding(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody OnboardingRequest request
    ) {
        JourneyProfileService.OnboardingResult result =
                journeyProfileService.completeOnboarding(principal.getUserId(), request);

        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.profile());
    }

    @GetMapping("/profile")
    public JourneyProfileResponse getProfile(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return journeyProfileService.getProfile(principal.getUserId());
    }
}
