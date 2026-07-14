package com.thabat.prayer.recovery;

import com.thabat.prayer.assessment.PrayerAssessment;
import com.thabat.prayer.recovery.dto.PrayerProgressResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Builds sequential recovery progress from immutable assessment baselines
 * and the count of completed recovery entries (never mutates the assessment).
 */
public final class PrayerProgressCalculator {

    public enum CyclePrayerStatus {
        COMPLETED,
        NEXT,
        LOCKED
    }

    private PrayerProgressCalculator() {
    }

    public static PrayerProgressResponse calculate(
            PrayerAssessment assessment,
            long totalCompletedPrayers
    ) {
        long originalTotal = assessment.getTotalRemainingPrayers();
        long totalRecoveryCycles = assessment.getTotalEstimatedDays();
        long remaining = Math.max(0L, originalTotal - totalCompletedPrayers);

        long completedCycles = totalCompletedPrayers / 5L;
        long completedPrayersInCurrentCycle = totalCompletedPrayers % 5L;

        long currentCycleNumber;
        if (remaining <= 0) {
            currentCycleNumber = totalRecoveryCycles;
        } else {
            currentCycleNumber = completedCycles + 1L;
        }

        PrayerType nextPrayer = remaining > 0
                ? SequentialPrayerOrder.prayerForSequence(totalCompletedPrayers + 1L)
                : null;

        long estimatedRemainingDays = estimatedRemainingDays(
                remaining,
                assessment.getDailyRecoveryTarget()
        );

        return new PrayerProgressResponse(
                assessment.getId(),
                totalRecoveryCycles,
                completedCycles,
                currentCycleNumber,
                completedPrayersInCurrentCycle,
                currentCycleStatuses(remaining <= 0, completedPrayersInCurrentCycle),
                nextPrayer,
                totalCompletedPrayers,
                remaining,
                percentage(totalCompletedPrayers, originalTotal),
                assessment.getDailyRecoveryTarget(),
                estimatedRemainingDays
        );
    }

    public static PrayerProgressResponse.CurrentCycle currentCycleStatuses(
            boolean fullyComplete,
            long completedPrayersInCurrentCycle
    ) {
        if (fullyComplete) {
            return new PrayerProgressResponse.CurrentCycle(
                    CyclePrayerStatus.COMPLETED,
                    CyclePrayerStatus.COMPLETED,
                    CyclePrayerStatus.COMPLETED,
                    CyclePrayerStatus.COMPLETED,
                    CyclePrayerStatus.COMPLETED
            );
        }

        return new PrayerProgressResponse.CurrentCycle(
                statusForIndex(0, completedPrayersInCurrentCycle),
                statusForIndex(1, completedPrayersInCurrentCycle),
                statusForIndex(2, completedPrayersInCurrentCycle),
                statusForIndex(3, completedPrayersInCurrentCycle),
                statusForIndex(4, completedPrayersInCurrentCycle)
        );
    }

    private static CyclePrayerStatus statusForIndex(
            int index,
            long completedPrayersInCurrentCycle
    ) {
        if (index < completedPrayersInCurrentCycle) {
            return CyclePrayerStatus.COMPLETED;
        }
        if (index == completedPrayersInCurrentCycle) {
            return CyclePrayerStatus.NEXT;
        }
        return CyclePrayerStatus.LOCKED;
    }

    static BigDecimal percentage(long completed, long original) {
        if (original <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(completed)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(original), 2, RoundingMode.HALF_UP);
    }

    /**
     * ceil(totalRemainingPrayers / dailyRecoveryTarget), or 0 when nothing remains.
     */
    static long estimatedRemainingDays(long totalRemainingPrayers, int dailyRecoveryTarget) {
        if (totalRemainingPrayers <= 0) {
            return 0L;
        }
        if (dailyRecoveryTarget <= 0) {
            throw new IllegalArgumentException("dailyRecoveryTarget must be positive");
        }
        return (totalRemainingPrayers + dailyRecoveryTarget - 1L) / dailyRecoveryTarget;
    }
}
