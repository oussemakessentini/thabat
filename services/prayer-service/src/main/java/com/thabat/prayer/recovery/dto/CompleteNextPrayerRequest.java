package com.thabat.prayer.recovery.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CompleteNextPrayerRequest(

        @NotNull(message = "Assessment id is required")
        UUID assessmentId,

        @NotNull(message = "Completion date is required")
        LocalDate completedOn
) {
}
