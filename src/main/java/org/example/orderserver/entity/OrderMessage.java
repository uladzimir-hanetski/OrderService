package org.example.orderserver.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class OrderMessage {
    private UUID orderId;
    private UUID userId;
    private BigDecimal paymentAmount;
}
