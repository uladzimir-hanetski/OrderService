package org.example.orderserver.controller;

import lombok.RequiredArgsConstructor;
import org.example.orderserver.dto.ItemRequest;
import org.example.orderserver.dto.ItemResponse;
import org.example.orderserver.service.ItemService;
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

@RequestMapping("/api/v1/items")
@RestController
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemResponse> createItem(@Validated(ItemRequest.CreateValidation.class)
                                                       @RequestBody ItemRequest itemRequest) {
        return ResponseEntity.ok(itemService.save(itemRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> findItemById(@PathVariable UUID id) {
        return ResponseEntity.ok(itemService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(@PathVariable UUID id,
                                                   @Validated(ItemRequest.UpdateValidation.class)
                                                   @RequestBody ItemRequest itemRequest) {
        return ResponseEntity.ok(itemService.update(id, itemRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItemById(@PathVariable UUID id) {
        itemService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
