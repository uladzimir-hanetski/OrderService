package org.example.orderserver.service;

import org.example.orderserver.dto.ItemRequest;
import org.example.orderserver.dto.ItemResponse;
import org.example.orderserver.entity.Item;
import org.example.orderserver.entity.OrderItem;
import org.example.orderserver.exception.ItemNotFoundException;
import org.example.orderserver.mapper.ItemMapper;
import org.example.orderserver.repository.ItemRepository;
import org.example.orderserver.repository.OrderItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ItemMapper mapper;

    @InjectMocks
    private ItemService itemService;

    private final ItemRequest itemRequest = new ItemRequest();
    private final Item item = new Item();
    private final ItemResponse itemResponse = new ItemResponse();
    private final UUID id = UUID.randomUUID();

    @BeforeEach
    void initialize() {
        itemRequest.setName("Test");
        itemRequest.setPrice(1.1f);

        item.setId(id);
        item.setName("Test");
        item.setPrice(1.1f);

        itemResponse.setId(id);
        itemResponse.setName("Test");
        itemResponse.setPrice(1.1f);
    }

    @Test
    void testSaveItem() {
        when(mapper.toEntity(itemRequest)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(mapper.toResponse(item)).thenReturn(itemResponse);

        ItemResponse response = itemService.save(itemRequest);

        assertThat(response).isEqualTo(itemResponse);
    }

    @Test
    void testFindById() {
        when(mapper.toResponse(item)).thenReturn(itemResponse);
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));

        ItemResponse response = itemService.findById(id);

        assertThat(response).isEqualTo(itemResponse);
    }

    @Test
    void testFindByIdNotFound() {
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.findById(id));
    }

    @Test
    void testUpdate() {
        ItemRequest request = new ItemRequest();
        request.setName("Test");
        request.setPrice(10.1f);

        Item updatedItem = new Item();
        updatedItem.setId(id);
        updatedItem.setName("Test");
        updatedItem.setPrice(10.1f);

        ItemResponse updatedItemResponse = new ItemResponse();
        updatedItemResponse.setId(id);
        updatedItemResponse.setName("Test");
        updatedItemResponse.setPrice(10.1f);

        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(itemRepository.save(updatedItem)).thenReturn(updatedItem);
        when(mapper.toResponse(any())).thenReturn(updatedItemResponse);

        ItemResponse response = itemService.update(id, request);

        assertThat(response.getPrice()).isEqualTo(request.getPrice());
    }

    @Test
    void testUpdateNotFound() {
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.update(id, itemRequest));
    }

    @Test
    void testDelete() {
        when(itemRepository.existsById(id)).thenReturn(true);
        when(orderItemRepository.findByItemId(id)).thenReturn(Collections.emptyList());

        itemService.delete(id);

        verify(itemRepository).deleteById(id);
    }

    @Test
    void testDeleteNotFound() {
        when(itemRepository.existsById(id)).thenReturn(false);

        assertThrows(ItemNotFoundException.class, () -> itemService.delete(id));
    }

    @Test
    void testDeleteWithOrderItem() {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setId(UUID.randomUUID());

        when(orderItemRepository.findByItemId(id)).thenReturn(List.of(orderItem));
        when(itemRepository.existsById(id)).thenReturn(true);

        itemService.delete(id);

        verify(orderItemRepository).deleteById(orderItem.getId());
    }
}
