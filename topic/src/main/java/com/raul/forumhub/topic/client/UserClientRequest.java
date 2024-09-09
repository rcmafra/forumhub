package com.raul.forumhub.topic.client;

import com.raul.forumhub.topic.domain.Author;
import com.raul.forumhub.topic.exception.RestClientException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
public class UserClientRequest {

    private final WebClient webClient;
    private final UserClientProperties userClientProperties;
    private final Logger logger = Loggers.getLogger(UserClientRequest.class);

    public UserClientRequest(WebClient webClient, UserClientProperties userClientProperties) {
        this.webClient = webClient;
        this.userClientProperties = userClientProperties;
    }

    public Author getUserById(Long id) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme(userClientProperties.getSchema())
                        .host(userClientProperties.getHost())
                        .port(userClientProperties.getPort())
                        .path("/api-forum/v1/forumhub/users/summary-info")
                        .queryParam("user_id", id)
                        .build())
                .retrieve()
                .bodyToMono(Author.class)
                .timeout(Duration.ofMillis(1000))
                .onErrorResume(TimeoutException.class, ex ->
                        Mono.error(() -> new TimeoutException("Nenhuma resposta obtida dentro do tempo estimado")))
                .onErrorResume(WebClientResponseException.class, ex ->
                        Mono.error(new RestClientException(ex.getStatusCode(), ex.getResponseBodyAsString())))
                .map(user -> new Author(user.getId(), user.getUsername(), user.getEmail(), user.getProfile()))
                .log(logger)
                .block();

    }

}
