package org.example.orderserver.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentMessage {
    private String paymentId;
    private String orderId;
    private String paymentStatus;
}
