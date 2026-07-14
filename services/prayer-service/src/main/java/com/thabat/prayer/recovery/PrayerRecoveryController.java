package com.thabat.prayer.recovery;

import com.thabat.prayer.recovery.dto.CompleteNextPrayerRequest;
import com.thabat.prayer.recovery.dto.CompleteNextPrayerResponse;
import com.thabat.prayer.recovery.dto.PrayerProgressResponse;
import com.thabat.prayer.recovery.dto.RecoveryHistoryItemResponse;
import com.thabat.prayer.recovery.dto.UndoLatestRecoveryRequest;
import com.thabat.prayer.security.JwtUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prayers")
@RequiredArgsConstructor
public class PrayerRecoveryController {

    private final PrayerRecoveryService prayerRecoveryService;

    @PostMapping("/recovery/complete-next")
    public ResponseEntity<CompleteNextPrayerResponse> completeNext(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody CompleteNextPrayerRequest request
    ) {
        CompleteNextPrayerResponse response =
                prayerRecoveryService.completeNext(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/recovery/latest")
    public PrayerProgressResponse undoLatest(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody UndoLatestRecoveryRequest request
    ) {
        return prayerRecoveryService.undoLatest(principal.getUserId(), request);
    }

    @GetMapping("/progress")
    public PrayerProgressResponse getProgress(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return prayerRecoveryService.getProgress(principal.getUserId());
    }

    @GetMapping("/recovery/history")
    public List<RecoveryHistoryItemResponse> getHistory(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return prayerRecoveryService.getHistory(principal.getUserId());
    }
}
