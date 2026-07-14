package com.thabat.quran.task.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CompleteQuranTaskRequest(
        @Min(value = 1, message = "confidenceLevel must be between 1 and 5")
        @Max(value = 5, message = "confidenceLevel must be between 1 and 5")
        Integer confidenceLevel,
        Boolean successful
) {
}
