package com.thabat.quran.goal.dto;

import java.util.UUID;

public record QuranDailyGoalResponse(
        UUID id,
        int memorizationPagesPerDay,
        int revisionPagesPerDay,
        Integer preferredStartPage,
        boolean active
) {
}
