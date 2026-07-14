package com.thabat.quran.aggregate.dto;

import java.util.List;

public record QuranSectionDetailResponse(
        String sectionType,
        int sectionNumber,
        String title,
        String nameArabic,
        String nameEnglish,
        String transliteration,
        Integer ayahCount,
        String revelationType,
        int startPage,
        int endPage,
        int totalPages,
        int notStartedPages,
        int learningPages,
        int memorizedPages,
        int needsRevisionPages,
        int strongPages,
        int completedPages,
        double completionPercentage,
        List<QuranSectionPageResponse> pages
) {
}
