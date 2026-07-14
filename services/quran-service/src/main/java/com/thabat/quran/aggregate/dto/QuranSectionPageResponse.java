package com.thabat.quran.aggregate.dto;

import com.thabat.quran.page.QuranPageStatus;

public record QuranSectionPageResponse(
        int pageNumber,
        Integer startAyah,
        Integer endAyah,
        QuranPageStatus status
) {
}
