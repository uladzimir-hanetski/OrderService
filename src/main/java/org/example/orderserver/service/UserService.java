package org.example.orderserver.service;

import lombok.RequiredArgsConstructor;
import org.example.orderserver.entity.UserInfo;
import org.example.orderserver.exception.AuthorizationException;
import org.example.orderserver.exception.UserNotFoundException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final WebClient webClient;

    public UserInfo getUserInfoByEmail(final String token, final String email) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users/email/")
                        .pathSegment(email)
                        .build())
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND,
                        e -> Mono.error(new UserNotFoundException("User not found")))
                .onStatus(status -> status == HttpStatus.UNAUTHORIZED,
                        e -> Mono.error(new AuthorizationException("Incorrect token")))
                .onStatus(status -> status == HttpStatus.INTERNAL_SERVER_ERROR,
                        e -> Mono.error(new RuntimeException("User Service unavailable")))
                .bodyToMono(UserInfo.class)
                .block();
    }

    public List<UserInfo> getUserInfoByIds(String token, List<UUID> ids) {
        return webClient
                .post()
                .uri("/users/ids")
                .bodyValue(ids)
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .onStatus(status -> status == HttpStatus.UNAUTHORIZED,
                        e -> Mono.error(new AuthorizationException("Incorrect token")))
                .onStatus(status -> status == HttpStatus.INTERNAL_SERVER_ERROR,
                        e -> Mono.error(new RuntimeException("User Service unavailable")))
                .bodyToMono(new ParameterizedTypeReference<List<UserInfo>>() {})
                .block();
    }
}
