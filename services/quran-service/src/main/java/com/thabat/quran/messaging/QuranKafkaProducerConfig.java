package com.thabat.quran.messaging;

import com.thabat.events.EventEnvelope;
import com.thabat.events.quran.QuranTaskCompletedEvent;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("!test")
public class QuranKafkaProducerConfig {

    @Bean
    public ProducerFactory<String, EventEnvelope<QuranTaskCompletedEvent>> quranEventProducerFactory(
            KafkaProperties kafkaProperties
    ) {
        Map<String, Object> configs = new HashMap<>(kafkaProperties.buildProducerProperties());
        return new DefaultKafkaProducerFactory<>(
                configs,
                new StringSerializer(),
                new JsonSerializer<>()
        );
    }

    @Bean
    public KafkaTemplate<String, EventEnvelope<QuranTaskCompletedEvent>> quranEventKafkaTemplate(
            ProducerFactory<String, EventEnvelope<QuranTaskCompletedEvent>> quranEventProducerFactory
    ) {
        return new KafkaTemplate<>(quranEventProducerFactory);
    }
}
