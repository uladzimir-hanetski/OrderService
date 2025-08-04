package org.example.orderserver.controller;

import org.example.orderserver.dto.OrderItemRequest;
import org.example.orderserver.dto.OrderItemResponse;
import org.example.orderserver.entity.Item;
import org.example.orderserver.entity.Order;
import org.example.orderserver.entity.OrderItem;
import org.example.orderserver.entity.OrderStatus;
import org.example.orderserver.exception.ErrorResponse;
import org.example.orderserver.repository.ItemRepository;
import org.example.orderserver.repository.OrderItemRepository;
import org.example.orderserver.repository.OrderRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderItemControllerIntegrationTest {
    private static final String BASE_URL_CREATE = "/api/v1/order_items/order/";
    private static final String BASE_URL_CHANGE = "/api/v1/order_items/";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @AfterAll
    static void stopServer() {
        postgres.stop();
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    private final OrderItemRequest orderItemRequest = new OrderItemRequest();
    private Order testOrder;
    private OrderItem testOrderItem;

    @BeforeEach
    void initialize() {
        orderItemRepository.deleteAll();

        Item item = new Item();
        item.setPrice(10f);
        item.setName("test");
        Item testItem = itemRepository.save(item);

        Order order = new Order();
        order.setUserId(UUID.randomUUID());
        order.setStatus(OrderStatus.CREATED);
        order.setOrderItems(new ArrayList<>());
        order.setCreationDate(LocalDate.now());
        testOrder = orderRepository.save(order);

        orderItemRequest.setQuantity(10L);
        orderItemRequest.setItemId(testItem.getId());

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(testOrder);
        testOrder.getOrderItems().add(orderItem);
        orderItem.setItem(testItem);
        orderItem.setQuantity(10L);
        testOrderItem = orderItemRepository.save(orderItem);
    }

    @Test
    void testSaveOrderItem() {
        ResponseEntity<OrderItemResponse> response = restTemplate.postForEntity(
                BASE_URL_CREATE + testOrder.getId(), orderItemRequest, OrderItemResponse.class);

        assertThat(response.getBody().getId()).isNotNull();
    }

    @Test
    void testSaveItemNotFound() {
        orderItemRequest.setItemId(UUID.randomUUID());

        ResponseEntity<OrderItemResponse> response = restTemplate.postForEntity(
                BASE_URL_CREATE + testOrder.getId(), orderItemRequest, OrderItemResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testSaveOrderItemOrderNotFound() {
        ResponseEntity<OrderItemResponse> response = restTemplate.postForEntity(
                BASE_URL_CREATE + UUID.randomUUID(), orderItemRequest, OrderItemResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testFindOrderItemById() {
        ResponseEntity<OrderItemResponse> response = restTemplate.getForEntity(
                BASE_URL_CHANGE + testOrderItem.getId(), OrderItemResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testFindOrderItemByIdNotFound() {
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                BASE_URL_CHANGE + UUID.randomUUID(), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testUpdateOrderItem() {
        OrderItemRequest updatedOrderItem = new OrderItemRequest();
        updatedOrderItem.setQuantity(10000L);

        HttpEntity<OrderItemRequest> entity = new HttpEntity<>(updatedOrderItem);

        ResponseEntity<OrderItemResponse> response = restTemplate.exchange(
                BASE_URL_CHANGE + testOrderItem.getId(), HttpMethod.PUT, entity, OrderItemResponse.class);

        assertThat(response.getBody().getQuantity()).isEqualTo(updatedOrderItem.getQuantity());
    }

    @Test
    void testUpdateOrderItemNotFound() {
        OrderItemRequest updatedOrderItem = new OrderItemRequest();
        updatedOrderItem.setQuantity(10000L);

        HttpEntity<OrderItemRequest> entity = new HttpEntity<>(updatedOrderItem);

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                BASE_URL_CHANGE + UUID.randomUUID(), HttpMethod.PUT, entity, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testDeleteOrderItem() {
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                BASE_URL_CHANGE + testOrderItem.getId(), HttpMethod.DELETE, null, Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> response = restTemplate.getForEntity(
                BASE_URL_CHANGE + testOrderItem.getId(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testDeleteOrderItemNotFound() {
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                BASE_URL_CHANGE + UUID.randomUUID(), HttpMethod.DELETE, null, Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
