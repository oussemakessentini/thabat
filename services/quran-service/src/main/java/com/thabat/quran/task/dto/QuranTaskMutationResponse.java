package com.thabat.quran.task.dto;

public record QuranTaskMutationResponse(
        QuranDailyTaskResponse task,
        QuranTodayTasksResponse today
) {
}
