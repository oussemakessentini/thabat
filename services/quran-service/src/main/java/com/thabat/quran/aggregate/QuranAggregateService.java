package com.thabat.quran.aggregate;

import com.thabat.quran.aggregate.dto.QuranAggregateProgress;
import com.thabat.quran.aggregate.dto.QuranHizbProgressResponse;
import com.thabat.quran.aggregate.dto.QuranJuzProgressResponse;
import com.thabat.quran.aggregate.dto.QuranSectionDetailResponse;
import com.thabat.quran.aggregate.dto.QuranSectionPageResponse;
import com.thabat.quran.aggregate.dto.QuranSurahProgressResponse;
import com.thabat.quran.common.exception.ResourceNotFoundException;
import com.thabat.quran.page.QuranPageProgress;
import com.thabat.quran.page.QuranPageProgressRepository;
import com.thabat.quran.page.QuranPageStatus;
import com.thabat.quran.reference.QuranHizb;
import com.thabat.quran.reference.QuranHizbRepository;
import com.thabat.quran.reference.QuranJuz;
import com.thabat.quran.reference.QuranJuzRepository;
import com.thabat.quran.reference.QuranPageSurahRange;
import com.thabat.quran.reference.QuranPageSurahRangeRepository;
import com.thabat.quran.reference.QuranSurah;
import com.thabat.quran.reference.QuranSurahRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuranAggregateService {

    private final QuranSurahRepository surahRepository;
    private final QuranJuzRepository juzRepository;
    private final QuranHizbRepository hizbRepository;
    private final QuranPageSurahRangeRepository pageSurahRangeRepository;
    private final QuranPageProgressRepository pageProgressRepository;

    @Transactional(readOnly = true)
    public List<QuranSurahProgressResponse> listSurahs(UUID userId) {
        Map<Integer, QuranPageStatus> progressByPage = loadProgressByPage(userId);
        Map<Integer, List<QuranPageSurahRange>> rangesBySurah = loadRangesBySurah();
        return surahRepository.findAllByOrderBySurahNumberAsc().stream()
                .map(surah -> toSurahResponse(
                        surah,
                        rangesBySurah.getOrDefault(surah.getSurahNumber(), List.of()),
                        progressByPage
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public QuranSectionDetailResponse getSurah(UUID userId, int surahNumber) {
        QuranSurah surah = surahRepository.findById(surahNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Surah not found: " + surahNumber));
        List<QuranPageSurahRange> ranges =
                pageSurahRangeRepository.findBySurahNumberOrderByPageNumberAsc(surahNumber);
        Map<Integer, QuranPageStatus> progressByPage = loadProgressByPage(userId);

        List<QuranSectionPageResponse> pages = new ArrayList<>(ranges.size());
        Map<QuranPageStatus, Integer> counts = emptyCounts();
        for (QuranPageSurahRange range : ranges) {
            QuranPageStatus status = progressByPage.getOrDefault(
                    range.getPageNumber(),
                    QuranPageStatus.NOT_STARTED
            );
            counts.merge(status, 1, Integer::sum);
            pages.add(new QuranSectionPageResponse(
                    range.getPageNumber(),
                    range.getStartAyah(),
                    range.getEndAyah(),
                    status
            ));
        }

        QuranAggregateProgress aggregate = QuranAggregateCalculator.calculate(counts, pages.size());
        return toDetail(
                "SURAH",
                surah.getSurahNumber(),
                surah.getTransliteration(),
                surah.getNameArabic(),
                surah.getNameEnglish(),
                surah.getTransliteration(),
                surah.getAyahCount(),
                surah.getRevelationType().name(),
                surah.getStartPage(),
                surah.getEndPage(),
                aggregate,
                pages
        );
    }

    @Transactional(readOnly = true)
    public List<QuranJuzProgressResponse> listJuz(UUID userId) {
        Map<Integer, QuranPageStatus> progressByPage = loadProgressByPage(userId);
        return juzRepository.findAllByOrderByJuzNumberAsc().stream()
                .map(juz -> toJuzResponse(juz, progressByPage))
                .toList();
    }

    @Transactional(readOnly = true)
    public QuranSectionDetailResponse getJuz(UUID userId, int juzNumber) {
        QuranJuz juz = juzRepository.findById(juzNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Juz not found: " + juzNumber));
        return toRangeDetail(
                "JUZ",
                juz.getJuzNumber(),
                "Juz " + juz.getJuzNumber(),
                null,
                null,
                null,
                null,
                null,
                juz.getStartPage(),
                juz.getEndPage(),
                userId
        );
    }

    @Transactional(readOnly = true)
    public List<QuranHizbProgressResponse> listHizbs(UUID userId) {
        Map<Integer, QuranPageStatus> progressByPage = loadProgressByPage(userId);
        return hizbRepository.findAllByOrderByHizbNumberAsc().stream()
                .map(hizb -> toHizbResponse(hizb, progressByPage))
                .toList();
    }

    @Transactional(readOnly = true)
    public QuranSectionDetailResponse getHizb(UUID userId, int hizbNumber) {
        QuranHizb hizb = hizbRepository.findById(hizbNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Hizb not found: " + hizbNumber));
        return toRangeDetail(
                "HIZB",
                hizb.getHizbNumber(),
                "Hizb " + hizb.getHizbNumber(),
                null,
                null,
                null,
                null,
                null,
                hizb.getStartPage(),
                hizb.getEndPage(),
                userId
        );
    }

    private QuranSurahProgressResponse toSurahResponse(
            QuranSurah surah,
            List<QuranPageSurahRange> ranges,
            Map<Integer, QuranPageStatus> progressByPage
    ) {
        List<Integer> pageNumbers = ranges.stream().map(QuranPageSurahRange::getPageNumber).toList();
        QuranAggregateProgress aggregate = aggregateForPages(pageNumbers, progressByPage);
        return new QuranSurahProgressResponse(
                surah.getSurahNumber(),
                surah.getNameArabic(),
                surah.getNameEnglish(),
                surah.getTransliteration(),
                surah.getAyahCount(),
                surah.getRevelationType(),
                surah.getStartPage(),
                surah.getEndPage(),
                aggregate.totalPages(),
                aggregate.notStartedPages(),
                aggregate.learningPages(),
                aggregate.memorizedPages(),
                aggregate.needsRevisionPages(),
                aggregate.strongPages(),
                aggregate.completedPages(),
                aggregate.completionPercentage()
        );
    }

    private QuranJuzProgressResponse toJuzResponse(
            QuranJuz juz,
            Map<Integer, QuranPageStatus> progressByPage
    ) {
        List<Integer> pageNumbers = pagesInRange(juz.getStartPage(), juz.getEndPage());
        QuranAggregateProgress aggregate = aggregateForPages(pageNumbers, progressByPage);
        return new QuranJuzProgressResponse(
                juz.getJuzNumber(),
                juz.getStartPage(),
                juz.getEndPage(),
                aggregate.totalPages(),
                aggregate.notStartedPages(),
                aggregate.learningPages(),
                aggregate.memorizedPages(),
                aggregate.needsRevisionPages(),
                aggregate.strongPages(),
                aggregate.completedPages(),
                aggregate.completionPercentage()
        );
    }

    private QuranHizbProgressResponse toHizbResponse(
            QuranHizb hizb,
            Map<Integer, QuranPageStatus> progressByPage
    ) {
        List<Integer> pageNumbers = pagesInRange(hizb.getStartPage(), hizb.getEndPage());
        QuranAggregateProgress aggregate = aggregateForPages(pageNumbers, progressByPage);
        return new QuranHizbProgressResponse(
                hizb.getHizbNumber(),
                hizb.getJuzNumber(),
                hizb.getStartPage(),
                hizb.getEndPage(),
                aggregate.totalPages(),
                aggregate.notStartedPages(),
                aggregate.learningPages(),
                aggregate.memorizedPages(),
                aggregate.needsRevisionPages(),
                aggregate.strongPages(),
                aggregate.completedPages(),
                aggregate.completionPercentage()
        );
    }

    private QuranSectionDetailResponse toRangeDetail(
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
            UUID userId
    ) {
        Map<Integer, QuranPageStatus> progressByPage = loadProgressByPage(userId);
        List<Integer> pageNumbers = pagesInRange(startPage, endPage);
        List<QuranSectionPageResponse> pages = new ArrayList<>(pageNumbers.size());
        Map<QuranPageStatus, Integer> counts = emptyCounts();
        for (Integer pageNumber : pageNumbers) {
            QuranPageStatus status = progressByPage.getOrDefault(
                    pageNumber,
                    QuranPageStatus.NOT_STARTED
            );
            counts.merge(status, 1, Integer::sum);
            pages.add(new QuranSectionPageResponse(pageNumber, null, null, status));
        }
        QuranAggregateProgress aggregate = QuranAggregateCalculator.calculate(counts, pages.size());
        return toDetail(
                sectionType,
                sectionNumber,
                title,
                nameArabic,
                nameEnglish,
                transliteration,
                ayahCount,
                revelationType,
                startPage,
                endPage,
                aggregate,
                pages
        );
    }

    private QuranSectionDetailResponse toDetail(
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
            QuranAggregateProgress aggregate,
            List<QuranSectionPageResponse> pages
    ) {
        return new QuranSectionDetailResponse(
                sectionType,
                sectionNumber,
                title,
                nameArabic,
                nameEnglish,
                transliteration,
                ayahCount,
                revelationType,
                startPage,
                endPage,
                aggregate.totalPages(),
                aggregate.notStartedPages(),
                aggregate.learningPages(),
                aggregate.memorizedPages(),
                aggregate.needsRevisionPages(),
                aggregate.strongPages(),
                aggregate.completedPages(),
                aggregate.completionPercentage(),
                pages
        );
    }

    private QuranAggregateProgress aggregateForPages(
            List<Integer> pageNumbers,
            Map<Integer, QuranPageStatus> progressByPage
    ) {
        Map<QuranPageStatus, Integer> counts = emptyCounts();
        for (Integer pageNumber : pageNumbers) {
            QuranPageStatus status = progressByPage.getOrDefault(
                    pageNumber,
                    QuranPageStatus.NOT_STARTED
            );
            counts.merge(status, 1, Integer::sum);
        }
        return QuranAggregateCalculator.calculate(counts, pageNumbers.size());
    }

    private Map<Integer, QuranPageStatus> loadProgressByPage(UUID userId) {
        Map<Integer, QuranPageStatus> progressByPage = new HashMap<>();
        for (QuranPageProgress page : pageProgressRepository.findByUserIdOrderByPageNumberAsc(userId)) {
            progressByPage.put(page.getPageNumber(), page.getStatus());
        }
        return progressByPage;
    }

    private Map<Integer, List<QuranPageSurahRange>> loadRangesBySurah() {
        Map<Integer, List<QuranPageSurahRange>> rangesBySurah = new HashMap<>();
        for (QuranPageSurahRange range :
                pageSurahRangeRepository.findAllByOrderBySurahNumberAscPageNumberAsc()) {
            rangesBySurah
                    .computeIfAbsent(range.getSurahNumber(), key -> new ArrayList<>())
                    .add(range);
        }
        return rangesBySurah;
    }

    private static List<Integer> pagesInRange(int startPage, int endPage) {
        List<Integer> pages = new ArrayList<>(endPage - startPage + 1);
        for (int page = startPage; page <= endPage; page++) {
            pages.add(page);
        }
        return pages;
    }

    private static Map<QuranPageStatus, Integer> emptyCounts() {
        Map<QuranPageStatus, Integer> counts = new EnumMap<>(QuranPageStatus.class);
        for (QuranPageStatus status : QuranPageStatus.values()) {
            counts.put(status, 0);
        }
        return counts;
    }
}
