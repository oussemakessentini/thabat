package com.thabat.quran.page;

import com.thabat.quran.page.dto.QuranProgressSummaryResponse;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class QuranProgressMapperTest {

    private final QuranProgressMapper mapper = new QuranProgressMapper();

    @Test
    void completionPercentage_roundsToTwoDecimals() {
        Map<QuranPageStatus, Long> counts = new EnumMap<>(QuranPageStatus.class);
        counts.put(QuranPageStatus.MEMORIZED, 50L);
        counts.put(QuranPageStatus.NEEDS_REVISION, 20L);
        counts.put(QuranPageStatus.STRONG, 14L);

        QuranProgressSummaryResponse summary = mapper.toSummary(counts, 7, 120);

        assertThat(summary.totalPages()).isEqualTo(604);
        assertThat(summary.completedPages()).isEqualTo(84);
        assertThat(summary.completionPercentage()).isEqualTo(13.91);
        assertThat(summary.notStartedPages()).isEqualTo(520);
        assertThat(summary.learningPages()).isEqualTo(0);
        assertThat(summary.memorizedPages()).isEqualTo(50);
        assertThat(summary.needsRevisionPages()).isEqualTo(20);
        assertThat(summary.strongPages()).isEqualTo(14);
        assertThat(summary.reviewedThisWeek()).isEqualTo(7);
        assertThat(summary.lastUpdatedPage()).isEqualTo(120);
    }

    @Test
    void progressTotals_equal604() {
        Map<QuranPageStatus, Long> counts = new EnumMap<>(QuranPageStatus.class);
        counts.put(QuranPageStatus.LEARNING, 20L);
        counts.put(QuranPageStatus.MEMORIZED, 50L);
        counts.put(QuranPageStatus.NEEDS_REVISION, 20L);
        counts.put(QuranPageStatus.STRONG, 14L);

        QuranProgressSummaryResponse summary = mapper.toSummary(counts, 0, null);

        assertThat(
                summary.notStartedPages()
                        + summary.learningPages()
                        + summary.memorizedPages()
                        + summary.needsRevisionPages()
                        + summary.strongPages()
        ).isEqualTo(604);
    }

    @Test
    void emptyProgress_allNotStarted() {
        QuranProgressSummaryResponse summary =
                mapper.toSummary(mapper.emptyStatusCounts(), 0, null);

        assertThat(summary.notStartedPages()).isEqualTo(604);
        assertThat(summary.completedPages()).isEqualTo(0);
        assertThat(summary.completionPercentage()).isEqualTo(0.0);
        assertThat(summary.lastUpdatedPage()).isNull();
    }
}
