package com.thabat.notification.domain;

import com.thabat.events.quran.QuranTaskCompletedEvent;
import com.thabat.notification.common.exception.ResourceNotFoundException;
import com.thabat.notification.dto.NotificationResponse;
import com.thabat.notification.dto.UnreadCountResponse;
import com.thabat.notification.messaging.QuranTaskCompletedEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public Page<NotificationResponse> list(UUID userId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(safePage, safeSize))
                .map(NotificationResponse::from);
    }

    @Transactional
    public NotificationResponse markRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository
                .findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(Instant.now(clock));
        }

        return NotificationResponse.from(notification);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        notificationRepository.markAllReadForUser(userId, Instant.now(clock));
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse unreadCount(UUID userId) {
        return new UnreadCountResponse(
                notificationRepository.countByUserIdAndReadFalse(userId)
        );
    }

    /**
     * Creates a notification from a Quran task completed event.
     * Idempotent on {@code eventId}: concurrent duplicates are treated as already processed.
     *
     * @return empty if the event was already handled
     */
    @Transactional
    public Optional<Notification> createFromQuranTaskCompleted(QuranTaskCompletedEnvelope envelope) {
        if (envelope == null || envelope.payload() == null) {
            return Optional.empty();
        }

        if (notificationRepository.existsByEventId(envelope.eventId())) {
            return Optional.empty();
        }

        QuranTaskCompletedEvent payload = envelope.payload();
        Notification notification = Notification.builder()
                .userId(payload.userId())
                .eventId(envelope.eventId())
                .type(NotificationType.QURAN_TASK_COMPLETED)
                .title("Quran task completed")
                .message(
                        "You completed page " + payload.pageNumber()
                                + ". Keep going at your own pace."
                )
                .read(false)
                .build();

        try {
            return Optional.of(notificationRepository.saveAndFlush(notification));
        } catch (DataIntegrityViolationException exception) {
            return Optional.empty();
        }
    }
}
