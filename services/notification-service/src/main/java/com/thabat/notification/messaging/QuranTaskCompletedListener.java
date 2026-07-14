package com.thabat.notification.messaging;

import com.thabat.events.quran.QuranTaskCompletedEvent;
import com.thabat.notification.domain.Notification;
import com.thabat.notification.domain.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class QuranTaskCompletedListener {

    private static final Logger log = LoggerFactory.getLogger(QuranTaskCompletedListener.class);

    private final NotificationService notificationService;

    @KafkaListener(
            topics = QuranKafkaTopics.QURAN_EVENTS_V1,
            groupId = "notification-service-quran-v1",
            containerFactory = "quranEventKafkaListenerContainerFactory"
    )
    public void onMessage(QuranTaskCompletedEnvelope envelope) {
        if (envelope == null || envelope.eventType() == null) {
            return;
        }

        if (!QuranTaskCompletedEvent.EVENT_TYPE.equals(envelope.eventType())) {
            log.debug(
                    "Ignoring eventId={} eventType={}",
                    envelope.eventId(),
                    envelope.eventType()
            );
            return;
        }

        Optional<Notification> created = notificationService.createFromQuranTaskCompleted(envelope);
        log.info(
                "Processed eventId={} result={}",
                envelope.eventId(),
                created.isPresent() ? "created" : "already-processed"
        );
    }
}
