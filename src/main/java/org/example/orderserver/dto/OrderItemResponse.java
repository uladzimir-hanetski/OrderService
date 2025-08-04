package org.example.orderserver.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class OrderItemResponse {
    private UUID id;
    private Long quantity;
    private UUID orderId;
    private UUID itemId;
}
