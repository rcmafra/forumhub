package com.raul.forumhub.topic.integration.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.raul.forumhub.topic.client.UserClientRequest;
import com.raul.forumhub.topic.domain.Answer;
import com.raul.forumhub.topic.domain.Topic;
import com.raul.forumhub.topic.dto.request.AnswerRequestDTO;
import com.raul.forumhub.topic.exception.RestClientException;
import com.raul.forumhub.topic.repository.*;
import com.raul.forumhub.topic.util.TestsHelper;
import org.junit.jupiter.api.*;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles(value = "test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestClassOrder(ClassOrderer.ClassName.class)
@Order(1)
public class AnswerControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    TopicRepository topicRepository;

    @Autowired
    AnswerRepository answerRepository;

    @MockBean
    ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    UserClientRequest userClientRequest;

    private static final Jwt JWT;

    private static boolean hasBeenInitialized = false;

    static {
        JWT = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("user_id", "1")
                .build();
    }

    @BeforeAll
    static void setup(@Autowired ProfileRepository profileRepository,
                      @Autowired AuthorRepository authorRepository,
                      @Autowired CourseRepository courseRepository,
                      @Autowired TopicRepository topicRepository,
                      @Autowired AnswerRepository answerRepository) {
        if (!hasBeenInitialized) {
            profileRepository.saveAll(TestsHelper.ProfileHelper.profileList());
            authorRepository.saveAll(TestsHelper.AuthorHelper.authorList());
            courseRepository.saveAll(TestsHelper.CourseHelper.courseList());
            topicRepository.saveAll(TestsHelper.TopicHelper.topicList());
            answerRepository.saveAll(TestsHelper.AnswerHelper.answerList());
            hasBeenInitialized = true;
        }
    }

    @DisplayName("Should fail with status code 404 if resource doesn't exists")
    @Test
    void shouldFailIfResourceDoesNotExistToTheSendRequest() throws Exception {
        final AnswerRequestDTO answerRequestDTO = new AnswerRequestDTO("Resposta teste");

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/{topic_id}/ans", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerRequestDTO)))
                .andExpect(status().isNotFound());

    }

    @DisplayName("Should fail with status code 400 if method isn't supported")
    @Test
    void shouldFailIfMethodIsNotSupportedToTheSendRequest() throws Exception {
        final AnswerRequestDTO answerRequestDTO = new AnswerRequestDTO("Resposta teste");

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answer", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerRequestDTO)))
                .andExpect(status().isBadRequest());

    }


    @DisplayName("Should fail with status code 401 when answer topic if user unauthenticated")
    @Test
    void shouldFailToAnswerTopicIfUnauthenticated() throws Exception {
        final AnswerRequestDTO answerRequestDTO = new AnswerRequestDTO("Resposta teste");

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/{topic_id}/answer", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerRequestDTO)))
                .andExpect(status().isUnauthorized());

        assertEquals(2, this.topicRepository.findById(1L).orElseThrow().getAnswers().size());

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 400 if solution property is sent empty when answer topic")
    @Test
    void shouldFailIfQuestionPropertyIsEmptyWhenAnswerTopic() throws Exception {
        final AnswerRequestDTO answerRequestDTO = new AnswerRequestDTO("");

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/{topic_id}/answer", 1)
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("A solução não pode ser vazia")));

        Assertions.assertAll(
                () -> assertEquals(4, this.answerRepository.findAll().size()),
                () -> assertEquals(2, this.topicRepository.findById(1L).orElseThrow().getAnswers().size())
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 404 when answer topic if the topic specified not exists")
    @Test
    void shouldFailToAnswerTopicIfSpecifiedTopicNotExists() throws Exception {
        final AnswerRequestDTO answerRequestDTO = new AnswerRequestDTO("Resposta teste");

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/{topic_id}/answer", 6)
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is("O tópico informado não existe")));


        assertFalse(this.topicRepository.findById(6L).isPresent());

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 404 when answer topic if the user service " +
                 "return 404 not found status code")
    @Test
    void shouldFailToAnswerTopicIfUserServiceReturn404StatusCode() throws Exception {
        final AnswerRequestDTO answerRequestDTO = new AnswerRequestDTO("Resposta teste");

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willThrow(new RestClientException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/{topic_id}/answer", 1)
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerRequestDTO)))
                .andExpectAll(status().isNotFound());


        assertEquals(2, this.topicRepository.findById(1L).orElseThrow().getAnswers().size());

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }


    @Transactional
    @DisplayName("Should answer topic with success if user is authenticated and " +
                 "previous premisses are adequate")
    @Test
    void shouldAnswerTopicWithSuccessIfAuthenticated() throws Exception {
        final AnswerRequestDTO answerRequestDTO = new AnswerRequestDTO("Resposta teste");

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/{topic_id}/answer", 1)
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerRequestDTO)))
                .andExpectAll(status().isCreated());

        Answer answer = this.answerRepository.findById(5L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals("Resposta teste", answer.getSolution())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 401 when mark best answer if user unauthenticated")
    @Test
    void shouldFailToMarkBestAnswerIfUnauthenticated() throws Exception {
        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/markBestAnswer",
                        1, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnauthorized());

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(2, topic.getAnswers().size()),
                () -> assertFalse(topic.getAnswers().stream().filter(answer -> answer.getId().equals(1L))
                        .findFirst().orElseThrow().isBestAnswer())
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }

    @DisplayName("Should fail with status code 400 when mark best answer with" +
                 " param different of number type")
    @Test
    void shouldFailToMarkBestAnswerIfParamDifferentOfNumberType() throws Exception {
        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/markBestAnswer",
                        1, "unexpected")
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

    }

    @DisplayName("Should fail with status code 404 when mark best answer if the " +
                 "topic specified not exists")
    @Test
    void shouldFailToMarkBestAnswerIfSpecifiedTopicNotExists() throws Exception {
        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/markBestAnswer",
                        6, 1)
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is("O tópico informado não existe")));

        Assertions.assertAll(
                () -> assertEquals(4, this.answerRepository.findAll().size()),
                () -> assertFalse(this.topicRepository.findById(6L).isPresent())
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 404 when mark best answer if the user service " +
                 "return 404 not found status code")
    @Test
    void shouldFailToMarkBestAnswerIfUserServiceReturn404StatusCode() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(1L)).
                willThrow(new RestClientException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/markBestAnswer",
                        1, 1)
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNotFound());

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(2, topic.getAnswers().size()),
                () -> assertFalse(topic.getAnswers().stream().filter(answer -> answer.getId().equals(1L))
                        .findFirst().orElseThrow().isBestAnswer())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 418 when mark best answer if the " +
                 "authenticated user isn't owner of topic")
    @Test
    void shouldFailToMarkBestAnswerIfAuthenticatedUserIsNotOwnerTopic() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(2L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/markBestAnswer",
                        1, 1)
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isIAmATeapot())
                .andExpect(jsonPath("$.detail",
                        is("O tópico fornecido não pertence ao usuário atualmente logado")));


        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(2, topic.getAnswers().size()),
                () -> assertFalse(topic.getAnswers().stream().filter(answer -> answer.getId().equals(1L))
                        .findFirst().orElseThrow().isBestAnswer())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 418 when mark best answer if " +
                 "yet not exists a answer for specified topic")
    @Test
    void shouldFailToMarkAnswerBestIfYetNotExistsAnswer() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(1L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/markBestAnswer",
                        4, 1)
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isIAmATeapot())
                .andExpect(jsonPath("$.detail",
                        is("Ainda não existe respostas para esse tópico")));


        Topic topic = this.topicRepository.findById(4L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(4, this.answerRepository.findAll().size()),
                () -> assertEquals(0, topic.getAnswers().size())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 422 when mark best answer if already " +
                 "exists a best answer for specified topic")
    @Test
    void shouldFailToMarkBestAnswerIfAlreadyExistsBestAnswer() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(2L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/markBestAnswer",
                        2, 2)
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail",
                        is("Este tópico já possui a resposta [ID: 2] como melhor resposta")));

        Topic topic = this.topicRepository.findById(2L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(1, topic.getAnswers().size()),
                () -> assertTrue(topic.getAnswers().stream().filter(answer -> answer.getId().equals(2L))
                        .findFirst().orElseThrow().isBestAnswer())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @Transactional
    @DisplayName("Should mark best answer with success if user is authenticated and " +
                 "previous premisses are adequate")
    @Test
    void shouldMarkBestAnswerWithSuccessIfAuthenticated() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(1L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/markBestAnswer",
                        1, 1)
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"HttpStatusCode OK\"}"));

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(2, topic.getAnswers().size()),
                () -> assertTrue(topic.getAnswers().stream().filter(answer -> answer.getId().equals(1L))
                        .findFirst().orElseThrow().isBestAnswer())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }

    @DisplayName("Should fail with status code 401 when unmark best answer if user unauthenticated")
    @Test
    void shouldFailToUnmarkBestAnswerIfUnauthenticated() throws Exception {
        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/unmarkBestAnswer",
                        1, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnauthorized());

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(2, topic.getAnswers().size()),
                () -> assertFalse(topic.getAnswers().stream().filter(answer -> answer.getId().equals(1L))
                        .findFirst().orElseThrow().isBestAnswer())
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }

    @DisplayName("Should fail with status code 400 when unmark best answer with" +
                 " param different of number type")
    @Test
    void shouldFailToUnmarkBestAnswerIfParamDifferentOfNumberType() throws Exception {
        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/unmarkBestAnswer",
                        1, "unexpected")
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

    }

    @DisplayName("Should fail with status code 404 when unmark best answer if the " +
                 "topic specified not exists")
    @Test
    void shouldFailToUnmarkBestAnswerIfSpecifiedTopicNotExists() throws Exception {
        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/unmarkBestAnswer",
                        6, 1)
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is("O tópico informado não existe")));

        Assertions.assertAll(
                () -> assertEquals(4, this.answerRepository.findAll().size()),
                () -> assertFalse(this.topicRepository.findById(6L).isPresent())
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }

    @DisplayName("Should fail with status code 404 when unmark best answer if the " +
                 "answer specified not exists")
    @Test
    void shouldFailToUnmarkBestAnswerIfSpecifiedAnswerNotExists() throws Exception {
        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/unmarkBestAnswer",
                        1, 6)
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is("A resposta informada não existe")));

        Assertions.assertAll(
                () -> assertEquals(4, this.answerRepository.findAll().size()),
                () -> assertFalse(this.answerRepository.findById(6L).isPresent())
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 404 when unmark best answer if the user service " +
                 "return 404 not found status code")
    @Test
    void shouldFailToUnmarkBestAnswerIfUserServiceReturn404StatusCode() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(1L)).
                willThrow(new RestClientException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/unmarkBestAnswer",
                        1, 1)
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNotFound());

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(4, this.answerRepository.findAll().size()),
                () -> assertEquals(2, topic.getAnswers().size()),
                () -> assertFalse(topic.getAnswers().stream().filter(answer -> answer.getId().equals(1L))
                        .findFirst().orElseThrow().isBestAnswer())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 418 when unmark best answer if the " +
                 "authenticated user isn't owner of topic")
    @Test
    void shouldFailToUnmarkBestAnswerIfAuthenticatedUserIsNotOwnerTopic() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(2L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/unmarkBestAnswer",
                        1, 1)
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isIAmATeapot())
                .andExpect(jsonPath("$.detail",
                        is("O tópico fornecido não pertence ao usuário atualmente logado")));


        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(2, topic.getAnswers().size()),
                () -> assertFalse(topic.getAnswers().stream().filter(answer -> answer.getId().equals(1L))
                        .findFirst().orElseThrow().isBestAnswer())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 418 when unmark best answer if " +
                 "yet not exists a answer for specified topic")
    @Test
    void shouldFailToUnmarkAnswerBestIfYetNotExistsAnswer() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(1L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/unmarkBestAnswer",
                        4, 1)
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isIAmATeapot())
                .andExpect(jsonPath("$.detail",
                        is("Ainda não existe respostas para esse tópico")));


        Topic topic = this.topicRepository.findById(4L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(4, this.answerRepository.findAll().size()),
                () -> assertEquals(0, topic.getAnswers().size())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 418 when unmark best answer if answer provided " +
                 "not belongs to the topic provided")
    @Test
    void shouldFailToUnmarkBestAnswerIfAnswerProvidedNotBelongsToTheTopicProvided() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(2L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/unmarkBestAnswer",
                        2, 1)
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isIAmATeapot())
                .andExpect(jsonPath("$.detail",
                        is("A resposta fornecida não pertence ao tópico fornecido")));

        Topic topic = this.topicRepository.findById(2L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(1, topic.getAnswers().size()),
                () -> assertTrue(topic.getAnswers().stream().noneMatch(a -> a.getId().equals(1L)))
        );

        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }

    @DisplayName("Should fail with status code 418 when unmark best answer if answer provided " +
                 "isn't as best answer")
    @Test
    void shouldFailToUnmarkBestAnswerIfAnswerProvidedIsNotAsBestAnswer() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(1L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/unmarkBestAnswer",
                        1, 1)
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isIAmATeapot())
                .andExpect(jsonPath("$.detail",
                        is("A resposta fornecida não está definida como melhor resposta")));

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(2, topic.getAnswers().size()),
                () -> assertFalse(topic.getAnswers().stream().allMatch(Answer::isBestAnswer))
        );

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @Transactional
    @DisplayName("Should unmark best answer with success if user is authenticated and " +
                 "previous premisses are adequate")
    @Test
    void shouldUnmarkBestAnswerWithSuccessIfAuthenticated() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(2L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/unmarkBestAnswer",
                        2, 2)
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"HttpStatusCode OK\"}"));

        Topic topic = this.topicRepository.findById(2L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(1, topic.getAnswers().size()),
                () -> assertFalse(topic.getAnswers().stream().filter(answer -> answer.getId().equals(2L))
                        .findFirst().orElseThrow().isBestAnswer())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 403 if user authenticated hasn't authority 'answer:edit'" +
                 "when edit answer")
    @Test
    void shouldFailIfUserHasNotSuitableAuthorityWhenEditAnswer() throws Exception {
        final AnswerRequestDTO answerUpdateDTO =
                new AnswerRequestDTO("Primeiro teste de edição de uma resposta");

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/edit",
                        1, 1)
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerUpdateDTO)))
                .andExpect(status().isForbidden());

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(4, this.answerRepository.findAll().size()),
                () -> assertEquals(2, topic.getAnswers().size()),
                () -> assertEquals("Resposta do primeiro tópico", topic.getAnswers().stream()
                        .filter(answer -> answer.getId().equals(1L)).findFirst().orElseThrow()
                        .getSolution())
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 400 when edit answer with" +
                 " param different of type number")
    @Test
    void shouldFailToEditAnswerIfParamDifferentOfTypeNumber() throws Exception {
        final AnswerRequestDTO answerUpdateDTO =
                new AnswerRequestDTO("Primeiro teste de edição de uma resposta");

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/edit",
                        1, "unexpected")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerUpdateDTO)))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Should fail with status code 400 if solution property is sent empty when edit answer")
    @Test
    void shouldFailIfSolutionPropertyIsEmptyWhenEditAnswer() throws Exception {
        final AnswerRequestDTO answerUpdateDTO = new AnswerRequestDTO("");

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/edit",
                        1, 1)
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerUpdateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("A solução não pode ser vazia")));

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(2, topic.getAnswers().size()),
                () -> assertEquals("Resposta do primeiro tópico", topic.getAnswers().stream()
                        .filter(answer -> answer.getId().equals(1L)).findFirst().orElseThrow()
                        .getSolution())
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }

    @DisplayName("Should fail with status code 404 when update answer if topic not exists")
    @Test
    void shouldFailToEditAnswerIfTopicNotExists() throws Exception {
        final AnswerRequestDTO answerUpdateDTO =
                new AnswerRequestDTO("Primeiro teste de edição de uma resposta");

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/edit",
                        6, 1)
                        .queryParam("answer_id", "1")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerUpdateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail",
                        is("O tópico informado não existe")));

        List<Answer> answer = this.answerRepository.findAll();

        Assertions.assertAll(
                () -> assertFalse(this.topicRepository.findById(6L).isPresent()),
                () -> assertTrue(answer.stream().noneMatch(answer1 -> answer1
                        .getSolution().equals("Primeiro teste de edição de uma resposta")))
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 404 when update answer if answer not exists")
    @Test
    void shouldFailToEditAnswerIfAnswerNotExists() throws Exception {
        final AnswerRequestDTO answerUpdateDTO =
                new AnswerRequestDTO("Primeiro teste de edição de uma resposta");

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/edit",
                        1, 6)
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerUpdateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail",
                        is("A resposta informada não existe")));

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(2, topic.getAnswers().size()),
                () -> assertFalse(topic.getAnswers().stream().anyMatch(answer -> answer.getSolution()
                        .equals("Primeiro teste de edição de uma resposta")))
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 404 when edit answer if the user service return " +
                 "404 not found status code")
    @Test
    void shouldFailToEditAnswerIfUserServiceReturn404StatusCode() throws Exception {
        final AnswerRequestDTO answerUpdateDTO =
                new AnswerRequestDTO("Primeiro teste de edição de uma resposta");

        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willThrow(new RestClientException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/edit",
                        1, 1)
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerUpdateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is("Usuário não encontrado")));

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(4, this.answerRepository.findAll().size()),
                () -> assertEquals(2, topic.getAnswers().size()),
                () -> assertEquals("Resposta do primeiro tópico", topic.getAnswers().stream()
                        .filter(answer -> answer.getId().equals(1L)).findFirst().orElseThrow()
                        .getSolution())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 418 if basic user attempt edit answer of other author")
    @Test
    void shouldFailIfBasicUserAttemptEditAnswerOfOtherAuthor() throws Exception {
        final AnswerRequestDTO answerUpdateDTO =
                new AnswerRequestDTO("Primeiro teste de edição de uma resposta");

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/edit",
                        1, 1)
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerUpdateDTO)))
                .andExpect(status().isIAmATeapot())
                .andExpect(jsonPath("$.detail", is("Usuário com privilégios insuficientes para realizar esta operação!")));

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(2, topic.getAnswers().size()),
                () -> assertEquals("Resposta do primeiro tópico", topic.getAnswers().stream()
                        .filter(answer -> answer.getId().equals(1L)).findFirst().orElseThrow()
                        .getSolution())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 422 when attempt edit a answer of unknown author")
    @Test
    void shouldFailWhenAttemptEditAnswerOfUnknownAuthor() throws Exception {
        final AnswerRequestDTO answerUpdateDTO =
                new AnswerRequestDTO("Primeiro teste de edição de uma resposta");

        BDDMockito.given(this.userClientRequest.getUserById(3L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(2));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/edit",
                        1, 4)
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "3"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerUpdateDTO)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", is(
                        "A resposta pertence a um autor inexistente," +
                        " ela não pode ser editada"
                )));

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(2, topic.getAnswers().size()),
                () -> assertEquals("Resposta do primeiro tópico", topic.getAnswers().stream()
                        .filter(answer -> answer.getId().equals(4L)).findFirst().orElseThrow()
                        .getSolution())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(3L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @Transactional
    @DisplayName("Answer author should be able edit specified answer if authenticated, " +
                 "has authority 'answer:edit' and previous premisses are adequate")
    @Test
    void answerAuthorShouldEditSpecifiedAnswerWithSuccessIfHasSuitableAuthority() throws Exception {
        final AnswerRequestDTO answerUpdateDTO =
                new AnswerRequestDTO("Primeiro teste de edição de uma resposta");

        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/edit",
                        1, 1)
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer.solution",
                        is("Primeiro teste de edição de uma resposta")));


        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(2, topic.getAnswers().size()),
                () -> assertEquals("Primeiro teste de edição de uma resposta", topic.getAnswers().stream()
                        .filter(answer -> answer.getId().equals(1L)).findFirst().orElseThrow()
                        .getSolution())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @Transactional
    @DisplayName("User ADM should be able edit answer of other author if authenticated, " +
                 "has authority 'answer:edit' and previous premisses are adequate")
    @Test
    void userADMShouldEditAnswerOfOtherAuthorWithSuccessIfHasSuitableAuthority() throws Exception {
        final AnswerRequestDTO answerUpdateDTO =
                new AnswerRequestDTO("Segundo teste de edição de uma resposta");

        BDDMockito.given(this.userClientRequest.getUserById(3L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(2));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/edit",
                        1, 1)
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "3"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer.solution",
                        is("Segundo teste de edição de uma resposta")));

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(2, topic.getAnswers().size()),
                () -> assertEquals("Segundo teste de edição de uma resposta", topic.getAnswers().stream()
                        .filter(answer -> answer.getId().equals(1L)).findFirst().orElseThrow()
                        .getSolution())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(3L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @Transactional
    @DisplayName("User MOD should be able edit answer of other author if authenticated, " +
                 "has authority 'answer:edit' and previous premisses are adequate")
    @Test
    void userMODShouldEditAnswerOfOtherAuthorWithSuccessIfHasSuitableAuthority() throws Exception {
        final AnswerRequestDTO answerUpdateDTO =
                new AnswerRequestDTO("Terceiro teste de edição de uma resposta");

        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/edit",
                        3, 3)
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer.solution",
                        is("Terceiro teste de edição de uma resposta")));

        Topic topic = this.topicRepository.findById(3L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(1, topic.getAnswers().size()),
                () -> assertEquals("Terceiro teste de edição de uma resposta", topic.getAnswers().stream()
                        .filter(answer -> answer.getId().equals(3L)).findFirst().orElseThrow()
                        .getSolution())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 403 if user authenticated hasn't authority 'answer:delete'" +
                 " when delete answer")
    @Test
    void shouldFailIfUserHasNotSuitableAuthorityWhenDeleteAnswer() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/delete",
                        1, 1)
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(2, topic.getAnswers().size()),
                () -> assertTrue(topic.getAnswers().stream().anyMatch(answer -> answer.getId().equals(1L)))

        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }

    @DisplayName("Should fail with status code 400 when delete answer with" +
                 " param different of type number")
    @Test
    void shouldFailToDeleteTopicIfParamDifferentOfTypeNumber() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/delete",
                        1, "unexpected")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());
    }


    @DisplayName("Should fail with status code 404 when delete answer if the user service " +
                 "return 404 not found status code")
    @Test
    void shouldFailToDeleteAnswerIfUserServiceReturn404StatusCode() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willThrow(new RestClientException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        this.mockMvc.perform(delete("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/delete",
                        1, 1)
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNotFound());

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(2, topic.getAnswers().size()),
                () -> assertTrue(topic.getAnswers().stream().anyMatch(answer -> answer.getId().equals(1L)))

        );

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 422 if provided answer not belonging to the " +
                 "provided topic when delete answer")
    @Test
    void shouldFailIfProvidedAnswerNotBelongingToTheProvidedTopicWhenDeleteAnswer() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(delete("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/delete",
                        1, 2)
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail",
                        is("A resposta fornecida não pertence a esse tópico")));

        Topic topic = this.topicRepository.findById(1L).orElseThrow();
        Long[] answersId = {1L, 4L, 5L};

        Assertions.assertAll(
                () -> assertEquals(2, topic.getAnswers().size()),
                () -> containsInAnyOrder(answersId, topic.getAnswers().stream().map(Answer::getId).toArray())

        );

        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
    }


    @DisplayName("Should fail with status code 418 if basic user attempt delete answer of other author")
    @Test
    void shouldFailIfBasicUserAttemptDeleteAnswerOfOtherAuthor() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(delete("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/delete",
                        1, 1)
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isIAmATeapot())
                .andExpect(jsonPath("$.detail", is("Usuário com privilégios insuficientes para realizar esta operação!")));

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(2, topic.getAnswers().size()),
                () -> assertTrue(topic.getAnswers().stream().anyMatch(answer -> answer.getId().equals(1L)))

        );

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @Transactional
    @DisplayName("Answer author should be able delete your answer if authenticated, " +
                 "has authority 'answer:delete' and previous premisses are adequate")
    @Test
    void answerAuthorShouldDeleteYourAnswerWithSuccessIfHasSuitableAuthority() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        this.mockMvc.perform(delete("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/delete",
                        1, 1)
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"HttpStatusCode OK\"}"));

        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @Transactional
    @DisplayName("User ADM should be able delete an answer of the other author " +
                 "with successful if has authority 'answer:delete'")
    @Test
    void userADMShouldDeleteAnAnswerOfTheOtherAuthorWithSuccessful() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(3L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(2));

        this.mockMvc.perform(delete("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/delete",
                        2, 2)
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "3"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"HttpStatusCode OK\"}"));

        BDDMockito.verify(this.userClientRequest).getUserById(3L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @Transactional
    @DisplayName("User MOD should be able delete an answer of the other author " +
                 "with successful if has authority 'answer:delete'")
    @Test
    void userMODShouldDeleteAnAnswerOfTheOtherAuthorWithSuccessful() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        this.mockMvc.perform(delete("/forumhub.io/api/v1/topics/{topic_id}/answers/{answer_id}/delete",
                        2, 2)
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"HttpStatusCode OK\"}"));

        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


}
