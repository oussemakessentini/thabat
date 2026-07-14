package com.thabat.quran.page;

import com.thabat.quran.common.exception.InvalidQuranProgressException;
import com.thabat.quran.page.dto.QuranPageProgressResponse;
import com.thabat.quran.page.dto.QuranProgressSummaryResponse;
import com.thabat.quran.page.dto.RecordQuranReviewRequest;
import com.thabat.quran.page.dto.UpdateQuranPageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuranPageService {

    private final QuranPageProgressRepository repository;
    private final QuranProgressMapper mapper;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<QuranPageProgressResponse> listPages(UUID userId, QuranPageStatus statusFilter) {
        List<QuranPageProgress> stored = repository.findByUserIdOrderByPageNumberAsc(userId);
        Map<Integer, QuranPageProgress> byPage = new HashMap<>();
        for (QuranPageProgress page : stored) {
            byPage.put(page.getPageNumber(), page);
        }

        if (statusFilter == null) {
            List<QuranPageProgressResponse> pages =
                    new ArrayList<>(QuranPageConstants.TOTAL_PAGES);
            for (int pageNumber = QuranPageConstants.MIN_PAGE;
                 pageNumber <= QuranPageConstants.MAX_PAGE;
                 pageNumber++) {
                QuranPageProgress entity = byPage.get(pageNumber);
                pages.add(entity == null
                        ? mapper.notStarted(pageNumber)
                        : mapper.toResponse(entity));
            }
            return pages;
        }

        if (statusFilter == QuranPageStatus.NOT_STARTED) {
            Set<Integer> nonDefaultPages = new HashSet<>();
            for (QuranPageProgress page : stored) {
                if (page.getStatus() != QuranPageStatus.NOT_STARTED) {
                    nonDefaultPages.add(page.getPageNumber());
                }
            }

            List<QuranPageProgressResponse> pages = new ArrayList<>();
            for (int pageNumber = QuranPageConstants.MIN_PAGE;
                 pageNumber <= QuranPageConstants.MAX_PAGE;
                 pageNumber++) {
                if (!nonDefaultPages.contains(pageNumber)) {
                    QuranPageProgress entity = byPage.get(pageNumber);
                    pages.add(entity == null
                            ? mapper.notStarted(pageNumber)
                            : mapper.toResponse(entity));
                }
            }
            return pages;
        }

        return stored.stream()
                .filter(page -> page.getStatus() == statusFilter)
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuranPageProgressResponse getPage(UUID userId, int pageNumber) {
        validatePageNumber(pageNumber);
        return repository.findByUserIdAndPageNumber(userId, pageNumber)
                .map(mapper::toResponse)
                .orElseGet(() -> mapper.notStarted(pageNumber));
    }

    @Transactional
    public QuranPageProgressResponse updatePage(
            UUID userId,
            int pageNumber,
            UpdateQuranPageRequest request
    ) {
        validatePageNumber(pageNumber);
        LocalDate today = LocalDate.now(clock);
        rejectFutureDate(request.memorizedAt(), "memorizedAt", today);

        QuranPageProgress page = repository
                .findByUserIdAndPageNumber(userId, pageNumber)
                .orElseGet(() -> newPage(userId, pageNumber));

        if (request.status() != null) {
            page.setStatus(request.status());
        }

        if (request.memorizedAt() != null) {
            page.setMemorizedAt(request.memorizedAt());
        }

        if (request.confidenceLevel() != null) {
            page.setConfidenceLevel(request.confidenceLevel());
        }

        if (request.notes() != null) {
            String normalized = normalizeNotes(request.notes());
            page.setNotes(normalized);
        }

        applyMemorizedDefaults(page, today);

        return mapper.toResponse(repository.save(page));
    }

    @Transactional
    public QuranPageProgressResponse recordReview(
            UUID userId,
            int pageNumber,
            RecordQuranReviewRequest request
    ) {
        validatePageNumber(pageNumber);
        LocalDate today = LocalDate.now(clock);
        rejectFutureDate(request.reviewedAt(), "reviewedAt", today);

        QuranPageProgress page = repository
                .findByUserIdAndPageNumber(userId, pageNumber)
                .orElseGet(() -> newPage(userId, pageNumber));

        page.setLastReviewedAt(request.reviewedAt());

        if (Boolean.TRUE.equals(request.successful())) {
            page.setSuccessfulReviewCount(page.getSuccessfulReviewCount() + 1);
        }

        if (request.confidenceLevel() != null) {
            page.setConfidenceLevel(request.confidenceLevel());
        }

        if (request.newStatus() != null) {
            page.setStatus(request.newStatus());
        }

        applyMemorizedDefaults(page, today);

        return mapper.toResponse(repository.save(page));
    }

    @Transactional(readOnly = true)
    public QuranProgressSummaryResponse getProgress(UUID userId) {
        List<QuranPageProgress> stored = repository.findByUserIdOrderByPageNumberAsc(userId);
        Map<QuranPageStatus, Long> statusCounts = mapper.countStatuses(stored);

        LocalDate today = LocalDate.now(clock);
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        int reviewedThisWeek = (int) repository.countByUserIdAndLastReviewedAtBetween(
                userId,
                weekStart,
                weekEnd
        );

        Integer lastUpdatedPage = repository.findFirstByUserIdOrderByUpdatedAtDesc(userId)
                .map(QuranPageProgress::getPageNumber)
                .orElse(null);

        return mapper.toSummary(statusCounts, reviewedThisWeek, lastUpdatedPage);
    }

    private QuranPageProgress newPage(UUID userId, int pageNumber) {
        QuranPageProgress page = new QuranPageProgress();
        page.setUserId(userId);
        page.setPageNumber(pageNumber);
        page.setStatus(QuranPageStatus.NOT_STARTED);
        page.setSuccessfulReviewCount(0);
        return page;
    }

    private void applyMemorizedDefaults(QuranPageProgress page, LocalDate today) {
        if (page.getStatus() == QuranPageStatus.MEMORIZED && page.getMemorizedAt() == null) {
            page.setMemorizedAt(today);
        }
    }

    private String normalizeNotes(String notes) {
        String trimmed = notes.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void validatePageNumber(int pageNumber) {
        if (!QuranPageConstants.isValidPageNumber(pageNumber)) {
            throw new InvalidQuranProgressException(
                    "pageNumber must be between "
                            + QuranPageConstants.MIN_PAGE
                            + " and "
                            + QuranPageConstants.MAX_PAGE
            );
        }
    }

    private void rejectFutureDate(LocalDate date, String field, LocalDate today) {
        if (date != null && date.isAfter(today)) {
            throw new InvalidQuranProgressException(field + " cannot be in the future");
        }
    }
}
