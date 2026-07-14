package com.thabat.quran.goal;

import com.thabat.quran.goal.dto.QuranDailyGoalResponse;
import com.thabat.quran.goal.dto.UpsertQuranDailyGoalRequest;
import com.thabat.quran.security.JwtUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quran/goals")
@RequiredArgsConstructor
@Validated
public class QuranDailyGoalController {

    private final QuranDailyGoalService goalService;

    @PutMapping("/daily")
    public QuranDailyGoalResponse upsertDailyGoal(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody UpsertQuranDailyGoalRequest request
    ) {
        return goalService.upsert(principal.getUserId(), request);
    }

    @GetMapping("/daily")
    public QuranDailyGoalResponse getDailyGoal(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return goalService.getActive(principal.getUserId());
    }
}
