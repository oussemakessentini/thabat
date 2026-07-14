package com.thabat.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Generic event envelope for inter-service messaging.
 * Never include passwords, JWT tokens, or private user content in payloads.
 */
public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        String correlationId,
        String producer,
        T payload
) {
}
