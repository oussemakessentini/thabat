package com.thabat.notification.messaging;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class NotificationKafkaConfig {

    @Bean
    public ConsumerFactory<String, QuranTaskCompletedEnvelope> quranEventConsumerFactory(
            KafkaProperties kafkaProperties
    ) {
        Map<String, Object> configs = new HashMap<>(kafkaProperties.buildConsumerProperties());
        // Avoid mixing JsonDeserializer setters with spring.json.* consumer properties.
        configs.keySet().removeIf(key ->
                key.startsWith("spring.json")
                        || ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG.equals(key)
                        || ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG.equals(key));

        JsonDeserializer<QuranTaskCompletedEnvelope> deserializer =
                new JsonDeserializer<>(QuranTaskCompletedEnvelope.class, false);
        deserializer.addTrustedPackages(
                "com.thabat.events",
                "com.thabat.events.quran",
                "com.thabat.notification.messaging"
        );

        return new DefaultKafkaConsumerFactory<>(
                configs,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ProducerFactory<String, Object> dltProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> configs = new HashMap<>(kafkaProperties.buildProducerProperties());
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(
                configs,
                new StringSerializer(),
                new JsonSerializer<>()
        );
    }

    @Bean
    public KafkaTemplate<String, Object> dltKafkaTemplate(
            ProducerFactory<String, Object> dltProducerFactory
    ) {
        return new KafkaTemplate<>(dltProducerFactory);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, QuranTaskCompletedEnvelope>
    quranEventKafkaListenerContainerFactory(
            ConsumerFactory<String, QuranTaskCompletedEnvelope> quranEventConsumerFactory,
            KafkaTemplate<String, Object> dltKafkaTemplate,
            @Value("${spring.kafka.listener.auto-startup:true}") boolean autoStartup
    ) {
        ConcurrentKafkaListenerContainerFactory<String, QuranTaskCompletedEnvelope> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(quranEventConsumerFactory);
        factory.setAutoStartup(autoStartup);

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                dltKafkaTemplate,
                (record, exception) -> new TopicPartition(
                        QuranKafkaTopics.QURAN_EVENTS_V1_DLT,
                        record.partition()
                )
        );
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(1_000L, 3L)
        );
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
