package com.thabat.prayer.assessment;

import com.thabat.prayer.assessment.dto.CreatePrayerAssessmentRequest;
import com.thabat.prayer.assessment.dto.PrayerAssessmentResponse;
import com.thabat.prayer.security.JwtUserPrincipal;
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
@RequestMapping("/api/v1/prayers")
@RequiredArgsConstructor
public class PrayerAssessmentController {

    private final PrayerAssessmentService prayerAssessmentService;

    @PostMapping("/assessments")
    public ResponseEntity<PrayerAssessmentResponse> createAssessment(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody CreatePrayerAssessmentRequest request
    ) {
        PrayerAssessmentResponse response =
                prayerAssessmentService.createAssessment(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/assessments/latest")
    public PrayerAssessmentResponse getLatestAssessment(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return prayerAssessmentService.getLatestAssessment(principal.getUserId());
    }
}
