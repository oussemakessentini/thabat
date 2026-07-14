package com.thabat.quran.page.dto;

import com.thabat.quran.page.QuranPageStatus;

import java.time.LocalDate;

public record QuranPageProgressResponse(
        int pageNumber,
        QuranPageStatus status,
        LocalDate memorizedAt,
        LocalDate lastReviewedAt,
        int successfulReviewCount,
        Integer confidenceLevel,
        String notes
) {
}
