package org.example.orderserver.controller;

import org.example.orderserver.dto.ItemRequest;
import org.example.orderserver.dto.ItemResponse;
import org.example.orderserver.entity.Item;
import org.example.orderserver.exception.ErrorResponse;
import org.example.orderserver.repository.ItemRepository;
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
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ItemControllerIntegrationTest {
    private static final String BASE_URL = "/api/v1/items/";

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
    private ItemRepository itemRepository;

    private final ItemRequest itemRequest = new ItemRequest();
    private Item testItem = new Item();

    @BeforeEach
    void initialize() {
        itemRepository.deleteAll();

        itemRequest.setName("test");
        itemRequest.setPrice(10f);

        Item item = new Item();
        item.setPrice(10f);
        item.setName("test");

        testItem = itemRepository.save(item);
    }

    ItemRequest createUpdatedItemRequest() {
        ItemRequest item = new ItemRequest();
        item.setName("test");
        item.setPrice(1000f);

        return item;
    }

    @Test
    void testCreateItem() {
        ResponseEntity<ItemResponse> response = restTemplate.postForEntity(
                "/api/v1/items", itemRequest, ItemResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo(itemRequest.getName());
    }

    @Test
    void testFindItemById() {
        ResponseEntity<ItemResponse> response = restTemplate.getForEntity(
                BASE_URL + testItem.getId(), ItemResponse.class);

        assertThat(response.getBody().getName()).isEqualTo(itemRequest.getName());
    }

    @Test
    void testFindItemByIdNotFound() {
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                BASE_URL + UUID.randomUUID(), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testUpdateItem() {
        ItemRequest updatedItem = createUpdatedItemRequest();

        HttpEntity<ItemRequest> entity = new HttpEntity<>(updatedItem);

        ResponseEntity<ItemResponse> response = restTemplate.exchange(
                BASE_URL + testItem.getId(), HttpMethod.PUT, entity, ItemResponse.class);

        assertThat(response.getBody().getPrice()).isEqualTo(updatedItem.getPrice());
    }

    @Test
    void testUpdateItemNotFound() {
        HttpEntity<ItemRequest> entity = new HttpEntity<>(itemRequest);

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                BASE_URL + UUID.randomUUID(), HttpMethod.PUT, entity, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testDeleteItemById() {
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                BASE_URL + testItem.getId(), HttpMethod.DELETE, null, Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> response = restTemplate.getForEntity(
                BASE_URL + testItem.getId(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testDeleteItemByIdNotFound() {
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                BASE_URL + UUID.randomUUID(), HttpMethod.DELETE, null, Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
