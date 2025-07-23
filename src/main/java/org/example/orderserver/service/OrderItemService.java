package org.example.orderserver.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.orderserver.dto.OrderItemRequest;
import org.example.orderserver.dto.OrderItemResponse;
import org.example.orderserver.entity.Item;
import org.example.orderserver.entity.Order;
import org.example.orderserver.entity.OrderItem;
import org.example.orderserver.exception.ItemNotFoundException;
import org.example.orderserver.exception.OrderItemNotFoundException;
import org.example.orderserver.exception.OrderNotFoundException;
import org.example.orderserver.mapper.OrderItemMapper;
import org.example.orderserver.repository.ItemRepository;
import org.example.orderserver.repository.OrderItemRepository;
import org.example.orderserver.repository.OrderRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderItemService {
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemMapper mapper;

    public OrderItemResponse save(UUID orderId, OrderItemRequest orderItemRequest) {
        Item item = itemRepository.findById(orderItemRequest.getItemId())
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        OrderItem orderItem = mapper.toEntity(orderItemRequest);
        orderItem.setItem(item);

        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new OrderNotFoundException("Order not found"));
        order.getOrderItems().add(orderItem);
        orderItem.setOrder(order);

        return mapper.toResponse(orderItemRepository.save(orderItem));
    }

    public OrderItemResponse findById(UUID id) {
        return mapper.toResponse(orderItemRepository.findById(id).orElseThrow(
                () -> new OrderItemNotFoundException("Order item not found")));
    }

    @Transactional
    public OrderItemResponse update(UUID id, OrderItemRequest orderItemRequest) {
        OrderItem orderItem = orderItemRepository.findById(id).orElseThrow(
                () -> new OrderItemNotFoundException("Order item not found"));

        if (orderItemRequest.getQuantity() != null) {
            orderItem.setQuantity(orderItemRequest.getQuantity());
        }

        return mapper.toResponse(orderItemRepository.save(orderItem));
    }

    @Transactional
    public void delete(UUID id) {
        if (!orderItemRepository.existsById(id)) {
            throw new OrderItemNotFoundException("Order item not found");
        }

        orderItemRepository.deleteById(id);
    }
}
