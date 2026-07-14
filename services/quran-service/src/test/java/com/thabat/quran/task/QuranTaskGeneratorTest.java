package com.thabat.quran.task;

import com.thabat.quran.goal.QuranDailyGoal;
import com.thabat.quran.page.QuranPageProgress;
import com.thabat.quran.page.QuranPageStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class QuranTaskGeneratorTest {

    private final QuranTaskGenerator generator = new QuranTaskGenerator();

    @Test
    void memorization_selectsNotStartedAndLearning_skipsCompleted() {
        QuranDailyGoal goal = goal(2, 0, null);
        Map<Integer, QuranPageProgress> byPage = Map.of(
                1, page(1, QuranPageStatus.MEMORIZED, null),
                2, page(2, QuranPageStatus.LEARNING, null),
                3, page(3, QuranPageStatus.STRONG, null)
        );

        assertThat(generator.selectMemorizationPages(goal, byPage))
                .containsExactly(2, 4);
    }

    @Test
    void memorization_usesPreferredStartPage() {
        QuranDailyGoal goal = goal(2, 0, 10);
        Map<Integer, QuranPageProgress> byPage = Map.of(
                10, page(10, QuranPageStatus.NOT_STARTED, null),
                11, page(11, QuranPageStatus.LEARNING, null)
        );

        assertThat(generator.selectMemorizationPages(goal, byPage))
                .containsExactly(10, 11);
    }

    @Test
    void revision_prioritizesNeedsRevision_thenNeverReviewed_thenOldest_thenMemorizedBeforeStrong() {
        QuranDailyGoal goal = goal(0, 4, null);
        Map<Integer, QuranPageProgress> byPage = new HashMap<>();
        byPage.put(5, page(5, QuranPageStatus.STRONG, LocalDate.of(2026, 1, 1)));
        byPage.put(4, page(4, QuranPageStatus.MEMORIZED, LocalDate.of(2026, 1, 2)));
        byPage.put(3, page(3, QuranPageStatus.MEMORIZED, null));
        byPage.put(2, page(2, QuranPageStatus.NEEDS_REVISION, LocalDate.of(2026, 2, 1)));
        byPage.put(1, page(1, QuranPageStatus.NEEDS_REVISION, null));

        assertThat(generator.selectRevisionPages(goal, byPage, Set.of()))
                .containsExactly(1, 2, 3, 4);
    }

    @Test
    void revision_excludesPagesAlreadySelectedForMemorization() {
        QuranDailyGoal goal = goal(0, 2, null);
        Map<Integer, QuranPageProgress> byPage = Map.of(
                1, page(1, QuranPageStatus.NEEDS_REVISION, null),
                2, page(2, QuranPageStatus.MEMORIZED, null)
        );

        assertThat(generator.selectRevisionPages(goal, byPage, Set.of(1)))
                .containsExactly(2);
    }

    @Test
    void generate_avoidsSamePageInBothTaskTypes() {
        QuranDailyGoal goal = goal(1, 2, 1);
        List<QuranPageProgress> pages = List.of(
                page(1, QuranPageStatus.LEARNING, null),
                page(2, QuranPageStatus.NEEDS_REVISION, null),
                page(3, QuranPageStatus.MEMORIZED, null)
        );

        List<QuranDailyTask> tasks = generator.generate(
                UUID.randomUUID(),
                goal,
                LocalDate.of(2026, 7, 13),
                pages
        );

        List<Integer> memoPages = tasks.stream()
                .filter(t -> t.getTaskType() == QuranTaskType.MEMORIZATION)
                .map(QuranDailyTask::getPageNumber)
                .toList();
        List<Integer> revPages = tasks.stream()
                .filter(t -> t.getTaskType() == QuranTaskType.REVISION)
                .map(QuranDailyTask::getPageNumber)
                .toList();

        assertThat(memoPages).containsExactly(1);
        assertThat(revPages).containsExactly(2, 3);
        assertThat(revPages).doesNotContainAnyElementsOf(memoPages);
    }

    private QuranDailyGoal goal(int mem, int rev, Integer preferred) {
        QuranDailyGoal goal = new QuranDailyGoal();
        goal.setId(UUID.randomUUID());
        goal.setUserId(UUID.randomUUID());
        goal.setMemorizationPagesPerDay(mem);
        goal.setRevisionPagesPerDay(rev);
        goal.setPreferredStartPage(preferred);
        goal.setActive(true);
        return goal;
    }

    private QuranPageProgress page(
            int pageNumber,
            QuranPageStatus status,
            LocalDate lastReviewedAt
    ) {
        QuranPageProgress page = new QuranPageProgress();
        page.setUserId(UUID.randomUUID());
        page.setPageNumber(pageNumber);
        page.setStatus(status);
        page.setLastReviewedAt(lastReviewedAt);
        page.setSuccessfulReviewCount(0);
        return page;
    }
}
