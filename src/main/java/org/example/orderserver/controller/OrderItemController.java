package org.example.orderserver.controller;

import lombok.RequiredArgsConstructor;
import org.example.orderserver.dto.OrderItemRequest;
import org.example.orderserver.dto.OrderItemResponse;
import org.example.orderserver.service.OrderItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RequestMapping("/api/v1/order_items")
@RestController
@RequiredArgsConstructor
public class OrderItemController {
    private final OrderItemService orderItemService;

    @PostMapping("/order/{id}")
    public ResponseEntity<OrderItemResponse> saveOrderItem(@PathVariable("id") UUID orderId,
                                                           @Validated(OrderItemRequest.CreateValidation.class)
                                                           @RequestBody OrderItemRequest orderItemRequest) {
        return ResponseEntity.ok(orderItemService.save(orderId, orderItemRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderItemResponse> findOrderItemById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(orderItemService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderItemResponse> updateOrderItem(@PathVariable("id") UUID id,
                                                             @Validated(OrderItemRequest.UpdateValidation.class)
                                                             @RequestBody OrderItemRequest orderItemRequest) {
        return ResponseEntity.ok(orderItemService.update(id, orderItemRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderItem(@PathVariable("id") UUID id) {
        orderItemService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
