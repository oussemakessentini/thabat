package com.thabat.prayer.recovery.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UndoLatestRecoveryRequest(

        @NotNull(message = "Assessment id is required")
        UUID assessmentId
) {
}
