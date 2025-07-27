package org.example.orderserver.service;

import org.example.orderserver.dto.OrderItemRequest;
import org.example.orderserver.dto.OrderItemResponse;
import org.example.orderserver.dto.OrderRequest;
import org.example.orderserver.dto.OrderResponse;
import org.example.orderserver.entity.Item;
import org.example.orderserver.entity.Order;
import org.example.orderserver.entity.OrderItem;
import org.example.orderserver.entity.OrderStatus;
import org.example.orderserver.entity.UserInfo;
import org.example.orderserver.exception.InconsistentDataException;
import org.example.orderserver.exception.ItemNotFoundException;
import org.example.orderserver.exception.OrderNotFoundException;
import org.example.orderserver.exception.UserNotFoundException;
import org.example.orderserver.mapper.OrderMapper;
import org.example.orderserver.repository.ItemRepository;
import org.example.orderserver.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper mapper;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private Mono responseMono;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @InjectMocks
    private OrderService orderService;

    private final OrderRequest orderRequest = new OrderRequest();
    private final Order order = new Order();
    private final OrderResponse orderResponse = new OrderResponse();
    private final OrderItem orderItem = new OrderItem();
    private final OrderItemRequest orderItemRequest = new OrderItemRequest();
    private final OrderItemResponse orderItemResponse = new OrderItemResponse();
    private final UserInfo userInfo = new UserInfo();
    private final Item item = new Item();
    private final UUID itemId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final UUID orderItemId = UUID.randomUUID();

    @BeforeEach
    void initialize() {
        userInfo.setId(UUID.randomUUID());
        userInfo.setName("Test");
        userInfo.setEmail("test@gmail.com");

        item.setId(itemId);
        item.setName("Test");
        item.setPrice(1.1f);

        orderItem.setItem(item);
        orderItem.setId(orderItemId);
        orderItem.setOrder(order);
        orderItem.setQuantity(10L);

        orderItemRequest.setItemId(itemId);
        orderItemRequest.setQuantity(10L);

        orderItemResponse.setId(orderItemId);
        orderItemResponse.setOrderId(orderId);
        orderItemResponse.setQuantity(10L);
        orderItemResponse.setItemId(itemId);

        orderRequest.setUserEmail("test@gmail.com");
        orderRequest.setOrderItems(List.of(orderItemRequest));

        order.setId(orderId);
        order.setUserId(userInfo.getId());
        order.setOrderItems(List.of(orderItem));

        orderResponse.setId(orderId);
        orderResponse.setUserInfo(userInfo);
        orderResponse.setOrderItems(List.of(orderItemResponse));
    }

    void setupGetRequest() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any(Consumer.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    }

    void setupPostRequest() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any(Consumer.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    }

    void setupPostResponse() {
        when(responseSpec.bodyToMono(new ParameterizedTypeReference<List<UserInfo>>() {}))
                .thenReturn(responseMono);
        when(responseMono.block()).thenReturn(List.of(userInfo));
    }

    void setupPostUserNotFoundResponse() {
        when(responseSpec.bodyToMono(new ParameterizedTypeReference<List<UserInfo>>() {}))
                .thenReturn(Mono.error(new UserNotFoundException("User not found")));
    }

    void setupGetResponse() {
        when(responseSpec.bodyToMono(UserInfo.class)).thenReturn(responseMono);
        when(responseMono.block()).thenReturn(userInfo);
    }

    void setupGetUserNotFoundResponse() {
        when(responseSpec.bodyToMono(UserInfo.class))
                .thenReturn(Mono.error(new UserNotFoundException("User not found")));
    }

    @Test
    void testSave() {
        setupGetRequest();
        setupGetResponse();

        when(mapper.toEntity(orderRequest)).thenReturn(order);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(orderRepository.save(order)).thenReturn(order);
        when(mapper.toResponse(order)).thenReturn(orderResponse);

        OrderResponse response = orderService.save(orderRequest, "Bearer token");

        assertThat(response).isEqualTo(orderResponse);
    }

    @Test
    void testSaveItemNotFound() {
        when(mapper.toEntity(orderRequest)).thenReturn(order);
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class,
                () -> orderService.save(orderRequest, "Bearer token"));
    }

    @Test
    void testSaveUserNotFound() {
        setupGetRequest();
        setupGetUserNotFoundResponse();

        when(mapper.toEntity(orderRequest)).thenReturn(order);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        when(responseSpec.bodyToMono(UserInfo.class))
                .thenReturn(Mono.error(new UserNotFoundException("User not found")));

        assertThrows(UserNotFoundException.class,
                () -> orderService.save(orderRequest, "Bearer token"));
    }

    @Test
    void testFindById() {
        setupGetRequest();
        setupGetResponse();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(mapper.toResponse(order)).thenReturn(orderResponse);

        OrderResponse response = orderService.findById(
                orderId, "test@gmail.com", "Bearer token");

        assertThat(response).isEqualTo(orderResponse);
    }

    @Test
    void testFindByIdOrderNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.findById(orderId, "test@gmail.com", "Bearer token"));
    }

    @Test
    void testFindByIdUserNotFound() {
        setupGetRequest();
        setupGetUserNotFoundResponse();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(mapper.toResponse(order)).thenReturn(orderResponse);

        assertThrows(UserNotFoundException.class,
                () -> orderService.findById(orderId, "test@gmail.com", "Bearer token"));
    }

    @Test
    void testFindByIdUserIdMismatch() {
        setupGetRequest();
        setupGetResponse();

        order.setUserId(UUID.randomUUID());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(mapper.toResponse(order)).thenReturn(orderResponse);

        assertThrows(InconsistentDataException.class,
                () -> orderService.findById(orderId, "test@gmail.com", "Bearer token"));
    }

    @Test
    void testFindByIds() {
        setupPostRequest();
        setupPostResponse();

        when(orderRepository.findByIds(List.of(orderId))).thenReturn(List.of(order));
        when(mapper.toResponse(order)).thenReturn(orderResponse);

        List<OrderResponse> response = orderService.findByIds(List.of(orderId), "Bearer token");

        assertThat(response).isEqualTo(List.of(orderResponse));
    }

    @Test
    void testFindByIdsUserNotFound() {
        setupPostRequest();
        setupPostUserNotFoundResponse();

        when(orderRepository.findByIds(List.of(orderId))).thenReturn(List.of(order));

        assertThrows(UserNotFoundException.class,
                () -> orderService.findByIds(List.of(orderId), "Bearer token"));
    }

    @Test
    void testFindByStatuses() {
        setupPostRequest();
        setupPostResponse();

        when(orderRepository.findByStatuses(List.of(OrderStatus.CREATED))).thenReturn(List.of(order));
        when(mapper.toResponse(order)).thenReturn(orderResponse);

        List<OrderResponse> response = orderService
                .findByStatuses(List.of(OrderStatus.CREATED), "Bearer token");

        assertThat(response).isEqualTo(List.of(orderResponse));
    }

    @Test
    void testFindByStatusesUserNotFound() {
        setupPostRequest();
        setupPostUserNotFoundResponse();

        when(orderRepository.findByStatuses(List.of(OrderStatus.CREATED))).thenReturn(List.of(order));

        assertThrows(UserNotFoundException.class,
                () -> orderService.findByStatuses(List.of(OrderStatus.CREATED), "Bearer token"));
    }

    @Test
    void testUpdate() {
        setupGetRequest();
        setupGetResponse();

        OrderRequest request = new OrderRequest();
        request.setStatus(OrderStatus.IN_PROGRESS);
        orderResponse.setStatus("IN_PROGRESS");

        OrderResponse updatedOrderResponse = new OrderResponse();
        updatedOrderResponse.setId(orderId);
        updatedOrderResponse.setUserInfo(userInfo);
        updatedOrderResponse.setOrderItems(List.of(orderItemResponse));
        updatedOrderResponse.setStatus("IN_PROGRESS");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(mapper.toResponse(order)).thenReturn(orderResponse);

        OrderResponse response = orderService.update(orderId, "test@gmail.com",
                "Bearer token", request);

        assertThat(response).isEqualTo(updatedOrderResponse);
    }

    @Test
    void testUpdateOrderNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.update(orderId,
                "test@gmail.com", "Bearer token", null));
    }

    @Test
    void testUpdateUserNotFound() {
        setupGetRequest();
        setupGetUserNotFoundResponse();

        OrderRequest request = new OrderRequest();
        request.setStatus(OrderStatus.IN_PROGRESS);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(UserNotFoundException.class, () -> orderService.update(orderId,
                "test@gmail.com", "Bearer token", null));
    }

    @Test
    void testUpdateUserIdMismatch() {
        setupGetRequest();
        setupGetResponse();

        order.setUserId(UUID.randomUUID());

        OrderRequest request = new OrderRequest();
        request.setStatus(OrderStatus.IN_PROGRESS);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(InconsistentDataException.class, () -> orderService.update(orderId,
                "test@gmail.com", "Bearer token", request));
    }

    @Test
    void testDelete() {
        when(orderRepository.existsById(orderId)).thenReturn(true);

        orderService.delete(orderId);

        verify(orderRepository).deleteById(orderId);
    }

    @Test
    void testDeleteOrderNotFound() {
        when(orderRepository.existsById(orderId)).thenReturn(false);

        assertThrows(OrderNotFoundException.class, () -> orderService.delete(orderId));
    }
}
