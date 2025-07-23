package org.example.orderserver.controller;

import lombok.RequiredArgsConstructor;
import org.example.orderserver.dto.OrderRequest;
import org.example.orderserver.dto.OrderResponse;
import org.example.orderserver.entity.OrderStatus;
import org.example.orderserver.service.OrderService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;

@RequestMapping("/api/v1/orders")
@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Validated(OrderRequest.CreateValidation.class)
                                                         @RequestBody OrderRequest orderRequest,
                                                     @RequestHeader(HttpHeaders.AUTHORIZATION) String tokenHeader) {
        return ResponseEntity.ok(orderService.save(orderRequest, tokenHeader));
    }

    @GetMapping("/{id}/{email}")
    public ResponseEntity<OrderResponse> findOrderById(@PathVariable("id") UUID id, @PathVariable("email") String email,
                                                  @RequestHeader(HttpHeaders.AUTHORIZATION) String tokenHeader) {
        return ResponseEntity.ok(orderService.findById(id, email, tokenHeader));
    }

    @PostMapping("/ids")
    public ResponseEntity<List<OrderResponse>> findOrdersByIds(@RequestBody List<UUID> ids,
                                                               @RequestHeader(HttpHeaders.AUTHORIZATION)
                                                               String tokenHeader) {
        return ResponseEntity.ok(orderService.findByIds(ids, tokenHeader));
    }

    @PostMapping("/statuses")
    public ResponseEntity<List<OrderResponse>> findOrdersByStatuses(@RequestBody List<OrderStatus> statuses,
                                                                    @RequestHeader(HttpHeaders.AUTHORIZATION)
                                                                    String tokenHeader) {
        return ResponseEntity.ok(orderService.findByStatuses(statuses, tokenHeader));
    }

    @PutMapping("/{id}/{email}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable("id") UUID id, @PathVariable("email") String email,
                                                     @RequestHeader(HttpHeaders.AUTHORIZATION) String tokenHeader,
                                                     @Validated(OrderRequest.UpdateValidation.class)
                                                     @RequestBody OrderRequest orderRequest) {
        return ResponseEntity.ok(orderService.update(id, email, tokenHeader, orderRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrderResponse> deleteOrder(@PathVariable("id") UUID id) {
        orderService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
