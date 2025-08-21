package org.example.orderserver.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderserver.entity.PaymentMessage;
import org.example.orderserver.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConsumer {
    private final OrderService orderService;

    @KafkaListener(topics = "payments", groupId = "payments1")
    public void consumePaymentMessage(PaymentMessage paymentMessage) {
        switch (paymentMessage.getPaymentStatus()) {
            case "CREATED" -> orderService.addPaymentId(paymentMessage);
            case "SUCCESS" -> orderService.processSuccessPayment(paymentMessage);
            default -> log.warn("There is no processing for such payment status: {}",
                    paymentMessage.getPaymentStatus());
        }
    }
}
