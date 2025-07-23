package org.example.orderserver.mapper;

import org.example.orderserver.dto.ItemRequest;
import org.example.orderserver.dto.ItemResponse;
import org.example.orderserver.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemMapper {

    Item toEntity(ItemRequest itemRequest);
    ItemResponse toResponse(Item item);
}
