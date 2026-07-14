package com.thabat.prayer.assessment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreatePrayerAssessmentRequest(

        @NotNull(message = "Missed years is required")
        @Min(value = 0, message = "Missed years cannot be negative")
        @Max(value = 120, message = "Missed years cannot exceed 120")
        Integer missedYears,

        @NotNull(message = "Missed months is required")
        @Min(value = 0, message = "Missed months cannot be negative")
        Integer missedMonths,

        @NotNull(message = "Missed days is required")
        @Min(value = 0, message = "Missed days cannot be negative")
        Integer missedDays,

        @NotNull(message = "Daily recovery target is required")
        @Min(value = 1, message = "Daily recovery target must be at least 1")
        @Max(value = 100, message = "Daily recovery target cannot exceed 100")
        Integer dailyRecoveryTarget
) {
}
