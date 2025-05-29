package com.raul.forumhub.topic.client;

import com.raul.forumhub.topic.domain.Author;
import com.raul.forumhub.topic.exception.RestClientException;
import com.raul.forumhub.topic.util.TestsHelper;
import jakarta.validation.ConstraintViolationException;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class UserClientRequestTest {

    @Autowired
    UserClientRequest userClientRequest;

    @Autowired
    UserClientProperties userClientProperties;

    static MockWebServer authorizationServer;

    MockWebServer userClient;


    @SneakyThrows
    @BeforeEach
    void userClientSetup() {
        userClient = new MockWebServer();
        userClient.start(Integer.parseInt(userClientProperties.getPort()));
    }

    @SneakyThrows
    @AfterEach
    void userClientTeardown() {
        userClient.shutdown();
    }

    @SneakyThrows
    @BeforeAll
    static void authorizationServerSetup() {
        authorizationServer = new MockWebServer();
        authorizationServer.setDispatcher(MockAuthorizationServer.dispatcher);
        authorizationServer.start(8092);

    }

    @SneakyThrows
    @AfterAll
    static void authorizationServerTeardown() {
        authorizationServer.shutdown();
    }


    @Test
    @DisplayName("Should return OK and summarized info user in the body with success")
    void shouldReturn200WhenRequestingSummarizedInfoUser() {
        Author author = TestsHelper.AuthorHelper.authorList().get(1);

        prepareResponse(response -> {
            response.setHeader("Content-Type", "application/json");
            response.setBody("{\"id\":2,\"username\":\"Jose\",\"email\":\"jose@email.com\"," +
                             "\"profile\":{\"profileName\":\"BASIC\"}}");
        });

        Author authorResponse = assertDoesNotThrow(() -> this.userClientRequest.getUserById(2L));

        assertThat(authorResponse.toString()).hasToString(author.toString());

        expecOnlyOneRequest();
        expectedRequest(expected -> {
                    assertThat(expected.getMethod()).isEqualTo("GET");
                    assertThat(expected.getPath()).isEqualTo(userClientProperties.getPath().concat("?user_id=2"));
                    assertThat(expected.getHeader(HttpHeaders.ACCEPT)).isEqualTo("application/json");
                    assertThat(expected.getHeader(HttpHeaders.ACCEPT_CHARSET)).isEqualTo("utf-8");
                    assertThat(expected.getHeader(HttpHeaders.AUTHORIZATION)).isNotEmpty();
                }
        );

    }

    @Test
    @DisplayName("Should return OK and summarized info user " +
                 "in the body with success same with the slow network")
    void shouldReturn200WhenRequestingSummarizedInfoUserWithSlowNetwork() {
        Author author = TestsHelper.AuthorHelper.authorList().get(1);

        prepareResponse(response -> {
            response.setHeader("Content-Type", "application/json");
            response.setChunkedBody("{\"id\":2,\"username\":\"Jose\",\"email\":\"jose@email.com\"," +
                                    "\"profile\":{\"profileName\":\"BASIC\"}}", 5);
        });

        Author authorResponse = assertDoesNotThrow(() -> this.userClientRequest.getUserById(2L));

        assertThat(authorResponse.toString()).hasToString(author.toString());

        expecOnlyOneRequest();
        expectedRequest(expected -> {
                    assertThat(expected.getMethod()).isEqualTo("GET");
                    assertThat(expected.getPath()).isEqualTo(userClientProperties.getPath().concat("?user_id=2"));
                    assertThat(expected.getHeader(HttpHeaders.ACCEPT)).isEqualTo("application/json");
                    assertThat(expected.getHeader(HttpHeaders.ACCEPT_CHARSET)).isEqualTo("utf-8");
                    assertThat(expected.getHeader(HttpHeaders.AUTHORIZATION)).isNotEmpty();
                }
        );

    }

    @Test
    @DisplayName("Should throw an exception when requesting info " +
                 "summarized from user and the server doesn't respond")
    void shouldThrowExceptionWhenReceiveTimedOutInTheRequestWithClient() {
        prepareResponse(response -> {
            response.setHeadersDelay(11000, TimeUnit.MILLISECONDS);
            response.setBodyDelay(11000, TimeUnit.MILLISECONDS);
            response.setHeader("Content-Type", "application/json");
        });

        assertThrows(RestClientException.class, () -> this.userClientRequest.getUserById(2L),
                "Erro inesperado durante a comunicação com o serviço de usuário");

        expecOnlyOneRequest();
        expectedRequest(expected -> {
                    assertThat(expected.getMethod()).isEqualTo("GET");
                    assertThat(expected.getPath()).isEqualTo(userClientProperties.getPath().concat("?user_id=2"));
                    assertThat(expected.getHeader(HttpHeaders.ACCEPT)).isEqualTo("application/json");
                    assertThat(expected.getHeader(HttpHeaders.ACCEPT_CHARSET)).isEqualTo("utf-8");
                    assertThat(expected.getHeader(HttpHeaders.AUTHORIZATION)).isNotEmpty();
                }
        );

    }

    @Test
    @DisplayName("Should throw an exception when requesting info summarized from user " +
                 "and the client not able open connection with server")
    void shouldThrowExceptionWhenNotAbleOpenConnectionWithServer() {
        prepareResponse(response -> {
            response.setHeader("Content-Type", "application/json");
            response.setResponseCode(503);
            response.socketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST);
            response.setBody("Serviço de usuário indisponível");
        });

        assertThrows(RestClientException.class, () -> this.userClientRequest.getUserById(2L),
                "Serviço de usuário indisponível");

        expecOnlyOneRequest();
        expectedRequest(expected -> {
                    assertThat(expected.getMethod()).isEqualTo("GET");
                    assertThat(expected.getPath()).isEqualTo(userClientProperties.getPath().concat("?user_id=2"));
                    assertThat(expected.getHeader(HttpHeaders.ACCEPT)).isEqualTo("application/json");
                    assertThat(expected.getHeader(HttpHeaders.ACCEPT_CHARSET)).isEqualTo("utf-8");
                    assertThat(expected.getHeader(HttpHeaders.AUTHORIZATION)).isNotEmpty();
                }
        );

    }

    @Test
    @DisplayName("Should throw an exception when requesting info " +
                 "summarized from user and he not exists")
    void shouldThrowExceptionWhenRequestingInfoUserAndHimNotToExists() {
        String body = """
                {
                    "timestamp":"2024-12-17T14:58:21.9721648","status":404,"title":"Solicitação não processada",
                    "detail":"{"timestamp":"2024-12-17T14:58:21.9653061","status":404,"title":"Solicitação não encontrada"
                    "detail":"Usuário não encontrado","instance":"/forumhub.io/api/v1/users/summary-info"}"
                    "instance":"/forumhub.io/api/v1/topics/create"
                  }
                """;

        prepareResponse(response -> {
            response.setHeader("Content-Type", "application/json");
            response.setResponseCode(404);
            response.setBody(body);
        });

        assertThrows(RestClientException.class,
                () -> this.userClientRequest.getUserById(5L), body);

        expecOnlyOneRequest();
        expectedRequest(expected -> {
                    assertThat(expected.getMethod()).isEqualTo("GET");
                    assertThat(expected.getPath()).isEqualTo(userClientProperties.getPath().concat("?user_id=5"));
                    assertThat(expected.getHeader(HttpHeaders.ACCEPT)).isEqualTo("application/json");
                    assertThat(expected.getHeader(HttpHeaders.ACCEPT_CHARSET)).isEqualTo("utf-8");
                    assertThat(expected.getHeader(HttpHeaders.AUTHORIZATION)).isNotEmpty();
                }
        );

    }

    @Test
    @DisplayName("Should throw an exception when requesting info " +
                 "summarized from user and some field is null")
    void shouldThrowExceptionWhenRequestingInfoUserAndReturnSomeNullField() {
        prepareResponse(response -> {
            response.setHeader("Content-Type", "application/json");
            response.setResponseCode(200);
            response.setBody("{\"id\":1,\"username\":null,\"email\":\"jose@email.com\"," +
                             "\"profile\":{\"profileName\":\"BASIC\"}}");
        });

        assertThrows(ConstraintViolationException.class, () -> this.userClientRequest.getUserById(2L),
                "O username do usuário não pode ser nulo");

        expecOnlyOneRequest();
        expectedRequest(expected -> {
                    assertThat(expected.getMethod()).isEqualTo("GET");
                    assertThat(expected.getPath()).isEqualTo(userClientProperties.getPath().concat("?user_id=2"));
                    assertThat(expected.getHeader(HttpHeaders.ACCEPT)).isEqualTo("application/json");
                    assertThat(expected.getHeader(HttpHeaders.ACCEPT_CHARSET)).isEqualTo("utf-8");
                    assertThat(expected.getHeader(HttpHeaders.AUTHORIZATION)).isNotEmpty();
                }
        );

    }

    private void prepareResponse(Consumer<MockResponse> consumer) {
        MockResponse mockResponse = new MockResponse();
        consumer.accept(mockResponse);
        userClient.enqueue(mockResponse);

    }


    private void expecOnlyOneRequest() {
        assertThat(userClient.getRequestCount()).isEqualTo(1);
    }


    private void expectedRequest(Consumer<RecordedRequest> consumer) {
        try {
            consumer.accept(this.userClient.takeRequest());
        } catch (InterruptedException ex) {
            throw new IllegalStateException();
        }
    }

}