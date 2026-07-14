package com.thabat.prayer.assessment;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MissedPrayerCalculatorTest {

    @Test
    void validCalculation() {
        var result = MissedPrayerCalculator.calculate(
                new MissedPrayerCalculator.AssessmentInput(2, 3, 10, 5)
        );

        assertThat(result.totalEstimatedDays()).isEqualTo(830L);
        assertThat(result.fajrRemaining()).isEqualTo(830L);
        assertThat(result.totalRemainingPrayers()).isEqualTo(4150L);
        assertThat(result.estimatedCompletionDays()).isEqualTo(830L);
    }

    @Test
    void completionDaysUsesCeilingDivision() {
        // 5 prayers/day * 1 day = 5 remaining; target 2 → ceil(5/2) = 3
        var result = MissedPrayerCalculator.calculate(
                new MissedPrayerCalculator.AssessmentInput(0, 0, 1, 2)
        );

        assertThat(result.totalRemainingPrayers()).isEqualTo(5L);
        assertThat(result.estimatedCompletionDays()).isEqualTo(3L);
        assertThat(MissedPrayerCalculator.ceilingDivide(5, 2)).isEqualTo(3L);
        assertThat(MissedPrayerCalculator.ceilingDivide(4150, 5)).isEqualTo(830L);
    }

    @Test
    void allZeroThrows() {
        assertThatThrownBy(() -> MissedPrayerCalculator.calculate(
                new MissedPrayerCalculator.AssessmentInput(0, 0, 0, 5)
        )).hasMessageContaining("at least one");
    }

    @Test
    void moreThan120YearsThrows() {
        assertThatThrownBy(() -> MissedPrayerCalculator.calculate(
                new MissedPrayerCalculator.AssessmentInput(121, 0, 0, 5)
        )).hasMessageContaining("120");
    }
}
