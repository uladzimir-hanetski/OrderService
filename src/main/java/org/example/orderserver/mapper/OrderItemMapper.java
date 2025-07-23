package org.example.orderserver.mapper;

import org.example.orderserver.dto.OrderItemRequest;
import org.example.orderserver.dto.OrderItemResponse;
import org.example.orderserver.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemMapper {

    OrderItem toEntity(OrderItemRequest orderItemRequest);

    @Mapping(source =  "item.id", target = "itemId")
    @Mapping(source = "order.id", target = "orderId")
    OrderItemResponse toResponse(OrderItem orderItem);
}
