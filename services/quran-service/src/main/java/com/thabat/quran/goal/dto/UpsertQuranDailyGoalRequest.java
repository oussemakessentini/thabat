package com.thabat.quran.goal.dto;

import com.thabat.quran.page.QuranPageConstants;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpsertQuranDailyGoalRequest(
        @NotNull(message = "memorizationPagesPerDay is required")
        @Min(value = 0, message = "memorizationPagesPerDay must be between 0 and 20")
        @Max(value = 20, message = "memorizationPagesPerDay must be between 0 and 20")
        Integer memorizationPagesPerDay,

        @NotNull(message = "revisionPagesPerDay is required")
        @Min(value = 0, message = "revisionPagesPerDay must be between 0 and 50")
        @Max(value = 50, message = "revisionPagesPerDay must be between 0 and 50")
        Integer revisionPagesPerDay,

        @Min(
                value = QuranPageConstants.MIN_PAGE,
                message = "preferredStartPage must be between 1 and 604"
        )
        @Max(
                value = QuranPageConstants.MAX_PAGE,
                message = "preferredStartPage must be between 1 and 604"
        )
        Integer preferredStartPage
) {
    @AssertTrue(message = "At least one of memorizationPagesPerDay or revisionPagesPerDay must be greater than zero")
    public boolean isAtLeastOneTargetPositive() {
        int mem = memorizationPagesPerDay == null ? 0 : memorizationPagesPerDay;
        int rev = revisionPagesPerDay == null ? 0 : revisionPagesPerDay;
        return mem > 0 || rev > 0;
    }
}
