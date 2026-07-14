package com.thabat.quran.aggregate;

import com.thabat.quran.aggregate.dto.QuranAggregateProgress;
import com.thabat.quran.page.QuranPageStatus;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class QuranAggregateCalculatorTest {

    @Test
    void calculatesAllStatusBucketsAndPercentage() {
        Map<QuranPageStatus, Integer> counts = new EnumMap<>(QuranPageStatus.class);
        counts.put(QuranPageStatus.NOT_STARTED, 10);
        counts.put(QuranPageStatus.LEARNING, 2);
        counts.put(QuranPageStatus.MEMORIZED, 5);
        counts.put(QuranPageStatus.NEEDS_REVISION, 1);
        counts.put(QuranPageStatus.STRONG, 2);

        QuranAggregateProgress progress = QuranAggregateCalculator.calculate(counts, 20);

        assertThat(progress.totalPages()).isEqualTo(20);
        assertThat(progress.notStartedPages()).isEqualTo(10);
        assertThat(progress.learningPages()).isEqualTo(2);
        assertThat(progress.memorizedPages()).isEqualTo(5);
        assertThat(progress.needsRevisionPages()).isEqualTo(1);
        assertThat(progress.strongPages()).isEqualTo(2);
        assertThat(progress.completedPages()).isEqualTo(8);
        assertThat(progress.completionPercentage()).isEqualTo(40.0);
    }

    @Test
    void roundsPercentageToTwoDecimals() {
        Map<QuranPageStatus, Integer> counts = new EnumMap<>(QuranPageStatus.class);
        counts.put(QuranPageStatus.MEMORIZED, 1);
        counts.put(QuranPageStatus.NOT_STARTED, 6);

        QuranAggregateProgress progress = QuranAggregateCalculator.calculate(counts, 7);

        assertThat(progress.completionPercentage()).isEqualTo(14.29);
    }
}
