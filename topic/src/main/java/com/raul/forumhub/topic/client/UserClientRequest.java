package com.raul.forumhub.topic.client;

import com.raul.forumhub.topic.domain.Author;
import com.raul.forumhub.topic.exception.RestClientException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

@Component
@Validated
public class UserClientRequest {

    private final WebClient webClient;
    private final UserClientProperties userClientProperties;

    public UserClientRequest(WebClient webClient, UserClientProperties userClientProperties) {
        this.webClient = webClient;
        this.userClientProperties = userClientProperties;
    }

    public @Valid Author getUserById(Long id) {
        try {
            Mono<ResponseEntity<Author>> response = this.webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme(userClientProperties.getSchema())
                            .host(userClientProperties.getHost())
                            .port(userClientProperties.getPort())
                            .path("/api-forum/v1/forumhub/users/summary-info")
                            .queryParam("user_id", id)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .acceptCharset(StandardCharsets.UTF_8)
                    .retrieve()
                    .toEntity(Author.class)
                    .timeout(Duration.ofMillis(10000))
                    .onErrorResume(WebClientRequestException.class, ex ->
                            Mono.error(new RestClientException(HttpStatus.SERVICE_UNAVAILABLE, "Serviço de usuário indisponível")))
                    .onErrorResume(WebClientResponseException.class, ex ->
                            Mono.error(new RestClientException(ex.getStatusCode(), ex.getResponseBodyAsString())))
                    .onErrorResume(TimeoutException.class, ex ->
                            Mono.error(() -> new RestClientException(HttpStatus.SERVICE_UNAVAILABLE,
                                    "Erro inesperado durante a comunicação com o serviço de usuário")));

            return Objects.requireNonNull(response.block(),
                    "Erro inesperado durante a comunicação com o serviço de usuário").getBody();

        } catch (RestClientException ex) {
            throw new RestClientException(ex.getHttpStatusCode(), ex.getMessage());
        }

    }

}
