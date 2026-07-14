package com.thabat.prayer.assessment;

/**
 * MVP missed-prayer estimate. This is a planning estimate only,
 * not a religious ruling. Witr is excluded. A year is counted as 365 days
 * and a month as 30 days for simplicity.
 */
public final class MissedPrayerCalculator {

    public static final int MAX_MISSED_YEARS = 120;

    private MissedPrayerCalculator() {
    }

    public record AssessmentInput(
            int missedYears,
            int missedMonths,
            int missedDays,
            int dailyRecoveryTarget
    ) {
    }

    public record AssessmentResult(
            long totalEstimatedDays,
            long fajrRemaining,
            long dhuhrRemaining,
            long asrRemaining,
            long maghribRemaining,
            long ishaRemaining,
            long totalRemainingPrayers,
            long estimatedCompletionDays
    ) {
    }

    public static AssessmentResult calculate(AssessmentInput input) {
        validate(input);

        long totalEstimatedDays =
                (long) input.missedYears() * 365L
                        + (long) input.missedMonths() * 30L
                        + (long) input.missedDays();

        long totalRemainingPrayers = totalEstimatedDays * 5L;
        long estimatedCompletionDays = ceilingDivide(
                totalRemainingPrayers,
                input.dailyRecoveryTarget()
        );

        return new AssessmentResult(
                totalEstimatedDays,
                totalEstimatedDays,
                totalEstimatedDays,
                totalEstimatedDays,
                totalEstimatedDays,
                totalEstimatedDays,
                totalRemainingPrayers,
                estimatedCompletionDays
        );
    }

    static long ceilingDivide(long dividend, int divisor) {
        if (divisor <= 0) {
            throw new IllegalArgumentException("Divisor must be positive");
        }
        return (dividend + divisor - 1L) / divisor;
    }

    private static void validate(AssessmentInput input) {
        if (input.missedYears() < 0
                || input.missedMonths() < 0
                || input.missedDays() < 0) {
            throw new com.thabat.prayer.common.exception.InvalidAssessmentException(
                    "Years, months, and days cannot be negative"
            );
        }

        if (input.missedYears() > MAX_MISSED_YEARS) {
            throw new com.thabat.prayer.common.exception.InvalidAssessmentException(
                    "Missed years cannot exceed " + MAX_MISSED_YEARS
            );
        }

        if (input.missedYears() == 0
                && input.missedMonths() == 0
                && input.missedDays() == 0) {
            throw new com.thabat.prayer.common.exception.InvalidAssessmentException(
                    "Enter at least one missed year, month, or day"
            );
        }

        if (input.dailyRecoveryTarget() < 1 || input.dailyRecoveryTarget() > 100) {
            throw new com.thabat.prayer.common.exception.InvalidAssessmentException(
                    "Daily recovery target must be between 1 and 100"
            );
        }
    }
}
