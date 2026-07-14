package com.thabat.quran.page.dto;

public record QuranProgressSummaryResponse(
        int totalPages,
        int notStartedPages,
        int learningPages,
        int memorizedPages,
        int needsRevisionPages,
        int strongPages,
        int completedPages,
        double completionPercentage,
        int reviewedThisWeek,
        Integer lastUpdatedPage
) {
}
