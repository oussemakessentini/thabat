package com.thabat.quran.aggregate;

import com.thabat.quran.aggregate.dto.QuranAggregateProgress;
import com.thabat.quran.page.QuranPageStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

final class QuranAggregateCalculator {

    private QuranAggregateCalculator() {
    }

    /**
     * Aggregates page statuses for a section.
     * completedPages = MEMORIZED + NEEDS_REVISION + STRONG.
     * Surah MVP progress is page-based (not ayah-weighted).
     */
    static QuranAggregateProgress calculate(Map<QuranPageStatus, Integer> statusCounts, int totalPages) {
        int notStarted = statusCounts.getOrDefault(QuranPageStatus.NOT_STARTED, 0);
        int learning = statusCounts.getOrDefault(QuranPageStatus.LEARNING, 0);
        int memorized = statusCounts.getOrDefault(QuranPageStatus.MEMORIZED, 0);
        int needsRevision = statusCounts.getOrDefault(QuranPageStatus.NEEDS_REVISION, 0);
        int strong = statusCounts.getOrDefault(QuranPageStatus.STRONG, 0);
        int completed = memorized + needsRevision + strong;

        double percentage = totalPages == 0
                ? 0.0
                : BigDecimal.valueOf(completed)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalPages), 2, RoundingMode.HALF_UP)
                .doubleValue();

        return new QuranAggregateProgress(
                totalPages,
                notStarted,
                learning,
                memorized,
                needsRevision,
                strong,
                completed,
                percentage
        );
    }
}
