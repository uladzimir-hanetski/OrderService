package org.example.orderserver.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class TopicsConfig {
    private final String ordersTopic;
    private final String deadPaymentsTopic;

    public TopicsConfig(@Value("${ORDERS_TOPIC}") String ordersTopic,
                        @Value("${DEAD_PAYMENTS_TOPIC}") String deadPaymentsTopic) {
        this.ordersTopic = ordersTopic;
        this.deadPaymentsTopic = deadPaymentsTopic;
    }

    @Bean
    public NewTopic createOrdersTopic() {
        return TopicBuilder.name(ordersTopic).build();
    }

    @Bean
    public NewTopic createDeadPaymentsTopic() {
        return TopicBuilder.name(deadPaymentsTopic).build();
    }
}
