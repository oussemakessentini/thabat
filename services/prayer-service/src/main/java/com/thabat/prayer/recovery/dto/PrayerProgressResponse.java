package com.thabat.prayer.recovery.dto;

import com.thabat.prayer.recovery.PrayerProgressCalculator.CyclePrayerStatus;
import com.thabat.prayer.recovery.PrayerType;

import java.math.BigDecimal;
import java.util.UUID;

public record PrayerProgressResponse(
        UUID assessmentId,
        long totalRecoveryCycles,
        long completedCycles,
        long currentCycleNumber,
        long completedPrayersInCurrentCycle,
        CurrentCycle currentCycle,
        PrayerType nextPrayer,
        long totalCompletedPrayers,
        long totalRemainingPrayers,
        BigDecimal progressPercentage,
        int dailyRecoveryTarget,
        long estimatedRemainingDays
) {
    public record CurrentCycle(
            CyclePrayerStatus fajr,
            CyclePrayerStatus dhuhr,
            CyclePrayerStatus asr,
            CyclePrayerStatus maghrib,
            CyclePrayerStatus isha
    ) {
    }
}
