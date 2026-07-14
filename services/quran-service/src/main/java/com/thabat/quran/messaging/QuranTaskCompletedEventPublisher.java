package com.thabat.quran.messaging;

import com.thabat.events.EventEnvelope;
import com.thabat.events.quran.QuranTaskCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

/**
 * Publishes {@link QuranTaskCompletedEvent} to Kafka only after the DB transaction commits.
 *
 * <p><b>Not a transactional outbox:</b> AFTER_COMMIT publication can still lose events if the
 * JVM crashes between commit and Kafka acknowledgement. Replace with an outbox table later
 * for stronger delivery guarantees.
 */
@Component
public class QuranTaskCompletedEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(QuranTaskCompletedEventPublisher.class);

    private final ObjectProvider<KafkaTemplate<String, EventEnvelope<QuranTaskCompletedEvent>>> kafkaTemplateProvider;

    public QuranTaskCompletedEventPublisher(
            ObjectProvider<KafkaTemplate<String, EventEnvelope<QuranTaskCompletedEvent>>> kafkaTemplateProvider
    ) {
        this.kafkaTemplateProvider = kafkaTemplateProvider;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskCompleted(QuranTaskCompletedDomainEvent domainEvent) {
        KafkaTemplate<String, EventEnvelope<QuranTaskCompletedEvent>> kafkaTemplate =
                kafkaTemplateProvider.getIfAvailable();
        if (kafkaTemplate == null) {
            log.debug(
                    "KafkaTemplate unavailable; skipping publish for eventId={} pageNumber={}",
                    domainEvent.eventId(),
                    domainEvent.pageNumber()
            );
            return;
        }

        EventEnvelope<QuranTaskCompletedEvent> envelope = new EventEnvelope<>(
                domainEvent.eventId(),
                QuranTaskCompletedEvent.EVENT_TYPE,
                QuranTaskCompletedEvent.EVENT_VERSION,
                Instant.now(),
                null,
                "quran-service",
                new QuranTaskCompletedEvent(
                        domainEvent.userId(),
                        domainEvent.taskId(),
                        domainEvent.pageNumber(),
                        domainEvent.taskType(),
                        domainEvent.completedAt()
                )
        );

        // Async send — do not block the HTTP thread waiting indefinitely
        kafkaTemplate.send(
                        QuranKafkaTopics.QURAN_EVENTS_V1,
                        domainEvent.userId().toString(),
                        envelope
                )
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error(
                                "Failed to publish eventId={} eventType={} pageNumber={}",
                                envelope.eventId(),
                                envelope.eventType(),
                                domainEvent.pageNumber(),
                                error
                        );
                        return;
                    }
                    log.info(
                            "Published eventId={} eventType={} pageNumber={}",
                            envelope.eventId(),
                            envelope.eventType(),
                            domainEvent.pageNumber()
                    );
                });
    }
}
