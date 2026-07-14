package com.thabat.quran.messaging;

import java.time.Instant;
import java.util.UUID;

/**
 * In-process domain event published inside the completing transaction.
 * Kafka publication happens AFTER_COMMIT in {@link QuranTaskCompletedEventPublisher}.
 *
 * <p>Note: AFTER_COMMIT is not a transactional outbox. If the process dies after commit
 * but before Kafka send, the event can be lost. A transactional outbox will replace this later.
 */
public record QuranTaskCompletedDomainEvent(
        UUID eventId,
        UUID userId,
        UUID taskId,
        int pageNumber,
        String taskType,
        Instant completedAt
) {
}
