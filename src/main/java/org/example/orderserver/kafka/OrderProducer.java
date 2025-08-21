package org.example.orderserver.kafka;

import lombok.extern.slf4j.Slf4j;
import org.example.orderserver.entity.OrderMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderProducer {
    private final String ordersTopic;
    private final KafkaTemplate<String, OrderMessage> kafkaTemplate;

    public OrderProducer(KafkaTemplate<String, OrderMessage> kafkaTemplate,
                         @Value("${ORDERS_TOPIC}") String ordersTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.ordersTopic = ordersTopic;
    }

    public void sendOrderMessage(OrderMessage orderMessage) {
        kafkaTemplate.send(ordersTopic, orderMessage)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send order message with id {} to Kafka. Reason: {}",
                                orderMessage.getOrderId(), ex.getMessage());
                    } else {
                        log.info("Order with id {} sent to Kafka successfully.", orderMessage.getOrderId());
                    }
                });
    }
}
