package com.thabat.quran.task;

import com.thabat.quran.goal.QuranDailyGoal;
import com.thabat.quran.page.QuranPageConstants;
import com.thabat.quran.page.QuranPageProgress;
import com.thabat.quran.page.QuranPageStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Deterministic memorization and revision page selection for today's tasks.
 * Not a spaced-repetition engine — fixed priority rules for MVP.
 */
@Component
public class QuranTaskGenerator {

    public List<QuranDailyTask> generate(
            UUID userId,
            QuranDailyGoal goal,
            LocalDate taskDate,
            List<QuranPageProgress> storedPages
    ) {
        Map<Integer, QuranPageProgress> byPage = indexByPage(storedPages);
        Set<Integer> usedPages = new HashSet<>();
        List<QuranDailyTask> tasks = new ArrayList<>();

        for (int pageNumber : selectMemorizationPages(goal, byPage)) {
            usedPages.add(pageNumber);
            tasks.add(newTask(
                    userId,
                    goal.getId(),
                    taskDate,
                    QuranTaskType.MEMORIZATION,
                    pageNumber
            ));
        }

        for (int pageNumber : selectRevisionPages(goal, byPage, usedPages)) {
            tasks.add(newTask(
                    userId,
                    goal.getId(),
                    taskDate,
                    QuranTaskType.REVISION,
                    pageNumber
            ));
        }

        return tasks;
    }

    List<Integer> selectMemorizationPages(
            QuranDailyGoal goal,
            Map<Integer, QuranPageProgress> byPage
    ) {
        int target = goal.getMemorizationPagesPerDay();
        if (target <= 0) {
            return List.of();
        }

        int startPage = goal.getPreferredStartPage() != null
                ? goal.getPreferredStartPage()
                : findFirstIncompletePage(byPage);

        List<Integer> selected = new ArrayList<>(target);
        for (int page = startPage; page <= QuranPageConstants.MAX_PAGE && selected.size() < target; page++) {
            QuranPageStatus status = statusOf(byPage.get(page));
            if (isCompletedMemorization(status)) {
                continue;
            }
            if (status == QuranPageStatus.NOT_STARTED || status == QuranPageStatus.LEARNING) {
                selected.add(page);
            }
        }
        return selected;
    }

    List<Integer> selectRevisionPages(
            QuranDailyGoal goal,
            Map<Integer, QuranPageProgress> byPage,
            Set<Integer> excludedPages
    ) {
        int target = goal.getRevisionPagesPerDay();
        if (target <= 0) {
            return List.of();
        }

        List<QuranPageProgress> candidates = byPage.values().stream()
                .filter(page -> isRevisionCandidate(page.getStatus()))
                .filter(page -> !excludedPages.contains(page.getPageNumber()))
                .sorted(revisionComparator())
                .toList();

        List<Integer> selected = new ArrayList<>(Math.min(target, candidates.size()));
        for (QuranPageProgress page : candidates) {
            if (selected.size() >= target) {
                break;
            }
            selected.add(page.getPageNumber());
        }
        return selected;
    }

    private int findFirstIncompletePage(Map<Integer, QuranPageProgress> byPage) {
        for (int page = QuranPageConstants.MIN_PAGE; page <= QuranPageConstants.MAX_PAGE; page++) {
            QuranPageStatus status = statusOf(byPage.get(page));
            if (status == QuranPageStatus.NOT_STARTED || status == QuranPageStatus.LEARNING) {
                return page;
            }
        }
        return QuranPageConstants.MAX_PAGE;
    }

    private Comparator<QuranPageProgress> revisionComparator() {
        return Comparator
                .comparingInt((QuranPageProgress page) -> revisionStatusRank(page.getStatus()))
                .thenComparing(page -> page.getLastReviewedAt() == null ? 0 : 1)
                .thenComparing(
                        QuranPageProgress::getLastReviewedAt,
                        Comparator.nullsFirst(Comparator.naturalOrder())
                )
                .thenComparingInt(QuranPageProgress::getPageNumber);
    }

    private int revisionStatusRank(QuranPageStatus status) {
        return switch (status) {
            case NEEDS_REVISION -> 0;
            case MEMORIZED -> 1;
            case STRONG -> 2;
            default -> 99;
        };
    }

    private boolean isCompletedMemorization(QuranPageStatus status) {
        return status == QuranPageStatus.MEMORIZED
                || status == QuranPageStatus.NEEDS_REVISION
                || status == QuranPageStatus.STRONG;
    }

    private boolean isRevisionCandidate(QuranPageStatus status) {
        return status == QuranPageStatus.NEEDS_REVISION
                || status == QuranPageStatus.MEMORIZED
                || status == QuranPageStatus.STRONG;
    }

    private QuranPageStatus statusOf(QuranPageProgress page) {
        return page == null ? QuranPageStatus.NOT_STARTED : page.getStatus();
    }

    private Map<Integer, QuranPageProgress> indexByPage(List<QuranPageProgress> storedPages) {
        Map<Integer, QuranPageProgress> byPage = new HashMap<>();
        for (QuranPageProgress page : storedPages) {
            byPage.put(page.getPageNumber(), page);
        }
        return byPage;
    }

    private QuranDailyTask newTask(
            UUID userId,
            UUID goalId,
            LocalDate taskDate,
            QuranTaskType type,
            int pageNumber
    ) {
        QuranDailyTask task = new QuranDailyTask();
        task.setUserId(userId);
        task.setGoalId(goalId);
        task.setTaskDate(taskDate);
        task.setTaskType(type);
        task.setPageNumber(pageNumber);
        task.setStatus(QuranTaskStatus.PENDING);
        return task;
    }
}
