package com.thabat.quran.aggregate.dto;

public record QuranAggregateProgress(
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
