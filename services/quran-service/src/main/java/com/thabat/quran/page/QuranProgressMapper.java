package com.thabat.quran.page;

import com.thabat.quran.page.dto.QuranPageProgressResponse;
import com.thabat.quran.page.dto.QuranProgressSummaryResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class QuranProgressMapper {

    public QuranPageProgressResponse toResponse(QuranPageProgress entity) {
        return new QuranPageProgressResponse(
                entity.getPageNumber(),
                entity.getStatus(),
                entity.getMemorizedAt(),
                entity.getLastReviewedAt(),
                entity.getSuccessfulReviewCount(),
                entity.getConfidenceLevel(),
                entity.getNotes()
        );
    }

    public QuranPageProgressResponse notStarted(int pageNumber) {
        return new QuranPageProgressResponse(
                pageNumber,
                QuranPageStatus.NOT_STARTED,
                null,
                null,
                0,
                null,
                null
        );
    }

    public QuranProgressSummaryResponse toSummary(
            Map<QuranPageStatus, Long> statusCounts,
            int reviewedThisWeek,
            Integer lastUpdatedPage
    ) {
        int learning = statusCounts.getOrDefault(QuranPageStatus.LEARNING, 0L).intValue();
        int memorized = statusCounts.getOrDefault(QuranPageStatus.MEMORIZED, 0L).intValue();
        int needsRevision = statusCounts.getOrDefault(QuranPageStatus.NEEDS_REVISION, 0L).intValue();
        int strong = statusCounts.getOrDefault(QuranPageStatus.STRONG, 0L).intValue();

        // Missing rows are treated as NOT_STARTED; stored NOT_STARTED rows also count.
        int notStarted =
                QuranPageConstants.TOTAL_PAGES - (learning + memorized + needsRevision + strong);

        int completedPages = memorized + needsRevision + strong;
        double completionPercentage = BigDecimal
                .valueOf(completedPages)
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        BigDecimal.valueOf(QuranPageConstants.TOTAL_PAGES),
                        2,
                        RoundingMode.HALF_UP
                )
                .doubleValue();

        return new QuranProgressSummaryResponse(
                QuranPageConstants.TOTAL_PAGES,
                notStarted,
                learning,
                memorized,
                needsRevision,
                strong,
                completedPages,
                completionPercentage,
                reviewedThisWeek,
                lastUpdatedPage
        );
    }

    public Map<QuranPageStatus, Long> emptyStatusCounts() {
        Map<QuranPageStatus, Long> counts = new EnumMap<>(QuranPageStatus.class);
        for (QuranPageStatus status : QuranPageStatus.values()) {
            counts.put(status, 0L);
        }
        return counts;
    }

    public Map<QuranPageStatus, Long> countStatuses(List<QuranPageProgress> pages) {
        Map<QuranPageStatus, Long> counts = emptyStatusCounts();
        for (QuranPageProgress page : pages) {
            counts.merge(page.getStatus(), 1L, Long::sum);
        }
        return counts;
    }
}
