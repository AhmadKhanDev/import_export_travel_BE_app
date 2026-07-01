package com.marketplace.infrastructure.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name("marketplace.user.events").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic kycEventsTopic() {
        return TopicBuilder.name("marketplace.kyc.events").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic offerEventsTopic() {
        return TopicBuilder.name("marketplace.offer.events").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic bookingEventsTopic() {
        return TopicBuilder.name("marketplace.booking.events").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name("marketplace.payment.events").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic deliveryEventsTopic() {
        return TopicBuilder.name("marketplace.delivery.events").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic notificationEventsTopic() {
        return TopicBuilder.name("marketplace.notification.events").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic disputeEventsTopic() {
        return TopicBuilder.name("marketplace.dispute.events").partitions(1).replicas(1).build();
    }
}
