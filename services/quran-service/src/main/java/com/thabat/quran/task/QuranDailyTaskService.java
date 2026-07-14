package com.thabat.quran.task;

import com.thabat.quran.common.exception.InvalidQuranProgressException;
import com.thabat.quran.common.exception.ResourceNotFoundException;
import com.thabat.quran.goal.QuranDailyGoal;
import com.thabat.quran.goal.QuranDailyGoalService;
import com.thabat.quran.messaging.QuranTaskCompletedDomainEvent;
import com.thabat.quran.page.QuranPageProgress;
import com.thabat.quran.page.QuranPageProgressRepository;
import com.thabat.quran.page.QuranPageStatus;
import com.thabat.quran.task.dto.CompleteQuranTaskRequest;
import com.thabat.quran.task.dto.QuranDailyTaskResponse;
import com.thabat.quran.task.dto.QuranTaskMutationResponse;
import com.thabat.quran.task.dto.QuranTodayTasksResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuranDailyTaskService {

    private final QuranDailyTaskRepository taskRepository;
    private final QuranPageProgressRepository pageRepository;
    private final QuranDailyGoalService goalService;
    private final QuranTaskGenerator taskGenerator;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional
    public QuranTodayTasksResponse getTodayTasks(UUID userId) {
        LocalDate today = LocalDate.now(clock);
        QuranDailyGoal goal = goalService.requireActiveGoal(userId);

        if (!taskRepository.existsByUserIdAndTaskDate(userId, today)) {
            List<QuranPageProgress> pages =
                    pageRepository.findByUserIdOrderByPageNumberAsc(userId);
            List<QuranDailyTask> generated =
                    taskGenerator.generate(userId, goal, today, pages);
            if (!generated.isEmpty()) {
                taskRepository.saveAll(generated);
            }
        }

        return toTodayResponse(userId, today);
    }

    @Transactional
    public QuranTaskMutationResponse completeTask(
            UUID userId,
            UUID taskId,
            CompleteQuranTaskRequest request
    ) {
        LocalDate today = LocalDate.now(clock);
        QuranDailyTask task = requireOwnedTask(userId, taskId);

        if (task.getTaskDate().isAfter(today)) {
            throw new InvalidQuranProgressException("taskDate cannot be in the future");
        }
        if (task.getStatus() != QuranTaskStatus.PENDING) {
            throw new InvalidQuranProgressException("Only PENDING tasks can be completed");
        }

        applyPageProgressOnComplete(userId, task, request, today);

        Instant completedAt = Instant.now(clock);
        task.setStatus(QuranTaskStatus.COMPLETED);
        task.setCompletedAt(completedAt);
        QuranDailyTask saved = taskRepository.save(task);

        // Publish only after a successful PENDING -> COMPLETED transition in this transaction.
        // Kafka send runs AFTER_COMMIT so a rolled-back transaction publishes nothing.
        eventPublisher.publishEvent(new QuranTaskCompletedDomainEvent(
                UUID.randomUUID(),
                userId,
                saved.getId(),
                saved.getPageNumber(),
                saved.getTaskType().name(),
                completedAt
        ));

        return new QuranTaskMutationResponse(
                toTaskResponse(saved),
                toTodayResponse(userId, task.getTaskDate())
        );
    }

    @Transactional
    public QuranTaskMutationResponse skipTask(UUID userId, UUID taskId) {
        QuranDailyTask task = requireOwnedTask(userId, taskId);

        if (task.getStatus() != QuranTaskStatus.PENDING) {
            throw new InvalidQuranProgressException("Only PENDING tasks can be skipped");
        }

        task.setStatus(QuranTaskStatus.SKIPPED);
        QuranDailyTask saved = taskRepository.save(task);

        return new QuranTaskMutationResponse(
                toTaskResponse(saved),
                toTodayResponse(userId, task.getTaskDate())
        );
    }

    private void applyPageProgressOnComplete(
            UUID userId,
            QuranDailyTask task,
            CompleteQuranTaskRequest request,
            LocalDate today
    ) {
        QuranPageProgress page = pageRepository
                .findByUserIdAndPageNumber(userId, task.getPageNumber())
                .orElseGet(() -> newPage(userId, task.getPageNumber()));

        if (request.confidenceLevel() != null) {
            page.setConfidenceLevel(request.confidenceLevel());
        }

        if (task.getTaskType() == QuranTaskType.MEMORIZATION) {
            page.setStatus(QuranPageStatus.MEMORIZED);
            if (page.getMemorizedAt() == null) {
                page.setMemorizedAt(today);
            }
        } else {
            page.setLastReviewedAt(today);
            boolean successful = Boolean.TRUE.equals(request.successful());
            if (successful) {
                page.setSuccessfulReviewCount(page.getSuccessfulReviewCount() + 1);
            } else {
                page.setStatus(QuranPageStatus.NEEDS_REVISION);
            }
        }

        pageRepository.save(page);
    }

    private QuranDailyTask requireOwnedTask(UUID userId, UUID taskId) {
        return taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Quran daily task not found"));
    }

    private QuranTodayTasksResponse toTodayResponse(UUID userId, LocalDate date) {
        List<QuranDailyTask> tasks =
                taskRepository.findByUserIdAndTaskDateOrderByTaskTypeAscPageNumberAsc(
                        userId,
                        date
                );

        List<QuranDailyTaskResponse> memorization = new ArrayList<>();
        List<QuranDailyTaskResponse> revision = new ArrayList<>();
        int completed = 0;
        int pending = 0;
        int skipped = 0;

        for (QuranDailyTask task : tasks) {
            QuranDailyTaskResponse response = toTaskResponse(task);
            if (task.getTaskType() == QuranTaskType.MEMORIZATION) {
                memorization.add(response);
            } else {
                revision.add(response);
            }

            switch (task.getStatus()) {
                case COMPLETED -> completed++;
                case PENDING -> pending++;
                case SKIPPED -> skipped++;
            }
        }

        int total = tasks.size();
        BigDecimal percentage = total == 0
                ? BigDecimal.ZERO.setScale(2)
                : BigDecimal.valueOf(completed)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

        return new QuranTodayTasksResponse(
                date,
                memorization,
                revision,
                completed,
                pending,
                skipped,
                total,
                percentage
        );
    }

    private QuranDailyTaskResponse toTaskResponse(QuranDailyTask task) {
        return new QuranDailyTaskResponse(
                task.getId(),
                task.getPageNumber(),
                task.getTaskType(),
                task.getStatus()
        );
    }

    private QuranPageProgress newPage(UUID userId, int pageNumber) {
        QuranPageProgress page = new QuranPageProgress();
        page.setUserId(userId);
        page.setPageNumber(pageNumber);
        page.setStatus(QuranPageStatus.NOT_STARTED);
        page.setSuccessfulReviewCount(0);
        return page;
    }
}
