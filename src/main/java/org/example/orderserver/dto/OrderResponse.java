package org.example.orderserver.dto;

import lombok.Data;
import org.example.orderserver.entity.UserInfo;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class OrderResponse {
    private UUID id;
    private String status;
    private LocalDate creationDate;
    private List<OrderItemResponse> orderItems;

    private UserInfo userInfo;
}
