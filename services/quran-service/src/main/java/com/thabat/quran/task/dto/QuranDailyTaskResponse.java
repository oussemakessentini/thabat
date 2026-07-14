package com.thabat.quran.task.dto;

import com.thabat.quran.task.QuranTaskStatus;
import com.thabat.quran.task.QuranTaskType;

import java.util.UUID;

public record QuranDailyTaskResponse(
        UUID id,
        int pageNumber,
        QuranTaskType taskType,
        QuranTaskStatus status
) {
}
