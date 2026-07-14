package com.thabat.quran.page.dto;

import com.thabat.quran.page.QuranPageStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RecordQuranReviewRequest(
        @NotNull(message = "reviewedAt is required")
        LocalDate reviewedAt,
        @NotNull(message = "successful is required")
        Boolean successful,
        @Min(value = 1, message = "confidenceLevel must be between 1 and 5")
        @Max(value = 5, message = "confidenceLevel must be between 1 and 5")
        Integer confidenceLevel,
        QuranPageStatus newStatus
) {
}
