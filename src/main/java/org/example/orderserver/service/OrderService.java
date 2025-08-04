package org.example.orderserver.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.orderserver.dto.OrderRequest;
import org.example.orderserver.dto.OrderResponse;
import org.example.orderserver.entity.Item;
import org.example.orderserver.entity.Order;
import org.example.orderserver.entity.OrderItem;
import org.example.orderserver.entity.OrderStatus;
import org.example.orderserver.entity.UserInfo;
import org.example.orderserver.exception.AuthorizationException;
import org.example.orderserver.exception.InconsistentDataException;
import org.example.orderserver.exception.ItemNotFoundException;
import org.example.orderserver.exception.OrderNotFoundException;
import org.example.orderserver.exception.UserNotFoundException;
import org.example.orderserver.mapper.OrderMapper;
import org.example.orderserver.repository.ItemRepository;
import org.example.orderserver.repository.OrderRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper mapper;
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Transactional
    public OrderResponse save(OrderRequest orderRequest, String tokenHeader) {
        Order order = mapper.toEntity(orderRequest);
        order.setCreationDate(LocalDate.now());

        for (int i = 0; i < order.getOrderItems().size(); i++) {
            OrderItem orderItem = order.getOrderItems().get(i);
            orderItem.setOrder(order);

            UUID itemId = orderRequest.getOrderItems().get(i).getItemId();
            Item item = itemRepository.findById(itemId).orElseThrow(
                    () -> new ItemNotFoundException("Item with id '"+ itemId + "' not found"));
            orderItem.setItem(item);
        }

        UserInfo userInfo = userService.getUserInfoByEmail(getTokenFromHeader(tokenHeader), orderRequest.getUserEmail());
        if (userInfo == null) {
            throw new UserNotFoundException("User not found");
        }
        order.setUserId(userInfo.getId());

        OrderResponse orderResponse = mapper.toResponse(orderRepository.save(order));
        orderResponse.setUserInfo(userInfo);

        return orderResponse;
    }

    public OrderResponse findById(UUID id, String email, String tokenHeader) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new OrderNotFoundException("Order not found"));
        OrderResponse orderResponse = mapper.toResponse(order);

        UserInfo userInfo = userService.getUserInfoByEmail(getTokenFromHeader(tokenHeader), email);
        if (userInfo == null) {
            throw new UserNotFoundException("User not found");
        }
        if (!userInfo.getId().equals(order.getUserId())) {
            throw new InconsistentDataException("User id mismatch");
        }

        orderResponse.setUserInfo(userInfo);

        return orderResponse;
    }

    public List<OrderResponse> findByIds(List<UUID> ids, String tokenHeader) {
        List<Order> orders = orderRepository.findByIds(ids);

        return createOrderResponses(orders, tokenHeader);
    }

    public List<OrderResponse> findByStatuses(List<OrderStatus> statuses, String tokenHeader) {
        List<Order> orders = orderRepository.findByStatuses(statuses);

        return createOrderResponses(orders, tokenHeader);
    }

    @Transactional
    public OrderResponse update(UUID id, String email, String tokenHeader, OrderRequest orderRequest) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new OrderNotFoundException("Order not found"));

        UserInfo userInfo = userService.getUserInfoByEmail(getTokenFromHeader(tokenHeader), email);
        if (userInfo == null) {
            throw new UserNotFoundException("User not found");
        }
        if (!userInfo.getId().equals(order.getUserId())) {
            throw new InconsistentDataException("User id mismatch");
        }

        if (orderRequest.getStatus() != null) {
            order.setStatus(orderRequest.getStatus());
        }

        OrderResponse orderResponse = mapper.toResponse(orderRepository.save(order));
        orderResponse.setUserInfo(userInfo);

        return orderResponse;
    }

    @Transactional
    public void delete(UUID id) {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException("Order not found");
        }

        orderRepository.deleteById(id);
    }

    private List<OrderResponse> createOrderResponses(List<Order> orders, String tokenHeader) {
        List<UUID> userIds = orders.stream().map(Order::getUserId).distinct().toList();
        List<UserInfo> usersInfo = userService.getUserInfoByIds(getTokenFromHeader(tokenHeader), userIds);
        if (usersInfo == null) {
            throw new UserNotFoundException("User not found");
        }

        Map<UUID, UserInfo> users = usersInfo.stream()
                .collect(Collectors.toMap(UserInfo::getId, Function.identity()));

        return orders.stream().map(order -> {
            OrderResponse orderResponse = mapper.toResponse(order);
            orderResponse.setUserInfo(users.get(order.getUserId()));
            return orderResponse;
        }).toList();
    }

    private String getTokenFromHeader(String header) {
        if (header != null && header.startsWith("Bearer ")) return header.substring(7);
        else throw new AuthorizationException("Invalid <Authorization> header");
    }
}
