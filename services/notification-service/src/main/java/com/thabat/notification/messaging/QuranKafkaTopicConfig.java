package com.thabat.notification.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile("!test")
public class QuranKafkaTopicConfig {

    @Bean
    public NewTopic quranEventsTopic() {
        return TopicBuilder.name(QuranKafkaTopics.QURAN_EVENTS_V1)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic quranEventsDltTopic() {
        return TopicBuilder.name(QuranKafkaTopics.QURAN_EVENTS_V1_DLT)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
