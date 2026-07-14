package com.thabat.events.quran;

import java.time.Instant;
import java.util.UUID;

/**
 * Emitted when a Quran daily task is completed successfully.
 * Minimal public payload — no notes, tokens, or secrets.
 */
public record QuranTaskCompletedEvent(
        UUID userId,
        UUID taskId,
        int pageNumber,
        String taskType,
        Instant completedAt
) {
    public static final String EVENT_TYPE = "QuranTaskCompleted";
    public static final int EVENT_VERSION = 1;
}
