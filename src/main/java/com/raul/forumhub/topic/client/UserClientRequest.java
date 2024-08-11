package com.raul.forumhub.topic.client;

import com.raul.forumhub.topic.domain.Author;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

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
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Author.class)
                .onErrorResume(WebClientResponseException.class, ex ->
                        ex.getStatusCode().value() == 404 ? Mono.empty() : Mono.error(ex))
                .map(user -> new Author(user.getId(), user.getName(), user.getEmail(), user.getProfile()))
                .log(logger)
                .block();

    }

}
