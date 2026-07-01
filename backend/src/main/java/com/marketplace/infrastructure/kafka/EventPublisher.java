package com.marketplace.infrastructure.kafka;

import com.marketplace.infrastructure.kafka.events.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.enabled:true}")
    private boolean kafkaEnabled;

    /**
     * Publish a domain event to the specified Kafka topic.
     * If Kafka is disabled via config, logs the event and skips publishing.
     */
    public void publish(String topic, DomainEvent event) {
        if (!kafkaEnabled) {
            log.info("[KAFKA DISABLED] Would publish: topic={}, eventType={}, aggregateId={}",
                    topic, event.getEventType(), event.getAggregateId());
            return;
        }

        try {
            kafkaTemplate.send(topic, event.getAggregateId().toString(), event);
            log.debug("Event published: topic={}, eventType={}, eventId={}, aggregateId={}",
                    topic, event.getEventType(), event.getEventId(), event.getAggregateId());
        } catch (Exception e) {
            // Do not let Kafka failure break the main business flow
            log.error("Failed to publish event to Kafka: topic={}, eventType={}, error={}",
                    topic, event.getEventType(), e.getMessage(), e);
        }
    }

    /**
     * Convenience builder for creating and publishing a domain event in one call.
     */
    public void publishEvent(String topic, String eventType, String aggregateType, UUID aggregateId, Object payload) {
        DomainEvent event = DomainEvent.builder()
                .eventType(eventType)
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .payload(payload)
                .build();
        publish(topic, event);
    }
}
