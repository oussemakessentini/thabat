package com.thabat.quran.aggregate.dto;

public record QuranHizbProgressResponse(
        int hizbNumber,
        int juzNumber,
        int startPage,
        int endPage,
        int totalPages,
        int notStartedPages,
        int learningPages,
        int memorizedPages,
        int needsRevisionPages,
        int strongPages,
        int completedPages,
        double completionPercentage
) {
}
