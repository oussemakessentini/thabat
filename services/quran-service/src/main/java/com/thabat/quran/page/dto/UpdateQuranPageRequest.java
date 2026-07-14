package com.thabat.quran.page.dto;

import com.thabat.quran.page.QuranPageStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateQuranPageRequest(
        QuranPageStatus status,
        LocalDate memorizedAt,
        @Min(value = 1, message = "confidenceLevel must be between 1 and 5")
        @Max(value = 5, message = "confidenceLevel must be between 1 and 5")
        Integer confidenceLevel,
        @Size(max = 1000, message = "notes must be at most 1000 characters")
        String notes
) {
}
