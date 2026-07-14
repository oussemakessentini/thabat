package com.thabat.notification;

import com.thabat.events.quran.QuranTaskCompletedEvent;
import com.thabat.notification.domain.NotificationRepository;
import com.thabat.notification.domain.NotificationService;
import com.thabat.notification.messaging.QuranTaskCompletedEnvelope;
import com.thabat.notification.messaging.QuranTaskCompletedListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class QuranTaskCompletedListenerTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private QuranTaskCompletedListener listener;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
    }

    @Test
    void createTwiceWithSameEventId_persistsOneNotification() {
        UUID eventId = UUID.randomUUID();
        QuranTaskCompletedEnvelope envelope = envelope(eventId, 42);

        assertThat(notificationService.createFromQuranTaskCompleted(envelope)).isPresent();
        assertThat(notificationService.createFromQuranTaskCompleted(envelope)).isEmpty();

        assertThat(notificationRepository.count()).isEqualTo(1);
        assertThat(notificationRepository.existsByEventId(eventId)).isTrue();
    }

    @Test
    void listener_ignoresUnknownEventType() {
        QuranTaskCompletedEnvelope envelope = new QuranTaskCompletedEnvelope(
                UUID.randomUUID(),
                "SomeOtherEvent",
                1,
                Instant.now(),
                null,
                "quran-service",
                new QuranTaskCompletedEvent(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        3,
                        "MEMORIZATION",
                        Instant.now()
                )
        );

        listener.onMessage(envelope);

        assertThat(notificationRepository.count()).isZero();
    }

    @Test
    void listener_createsNotificationForQuranTaskCompleted() {
        UUID eventId = UUID.randomUUID();
        listener.onMessage(envelope(eventId, 7));
        listener.onMessage(envelope(eventId, 7));

        assertThat(notificationRepository.count()).isEqualTo(1);
    }

    private static QuranTaskCompletedEnvelope envelope(UUID eventId, int pageNumber) {
        return new QuranTaskCompletedEnvelope(
                eventId,
                QuranTaskCompletedEvent.EVENT_TYPE,
                QuranTaskCompletedEvent.EVENT_VERSION,
                Instant.now(),
                null,
                "quran-service",
                new QuranTaskCompletedEvent(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        pageNumber,
                        "MEMORIZATION",
                        Instant.now()
                )
        );
    }
}
