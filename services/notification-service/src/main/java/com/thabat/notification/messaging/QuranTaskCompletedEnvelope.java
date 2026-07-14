package com.thabat.notification.messaging;

import com.thabat.events.quran.QuranTaskCompletedEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Concrete Kafka payload wrapper for {@code EventEnvelope&lt;QuranTaskCompletedEvent&gt;}
 * so JsonDeserializer can bind without type headers.
 */
public record QuranTaskCompletedEnvelope(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        String correlationId,
        String producer,
        QuranTaskCompletedEvent payload
) {
}
