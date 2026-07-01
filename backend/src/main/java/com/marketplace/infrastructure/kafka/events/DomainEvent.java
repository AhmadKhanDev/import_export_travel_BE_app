package com.marketplace.infrastructure.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainEvent {

    @Builder.Default
    private UUID eventId = UUID.randomUUID();

    private String eventType;
    private String aggregateType;
    private UUID aggregateId;

    @Builder.Default
    private Instant occurredAt = Instant.now();

    /**
     * JSON-encoded payload specific to the event type.
     * Use a Map or dedicated event-specific DTO serialized via ObjectMapper.
     */
    private Object payload;
}
