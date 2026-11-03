package com.chubb.claims.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "kafka")
public class KafkaEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.messaging.kafka-topic-claims:chubb.apac.claims.events}")
    private String claimsTopic;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainEvents.ClaimEvent event) {
        log.info("[Apache Kafka] Publishing event {} to topic {} with key {}",
            event.eventType(), claimsTopic, event.claimNumber());
        try {
            kafkaTemplate.send(claimsTopic, event.claimNumber(), event);
        } catch (Exception e) {
            log.error("[Apache Kafka] Failed to publish event to Kafka cluster: {}", e.getMessage(), e);
        }
    }
}
