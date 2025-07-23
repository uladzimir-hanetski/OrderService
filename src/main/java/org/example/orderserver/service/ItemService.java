package org.example.orderserver.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.orderserver.dto.ItemRequest;
import org.example.orderserver.dto.ItemResponse;
import org.example.orderserver.entity.Item;
import org.example.orderserver.exception.ItemNotFoundException;
import org.example.orderserver.mapper.ItemMapper;
import org.example.orderserver.repository.ItemRepository;
import org.example.orderserver.repository.OrderItemRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemMapper mapper;

    public ItemResponse save(ItemRequest itemRequest) {
        return mapper.toResponse(itemRepository.save(mapper.toEntity(itemRequest)));
    }

    public ItemResponse findById(UUID id) {
        return mapper.toResponse(itemRepository.findById(id).orElseThrow(
                () -> new ItemNotFoundException("Item not found")));
    }

    @Transactional
    public ItemResponse update(UUID id, ItemRequest itemRequest) {
        Item item = itemRepository.findById(id).orElseThrow(
                () -> new ItemNotFoundException("Item not found"));

        if (itemRequest.getName() != null) {
            item.setName(itemRequest.getName());
        }
        if (itemRequest.getPrice() != null) {
            item.setPrice(itemRequest.getPrice());
        }

        return mapper.toResponse(itemRepository.save(item));
    }

    @Transactional
    public void delete(UUID id) {
        if (!itemRepository.existsById(id)) {
            throw new ItemNotFoundException("Item not found");
        }

        orderItemRepository.findByItemId(id).forEach(
                orderItem -> orderItemRepository.deleteById(orderItem.getId()));

        itemRepository.deleteById(id);
    }
}
