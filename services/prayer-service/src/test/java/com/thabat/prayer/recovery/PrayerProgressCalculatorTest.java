package com.thabat.prayer.recovery;

import com.thabat.prayer.assessment.PrayerAssessment;
import com.thabat.prayer.recovery.dto.PrayerProgressResponse;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PrayerProgressCalculatorTest {

    @Test
    void estimatedRemainingDays_usesCeilingDivision() {
        assertThat(PrayerProgressCalculator.estimatedRemainingDays(3, 5)).isEqualTo(1L);
        assertThat(PrayerProgressCalculator.estimatedRemainingDays(5, 5)).isEqualTo(1L);
        assertThat(PrayerProgressCalculator.estimatedRemainingDays(6, 5)).isEqualTo(2L);
        assertThat(PrayerProgressCalculator.estimatedRemainingDays(0, 5)).isEqualTo(0L);
    }

    @Test
    void calculate_includesEstimatedRemainingDays() {
        PrayerAssessment assessment = PrayerAssessment.builder()
                .userId(UUID.randomUUID())
                .missedYears(0)
                .missedMonths(0)
                .missedDays(2)
                .totalEstimatedDays(2)
                .fajrRemaining(2)
                .dhuhrRemaining(2)
                .asrRemaining(2)
                .maghribRemaining(2)
                .ishaRemaining(2)
                .totalRemainingPrayers(10)
                .dailyRecoveryTarget(5)
                .build();

        PrayerProgressResponse progress = PrayerProgressCalculator.calculate(assessment, 7);

        assertThat(progress.totalRemainingPrayers()).isEqualTo(3L);
        assertThat(progress.estimatedRemainingDays()).isEqualTo(1L);
    }

    @Test
    void calculate_returnsZeroEstimatedDaysWhenComplete() {
        PrayerAssessment assessment = PrayerAssessment.builder()
                .userId(UUID.randomUUID())
                .missedYears(0)
                .missedMonths(0)
                .missedDays(1)
                .totalEstimatedDays(1)
                .fajrRemaining(1)
                .dhuhrRemaining(1)
                .asrRemaining(1)
                .maghribRemaining(1)
                .ishaRemaining(1)
                .totalRemainingPrayers(5)
                .dailyRecoveryTarget(5)
                .build();

        PrayerProgressResponse progress = PrayerProgressCalculator.calculate(assessment, 5);

        assertThat(progress.totalRemainingPrayers()).isEqualTo(0L);
        assertThat(progress.estimatedRemainingDays()).isEqualTo(0L);
    }
}
