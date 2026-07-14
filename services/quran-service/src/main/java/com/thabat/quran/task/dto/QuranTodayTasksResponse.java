package com.thabat.quran.task.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record QuranTodayTasksResponse(
        LocalDate date,
        List<QuranDailyTaskResponse> memorizationTasks,
        List<QuranDailyTaskResponse> revisionTasks,
        int completedTasks,
        int pendingTasks,
        int skippedTasks,
        int totalTasks,
        BigDecimal completionPercentage
) {
}
