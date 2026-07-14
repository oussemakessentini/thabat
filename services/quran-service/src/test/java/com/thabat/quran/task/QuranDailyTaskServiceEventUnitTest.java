package com.thabat.quran.task;

import com.thabat.quran.common.exception.InvalidQuranProgressException;
import com.thabat.quran.goal.QuranDailyGoalService;
import com.thabat.quran.messaging.QuranTaskCompletedDomainEvent;
import com.thabat.quran.page.QuranPageProgressRepository;
import com.thabat.quran.task.dto.CompleteQuranTaskRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuranDailyTaskServiceEventUnitTest {

    @Mock
    private QuranDailyTaskRepository taskRepository;
    @Mock
    private QuranPageProgressRepository pageRepository;
    @Mock
    private QuranDailyGoalService goalService;
    @Mock
    private QuranTaskGenerator taskGenerator;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private Clock clock;

    @InjectMocks
    private QuranDailyTaskService service;

    @Test
    void failedValidation_doesNotPublishEvent() {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        QuranDailyTask task = new QuranDailyTask();
        task.setId(taskId);
        task.setUserId(userId);
        task.setStatus(QuranTaskStatus.COMPLETED);
        task.setTaskDate(LocalDate.of(2026, 7, 14));
        task.setTaskType(QuranTaskType.MEMORIZATION);
        task.setPageNumber(1);

        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(Instant.parse("2026-07-14T12:00:00Z"));
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() ->
                service.completeTask(userId, taskId, new CompleteQuranTaskRequest(4, true))
        ).isInstanceOf(InvalidQuranProgressException.class);

        verify(eventPublisher, never()).publishEvent(any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void successfulComplete_publishesDomainEvent() {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        QuranDailyTask task = new QuranDailyTask();
        task.setId(taskId);
        task.setUserId(userId);
        task.setStatus(QuranTaskStatus.PENDING);
        task.setTaskDate(LocalDate.of(2026, 7, 14));
        task.setTaskType(QuranTaskType.MEMORIZATION);
        task.setPageNumber(12);
        task.setGoalId(UUID.randomUUID());

        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(Instant.parse("2026-07-14T12:00:00Z"));
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(task));
        when(pageRepository.findByUserIdAndPageNumber(userId, 12)).thenReturn(Optional.empty());
        when(pageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.findByUserIdAndTaskDateOrderByTaskTypeAscPageNumberAsc(any(), any()))
                .thenReturn(java.util.List.of());

        service.completeTask(userId, taskId, new CompleteQuranTaskRequest(5, true));

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(QuranTaskCompletedDomainEvent.class);
        QuranTaskCompletedDomainEvent event =
                (QuranTaskCompletedDomainEvent) captor.getValue();
        assertThat(event.pageNumber()).isEqualTo(12);
        assertThat(event.userId()).isEqualTo(userId);
    }
}
