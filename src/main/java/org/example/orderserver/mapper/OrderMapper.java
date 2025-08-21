package org.example.orderserver.mapper;

import org.example.orderserver.dto.OrderRequest;
import org.example.orderserver.dto.OrderResponse;
import org.example.orderserver.entity.Order;
import org.example.orderserver.entity.OrderMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {OrderItemMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    Order toEntity(OrderRequest orderRequest);
    OrderResponse toResponse(Order order);

    @Mapping(source = "order.id", target = "orderId")
    OrderMessage toMessage(Order order);
}
