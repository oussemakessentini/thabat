package com.thabat.prayer.assessment.dto;

import java.time.Instant;
import java.util.UUID;

public record PrayerAssessmentResponse(
        UUID id,
        int missedYears,
        int missedMonths,
        int missedDays,
        long totalEstimatedDays,
        RemainingByPrayer remainingByPrayer,
        long totalRemainingPrayers,
        int dailyRecoveryTarget,
        long estimatedCompletionDays,
        Instant createdAt
) {
    public record RemainingByPrayer(
            long fajr,
            long dhuhr,
            long asr,
            long maghrib,
            long isha
    ) {
    }
}
