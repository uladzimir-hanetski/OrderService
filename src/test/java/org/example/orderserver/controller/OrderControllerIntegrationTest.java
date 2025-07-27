package org.example.orderserver.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.example.orderserver.dto.OrderItemRequest;
import org.example.orderserver.dto.OrderRequest;
import org.example.orderserver.dto.OrderResponse;
import org.example.orderserver.entity.Item;
import org.example.orderserver.entity.Order;
import org.example.orderserver.entity.OrderStatus;
import org.example.orderserver.exception.ErrorResponse;
import org.example.orderserver.repository.ItemRepository;
import org.example.orderserver.repository.OrderRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
import java.util.List;
import java.util.UUID;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerIntegrationTest {
    private static final String USER_EMAIL = "/v1/users/email/.*";
    private static final String USER_IDS = "/v1/users/ids";
    private static final String BASE_URL = "/api/v1/orders/";

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

    @BeforeAll
    static void startServer() {
        wireMock = new WireMockServer(8080);
        wireMock.start();
        WireMock.configureFor("localhost", 8080);
    }

    @AfterAll
    static void stopServer() {
        wireMock.stop();
        postgres.stop();
    }

    @Autowired
    private TestRestTemplate restTemplate;

    private static WireMockServer wireMock;

    private final String userId = "d6d9d8f7-5d9f-4c99-9e6b-2d7e9d7f7c9b";

    private final String userServiceResponse = """
            {
            "id":""" + "\"" + userId + "\"," +"""
            "name": "Test",
            "surname": "Test",
            "birthDate": "2020-10-10",
            "email": "test@gmail.com"
            }
            """;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    private final OrderRequest orderRequest = new OrderRequest();
    private final OrderItemRequest orderItemRequest = new OrderItemRequest();
    private Order testOrder;

    @BeforeEach
    void initialize() {
        Item item = new Item();
        item.setName("test");
        item.setPrice(10f);
        Item testItem = itemRepository.save(item);

        orderItemRequest.setQuantity(10L);
        orderItemRequest.setItemId(testItem.getId());

        orderRequest.setStatus(OrderStatus.CREATED);
        orderRequest.setUserEmail("test@gmail.com");
        orderRequest.setOrderItems(List.of(orderItemRequest));
    }

    void initUserResponse() {
        wireMock.stubFor(get(urlPathMatching(USER_EMAIL))
                .willReturn(aResponse()
                        .withBody(userServiceResponse)
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.OK.value())));
    }

    void initUserResponseNotFound() {
        wireMock.stubFor(get(urlPathMatching(USER_EMAIL))
                .willReturn(aResponse().withStatus(404)));
    }

    void initUserResponses() {
        wireMock.stubFor(post(urlPathMatching(USER_IDS))
                .willReturn(aResponse()
                        .withBody("[" + userServiceResponse + "]")
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.OK.value())));
    }

    void saveOrder() {
        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        order.setUserId(UUID.fromString(userId));
        order.setOrderItems(new ArrayList<>());
        order.setCreationDate(LocalDate.now());

        testOrder = orderRepository.save(order);
    }

    void saveOrderMismatch() {
        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        order.setUserId(UUID.randomUUID());
        order.setOrderItems(new ArrayList<>());
        order.setCreationDate(LocalDate.now());

        testOrder = orderRepository.save(order);
    }

    HttpEntity<OrderRequest> initRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer token");
        return new HttpEntity<>(orderRequest, headers);
    }

    HttpEntity<Void> initEmptyRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        return new HttpEntity<>(headers);
    }

    @Test
    void testCreateOrder() {
        initUserResponse();
        HttpEntity<OrderRequest> requestEntity = initRequest();

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
                "/api/v1/orders", requestEntity, OrderResponse.class);

        assertThat(response.getBody().getUserInfo().getEmail()).isEqualTo(orderRequest.getUserEmail());
    }

    @Test
    void testCreateOrderItemNotFound() {
        initUserResponse();
        orderItemRequest.setItemId(UUID.randomUUID());
        HttpEntity<OrderRequest> requestEntity = initRequest();

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/v1/orders", requestEntity, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testCreateOrderUserNotFound() {
        initUserResponseNotFound();
        HttpEntity<OrderRequest> requestEntity = initRequest();

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/v1/orders", requestEntity, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testFindOrderById() {
        saveOrder();
        initUserResponse();
        HttpEntity<Void> entity = initEmptyRequest();

        ResponseEntity<OrderResponse> response = restTemplate.exchange(
                BASE_URL + testOrder.getId() + "/test@gmail.com",
                HttpMethod.GET, entity, OrderResponse.class);

        assertThat(response.getBody().getId()).isEqualTo(testOrder.getId());
    }

    @Test
    void testFindOrderByIdNotFound() {
        HttpEntity<Void> entity = initEmptyRequest();

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                BASE_URL + UUID.randomUUID() + "/test@gmail.com",
                HttpMethod.GET, entity, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testFindOrderByIdUserNotFound() {
        saveOrder();
        initUserResponseNotFound();
        HttpEntity<Void> entity = initEmptyRequest();

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                BASE_URL + testOrder.getId() + "/test@gmail.com",
                HttpMethod.GET, entity, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testFindOrderByIdMismatch() {
        saveOrderMismatch();
        initUserResponse();
        HttpEntity<Void> entity = initEmptyRequest();

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                BASE_URL + testOrder.getId() + "/test@gmail.com",
                HttpMethod.GET, entity, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testFindByIds() {
        saveOrder();
        initUserResponses();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        HttpEntity<List<UUID>> entity = new HttpEntity<>(List.of(testOrder.getId()), headers);

        ResponseEntity<List<OrderResponse>> response = restTemplate.exchange(
                "/api/v1/orders/ids",
                HttpMethod.POST,
                entity, new ParameterizedTypeReference<>() {});

        assertThat(response.getBody().getFirst().getId()).isEqualTo(testOrder.getId());
    }

    @Test
    void testFindByStatuses() {
        orderRepository.deleteAll();
        saveOrder();
        initUserResponses();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        HttpEntity<List<OrderStatus>> entity = new HttpEntity<>(
                List.of(OrderStatus.CREATED), headers);

        ResponseEntity<List<OrderResponse>> response = restTemplate.exchange(
                BASE_URL + "statuses",
                HttpMethod.POST,
                entity, new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getFirst().getId()).isEqualTo(testOrder.getId());
    }

    @Test
    void testUpdateOrder() {
        saveOrder();
        initUserResponses();

        OrderRequest request = new OrderRequest();
        request.setStatus(OrderStatus.COMPLETED);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        HttpEntity<OrderRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<OrderResponse> response = restTemplate.exchange(
                BASE_URL + testOrder.getId() + "/test@gmail.com",
                HttpMethod.PUT, entity, OrderResponse.class);

        assertThat(response.getBody().getStatus()).isEqualTo(
                request.getStatus().toString());
    }

    @Test
    void testUpdateOrderNotFound() {
        HttpEntity<OrderRequest> entity = initRequest();

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                BASE_URL + UUID.randomUUID() + "/test@gmail.com",
                HttpMethod.PUT, entity, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testUpdateOrderUserNotFound() {
        saveOrder();
        initUserResponseNotFound();
        HttpEntity<OrderRequest> entity = initRequest();

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                BASE_URL + UUID.randomUUID() + "/test@gmail.com",
                HttpMethod.PUT, entity, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testUpdateOrderMismatch() {
        saveOrderMismatch();
        initUserResponse();
        HttpEntity<OrderRequest> entity = initRequest();

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                BASE_URL + testOrder.getId() + "/test@gmail.com",
                HttpMethod.PUT, entity, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void deleteOrderById() {
        saveOrder();
        HttpEntity<Void> entity = initEmptyRequest();

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                BASE_URL + testOrder.getId(),
                HttpMethod.DELETE, null, Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                BASE_URL + testOrder.getId() + "/test@gmail.com",
                HttpMethod.GET, entity, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testDeleteNotFound() {
        ResponseEntity<Void> response = restTemplate.exchange(
                BASE_URL + UUID.randomUUID(),
                HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
