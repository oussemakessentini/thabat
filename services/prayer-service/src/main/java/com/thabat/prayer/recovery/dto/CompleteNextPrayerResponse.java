package com.thabat.prayer.recovery.dto;

import com.thabat.prayer.recovery.PrayerType;

import java.math.BigDecimal;

public record CompleteNextPrayerResponse(
        PrayerType completedPrayer,
        PrayerType nextPrayer,
        long completedCycles,
        long completedPrayersInCurrentCycle,
        long totalCompletedPrayers,
        long totalRemainingPrayers,
        long totalRecoveryCycles,
        long currentCycleNumber,
        BigDecimal progressPercentage
) {
}
