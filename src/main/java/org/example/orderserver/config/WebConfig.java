package org.example.orderserver.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;

@Configuration
public class WebConfig {
    private static final int TIMEOUT = 5000;

    @Bean
    public WebClient webClient(@Value("${USER_SERVICE_URI}") String userServiceUri) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT)
                .responseTimeout(Duration.ofMillis(TIMEOUT));

        return WebClient.builder()
                .baseUrl(userServiceUri)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
