package org.example.orderserver.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemMapper mapper;

    @InjectMocks
    private OrderItemService orderItemService;

    private final OrderItemRequest orderItemRequest = new OrderItemRequest();
    private final OrderItem orderItem = new OrderItem();
    private final OrderItemResponse orderItemResponse = new OrderItemResponse();
    private final UUID orderItemId = UUID.randomUUID();
    private final UUID itemId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final Item item = new Item();
    private final Order order = new Order();

    @BeforeEach
    void initialize() {
        item.setId(itemId);
        item.setName("Test");
        item.setPrice(1.1f);

        order.setId(orderId);
        order.setOrderItems(new ArrayList<>());

        orderItemRequest.setItemId(itemId);
        orderItemRequest.setQuantity(10L);

        orderItem.setId(orderItemId);
        orderItem.setItem(item);
        orderItem.setQuantity(10L);
        orderItem.setOrder(order);
    }

    @Test
    void testSave() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(mapper.toEntity(orderItemRequest)).thenReturn(orderItem);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.save(orderItem)).thenReturn(orderItem);
        when(mapper.toResponse(orderItem)).thenReturn(orderItemResponse);

        OrderItemResponse response = orderItemService.save(orderId, orderItemRequest);

        assertThat(response).isEqualTo(orderItemResponse);
    }

    @Test
    void testSaveItemNotFound() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> orderItemService.save(orderId, orderItemRequest));
    }

    @Test
    void testSaveOrderNotFound() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(mapper.toEntity(orderItemRequest)).thenReturn(orderItem);
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderItemService.save(orderId, orderItemRequest));
    }

    @Test
    void testFindById() {
        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(orderItem));
        when(mapper.toResponse(orderItem)).thenReturn(orderItemResponse);

        OrderItemResponse response = orderItemService.findById(orderItemId);

        assertThat(response).isEqualTo(orderItemResponse);
    }

    @Test
    void testFindByIdNotFound() {
        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.empty());

        assertThrows(OrderItemNotFoundException.class, () -> orderItemService.findById(orderItemId));
    }

    @Test
    void testUpdate() {
        OrderItemRequest updatedOrderItemRequest = new OrderItemRequest();
        updatedOrderItemRequest.setQuantity(1000L);

        OrderItem updatedOrderItem = new OrderItem();
        updatedOrderItem.setId(orderItemId);
        updatedOrderItem.setQuantity(1000L);
        updatedOrderItem.setItem(item);
        updatedOrderItem.setOrder(order);

        OrderItemResponse updatedOrderItemResponse = new OrderItemResponse();
        updatedOrderItemResponse.setId(orderItemId);
        updatedOrderItemResponse.setQuantity(1000L);
        updatedOrderItem.setItem(item);
        updatedOrderItem.setOrder(order);

        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(orderItem));
        when(orderItemRepository.save(updatedOrderItem)).thenReturn(updatedOrderItem);
        when(mapper.toResponse(updatedOrderItem)).thenReturn(updatedOrderItemResponse);

        OrderItemResponse response = orderItemService.update(orderItemId, updatedOrderItemRequest);

        assertThat(response).isEqualTo(updatedOrderItemResponse);
    }

    @Test
    void testUpdateOrderItemNotFound() {
        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.empty());

        assertThrows(OrderItemNotFoundException.class, () -> orderItemService.update(orderItemId, orderItemRequest));
    }

    @Test
    void testDelete() {
        when(orderItemRepository.existsById(orderItemId)).thenReturn(true);

        orderItemService.delete(orderItemId);

        verify(orderItemRepository).deleteById(orderItemId);
    }

    @Test
    void testDeleteNotFound() {
        when(orderItemRepository.existsById(orderItemId)).thenReturn(false);

        assertThrows(OrderItemNotFoundException.class, () -> orderItemService.delete(orderItemId));
    }
}
