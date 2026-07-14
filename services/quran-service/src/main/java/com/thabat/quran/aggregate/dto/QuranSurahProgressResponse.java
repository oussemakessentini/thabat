package com.thabat.quran.aggregate.dto;

import com.thabat.quran.reference.QuranRevelationType;

public record QuranSurahProgressResponse(
        int surahNumber,
        String nameArabic,
        String nameEnglish,
        String transliteration,
        int ayahCount,
        QuranRevelationType revelationType,
        int startPage,
        int endPage,
        int totalPages,
        int notStartedPages,
        int learningPages,
        int memorizedPages,
        int needsRevisionPages,
        int strongPages,
        int completedPages,
        double completionPercentage
) {
}
