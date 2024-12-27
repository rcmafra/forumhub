package com.raul.forumhub.topic.integration.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.raul.forumhub.topic.client.UserClientRequest;
import com.raul.forumhub.topic.domain.Answer;
import com.raul.forumhub.topic.domain.Topic;
import com.raul.forumhub.topic.dto.request.AnswerTopicDTO;
import com.raul.forumhub.topic.dto.request.AnswerUpdateDTO;
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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

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

    @Autowired
    AuthorRepository authorRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    ProfileRepository profileRepository;

    @MockBean
    ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    UserClientRequest userClientRequest;

    private static final Jwt jwt;

    private static boolean hasBeenInitialized = false;

    static {
        jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("user_id", "1")
                .build();
    }

    @BeforeEach
    void setup() {
        if (!hasBeenInitialized) {
            this.profileRepository.saveAll(TestsHelper.ProfileHelper.profileList());
            this.authorRepository.saveAll(TestsHelper.AuthorHelper.authorList());
            this.courseRepository.saveAll(TestsHelper.CourseHelper.courseList());
            this.topicRepository.saveAll(TestsHelper.TopicHelper.topicList());
            this.answerRepository.saveAll(TestsHelper.AnswerHelper.answerList());
            hasBeenInitialized = true;
        }
    }


    @Order(1)
    @DisplayName("Should fail with status code 401 when answer topic if user unauthenticated")
    @Test
    void shouldFailToAnswerTopicIfUnauthenticated() throws Exception {
        final AnswerTopicDTO answerTopicDTO = new AnswerTopicDTO("Resposta teste");

        this.mockMvc.perform(post("/api-forum/v1/forumhub/topics/1/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerTopicDTO)))
                .andExpect(status().isUnauthorized());

        Assertions.assertAll(
                () -> assertEquals(4, this.answerRepository.findAll().size()),
                () -> assertEquals(2, this.topicRepository.findById(1L).orElseThrow().getAnswers().size())
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }

    @Order(2)
    @DisplayName("Should fail with status code 400 if solution property is sent empty when answer topic")
    @Test
    void shouldFailIfQuestionPropertyIsEmptyWhenAnswerTopic() throws Exception {
        final AnswerTopicDTO answerTopicDTO = new AnswerTopicDTO("");

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(post("/api-forum/v1/forumhub/topics/1/answer")
                        .with(jwt().jwt(jwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerTopicDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("A solução não pode ser vazia")));

        Assertions.assertAll(
                () -> assertEquals(4, this.answerRepository.findAll().size()),
                () -> assertEquals(2, this.topicRepository.findById(1L).orElseThrow().getAnswers().size())
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }

    @Order(3)
    @DisplayName("Should fail with status code 404 when answer topic if the topic specified not exists")
    @Test
    void shouldFailToAnswerTopicIfSpecifiedTopicNotExists() throws Exception {
        final AnswerTopicDTO answerTopicDTO = new AnswerTopicDTO("Resposta teste");

        this.mockMvc.perform(post("/api-forum/v1/forumhub/topics/6/answer")
                        .with(jwt().jwt(jwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerTopicDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is("O tópico informado não existe")));

        Assertions.assertAll(
                () -> assertEquals(4, this.answerRepository.findAll().size()),
                () -> assertFalse(this.topicRepository.findById(6L).isPresent())
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }


    @Order(4)
    @DisplayName("Should fail with status code 404 when answer topic if the user service " +
            "return 404 not found status code")
    @Test
    void shouldFailToAnswerTopicIfUserServiceReturn404StatusCode() throws Exception {
        final AnswerTopicDTO answerTopicDTO = new AnswerTopicDTO("Resposta teste");

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willThrow(new RestClientException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        this.mockMvc.perform(post("/api-forum/v1/forumhub/topics/1/answer")
                        .with(jwt().jwt(jwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerTopicDTO)))
                .andExpectAll(status().isNotFound());

        Assertions.assertAll(
                () -> assertEquals(4, this.answerRepository.findAll().size()),
                () -> assertEquals(2, this.topicRepository.findById(1L).orElseThrow().getAnswers().size())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }

    @Order(5)
    @DisplayName("Should answer topic with success if user is authenticated and " +
            "previous premisses are adequate")
    @Test
    void shouldAnswerTopicWithSuccessIfAuthenticated() throws Exception {
        final AnswerTopicDTO answerTopicDTO = new AnswerTopicDTO("Resposta teste");

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(post("/api-forum/v1/forumhub/topics/1/answer")
                        .with(jwt().jwt(jwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerTopicDTO)))
                .andExpectAll(status().isCreated());

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals(3, topic.getAnswers().size()),
                () -> assertTrue(topic.getAnswers().stream().anyMatch(answer -> answer.getSolution()
                        .equals("Resposta teste")))
        );

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }

    @Order(6)
    @DisplayName("Should fail with status code 401 when mark answer best if user unauthenticated")
    @Test
    void shouldFailToMarkAnswerBestIfUnauthenticated() throws Exception {
        this.mockMvc.perform(post("/api-forum/v1/forumhub/topics/1/markBestAnswer")
                        .queryParam("answer_id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnauthorized());

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals(3, topic.getAnswers().size()),
                () -> assertFalse(topic.getAnswers().stream().filter(answer -> answer.getId().equals(1L))
                        .findFirst().orElseThrow().isBestAnswer())
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }

    @Order(7)
    @DisplayName("Should fail with status code 400 when attempt mark answer best if " +
            "answer_id property of query param is sent empty")
    @Test
    void shouldFailIfAnswerIdPropertyOfQueryParamIsEmptyWhenMarkAnswerBest() throws Exception {
        this.mockMvc.perform(post("/api-forum/v1/forumhub/topics/1/markBestAnswer")
                        .queryParam("answer_id", "")
                        .with(jwt().jwt(jwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals(3, topic.getAnswers().size()),
                () -> assertFalse(topic.getAnswers().stream().filter(answer -> answer.getId().equals(1L))
                        .findFirst().orElseThrow().isBestAnswer())
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }

    @Order(8)
    @DisplayName("Should fail with status code 404 when mark answer best if the " +
            "topic specified not exists")
    @Test
    void shouldFailToMarkAnswerBestIfSpecifiedTopicNotExists() throws Exception {
        this.mockMvc.perform(post("/api-forum/v1/forumhub/topics/6/markBestAnswer")
                        .queryParam("answer_id", "1")
                        .with(jwt().jwt(jwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is("O tópico informado não existe")));

        Assertions.assertAll(
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertFalse(this.topicRepository.findById(6L).isPresent())
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }

    @Order(9)
    @DisplayName("Should fail with status code 404 when mark answer best if the user service " +
            "return 404 not found status code")
    @Test
    void shouldFailToMarkAnswerBestIfUserServiceReturn404StatusCode() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(1L)).
                willThrow(new RestClientException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        this.mockMvc.perform(post("/api-forum/v1/forumhub/topics/1/markBestAnswer")
                        .queryParam("answer_id", "1")
                        .with(jwt().jwt(jwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNotFound());

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals(3, topic.getAnswers().size()),
                () -> assertFalse(topic.getAnswers().stream().filter(answer -> answer.getId().equals(1L))
                        .findFirst().orElseThrow().isBestAnswer())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }

    @Order(10)
    @DisplayName("Should fail with status code 422 when mark answer best if the " +
            "authenticated user isn't owner of topic")
    @Test
    void shouldFailToMarkAnswerBestIfAuthenticatedUserIsNotOwnerTopic() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(2L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        this.mockMvc.perform(post("/api-forum/v1/forumhub/topics/1/markBestAnswer")
                        .queryParam("answer_id", "1")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail",
                        is("O tópico fornecido não pertence a esse autor")));


        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals(3, topic.getAnswers().size()),
                () -> assertFalse(topic.getAnswers().stream().filter(answer -> answer.getId().equals(1L))
                        .findFirst().orElseThrow().isBestAnswer())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }

    @Order(11)
    @DisplayName("Should fail with status code 422 when mark answer best if " +
            "yet not exists a answer for specified topic")
    @Test
    void shouldFailToMarkAnswerBestIfYetNotExistsAnswer() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(1L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(post("/api-forum/v1/forumhub/topics/4/markBestAnswer")
                        .queryParam("answer_id", "1")
                        .with(jwt().jwt(jwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail",
                        is("Ainda não existe respostas para esse tópico")));


        Topic topic = this.topicRepository.findById(4L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals(0, topic.getAnswers().size())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @Order(12)
    @DisplayName("Should fail with status code 422 when mark answer best if already " +
            "exists a best answer for specified topic")
    @Test
    void shouldFailToMarkAnswerBestIfAlreadyExistsBestAnswer() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(2L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        this.mockMvc.perform(post("/api-forum/v1/forumhub/topics/2/markBestAnswer")
                        .queryParam("answer_id", "2")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail",
                        is("Este tópico já possui a resposta [ID: 2] como melhor resposta")));

        Topic topic = this.topicRepository.findById(2L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals(1, topic.getAnswers().size()),
                () -> assertTrue(topic.getAnswers().stream().filter(answer -> answer.getId().equals(2L))
                        .findFirst().orElseThrow().isBestAnswer())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }

    @Order(13)
    @DisplayName("Should mark answer best with success if user is authenticated and " +
            "previous premisses are adequate")
    @Test
    void shouldMarkAnswerBestWithSuccessIfAuthenticated() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(1L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(post("/api-forum/v1/forumhub/topics/1/markBestAnswer")
                        .queryParam("answer_id", "1")
                        .with(jwt().jwt(jwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"HttpStatusCode OK\"}"));

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals(3, topic.getAnswers().size()),
                () -> assertTrue(topic.getAnswers().stream().filter(answer -> answer.getId().equals(1L))
                        .findFirst().orElseThrow().isBestAnswer())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @Order(14)
    @DisplayName("Should fail with status code 403 if user authenticated hasn't authority 'answer:edit'" +
            "when edit answer")
    @Test
    void shouldFailIfUserHasNotSuitableAuthorityWhenEditAnswer() throws Exception {
        final AnswerUpdateDTO answerUpdateDTO =
                new AnswerUpdateDTO("Primeiro teste de edição de uma resposta");

        this.mockMvc.perform(put("/api-forum/v1/forumhub/topics/1/answers")
                        .queryParam("answer_id", "1")
                        .with(jwt().jwt(jwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerUpdateDTO)))
                .andExpect(status().isForbidden());

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals(3, topic.getAnswers().size()),
                () -> assertEquals("Resposta do primeiro tópico", topic.getAnswers().stream()
                        .filter(answer -> answer.getId().equals(1L)).findFirst().orElseThrow()
                        .getSolution())
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }

    @Order(15)
    @DisplayName("Should fail with status code 400 if solution property is sent empty when edit answer")
    @Test
    void shouldFailIfSolutionPropertyIsEmptyWhenEditAnswer() throws Exception {
        final AnswerUpdateDTO answerUpdateDTO = new AnswerUpdateDTO("");

        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        this.mockMvc.perform(put("/api-forum/v1/forumhub/topics/1/answers")
                        .queryParam("answer_id", "1")
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
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals(3, topic.getAnswers().size()),
                () -> assertEquals("Resposta do primeiro tópico", topic.getAnswers().stream()
                        .filter(answer -> answer.getId().equals(1L)).findFirst().orElseThrow()
                        .getSolution())
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }

    @Order(16)
    @DisplayName("Should fail with status code 400 when attempt update answer if answer_id property " +
            "of query param is sent empty")
    @Test
    void shouldFailIfAnswerIdPropertyOfQueryParamIsEmptyWhenUpdateAnswer() throws Exception {
        final AnswerUpdateDTO answerUpdateDTO =
                new AnswerUpdateDTO("Primeiro teste de edição de uma resposta");

        this.mockMvc.perform(put("/api-forum/v1/forumhub/topics/1/answers")
                        .queryParam("answer_id", "")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerUpdateDTO)))
                .andExpect(status().isBadRequest());

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals(3, topic.getAnswers().size()),
                () -> assertFalse(topic.getAnswers().stream().anyMatch(answer -> answer.getSolution()
                        .equals("Primeiro teste de edição de uma resposta")))
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }

    @Order(17)
    @DisplayName("Should fail with status code 404 when update answer if topic not exists")
    @Test
    void shouldFailToEditAnswerIfTopicNotExists() throws Exception {
        final AnswerUpdateDTO answerUpdateDTO =
                new AnswerUpdateDTO("Primeiro teste de edição de uma resposta");

        this.mockMvc.perform(put("/api-forum/v1/forumhub/topics/6/answers")
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
                () -> assertEquals(5, answer.size()),
                () -> assertTrue(answer.stream().noneMatch(answer1 -> answer1
                        .getSolution().equals("Primeiro teste de edição de uma resposta")))
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }

    @Order(18)
    @DisplayName("Should fail with status code 404 when update answer if answer not exists")
    @Test
    void shouldFailToEditAnswerIfAnswerNotExists() throws Exception {
        final AnswerUpdateDTO answerUpdateDTO =
                new AnswerUpdateDTO("Primeiro teste de edição de uma resposta");

        this.mockMvc.perform(put("/api-forum/v1/forumhub/topics/1/answers")
                        .queryParam("answer_id", "6")
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
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals(3, topic.getAnswers().size()),
                () -> assertFalse(topic.getAnswers().stream().anyMatch(answer -> answer.getSolution()
                        .equals("Primeiro teste de edição de uma resposta")))
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }


    @Order(19)
    @DisplayName("Should fail with status code 404 when edit answer if the user service return " +
            "404 not found status code")
    @Test
    void shouldFailToEditAnswerIfUserServiceReturn404StatusCode() throws Exception {
        final AnswerUpdateDTO answerUpdateDTO =
                new AnswerUpdateDTO("Primeiro teste de edição de uma resposta");

        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willThrow(new RestClientException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        this.mockMvc.perform(put("/api-forum/v1/forumhub/topics/1/answers")
                        .queryParam("answer_id", "1")
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
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals(3, topic.getAnswers().size()),
                () -> assertEquals("Resposta do primeiro tópico", topic.getAnswers().stream()
                        .filter(answer -> answer.getId().equals(1L)).findFirst().orElseThrow()
                        .getSolution())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }

    @Order(20)
    @DisplayName("Should fail with status code 418 if basic user attempt edit answer of other author")
    @Test
    void shouldFailIfBasicUserAttemptEditAnswerOfOtherAuthor() throws Exception {
        final AnswerUpdateDTO answerUpdateDTO =
                new AnswerUpdateDTO("Primeiro teste de edição de uma resposta");

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(put("/api-forum/v1/forumhub/topics/1/answers")
                        .queryParam("answer_id", "1")
                        .with(jwt().jwt(jwt)
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerUpdateDTO)))
                .andExpect(status().isIAmATeapot())
                .andExpect(jsonPath("$.detail", is("Privilégio insuficiente")));

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals(3, topic.getAnswers().size()),
                () -> assertEquals("Resposta do primeiro tópico", topic.getAnswers().stream()
                        .filter(answer -> answer.getId().equals(1L)).findFirst().orElseThrow()
                        .getSolution())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }

    @Order(21)
    @DisplayName("Should fail with status code 422 when attempt edit a answer of unknown author")
    @Test
    void shouldFailWhenAttemptEditAnswerOfUnknownAuthor() throws Exception {
        final AnswerUpdateDTO answerUpdateDTO =
                new AnswerUpdateDTO("Primeiro teste de edição de uma resposta");

        BDDMockito.given(this.userClientRequest.getUserById(3L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(2));

        this.mockMvc.perform(put("/api-forum/v1/forumhub/topics/1/answers")
                        .queryParam("answer_id", "4")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "3"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(answerUpdateDTO)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", is(
                        "O tópico pertence a um autor inexistente," +
                                " ele não pode ser editado"
                )));

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals(3, topic.getAnswers().size()),
                () -> assertEquals("Resposta do primeiro tópico", topic.getAnswers().stream()
                        .filter(answer -> answer.getId().equals(4L)).findFirst().orElseThrow()
                        .getSolution())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(3L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }

    @Order(22)
    @DisplayName("Answer author should be able edit specified answer if authenticated, " +
            "has authority 'answer:edit' and previous premisses are adequate")
    @Test
    void answerAuthorShouldEditSpecifiedAnswerWithSuccessIfHasSuitableAuthority() throws Exception {
        final AnswerUpdateDTO answerUpdateDTO =
                new AnswerUpdateDTO("Primeiro teste de edição de uma resposta");

        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        this.mockMvc.perform(put("/api-forum/v1/forumhub/topics/1/answers")
                        .queryParam("answer_id", "1")
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
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals(3, topic.getAnswers().size()),
                () -> assertEquals("Primeiro teste de edição de uma resposta", topic.getAnswers().stream()
                        .filter(answer -> answer.getId().equals(1L)).findFirst().orElseThrow()
                        .getSolution())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }

    @Order(23)
    @DisplayName("User ADM should be able edit answer of other author if authenticated, " +
            "has authority 'answer:edit' and previous premisses are adequate")
    @Test
    void userADMShouldEditAnswerOfOtherAuthorWithSuccessIfHasSuitableAuthority() throws Exception {
        final AnswerUpdateDTO answerUpdateDTO =
                new AnswerUpdateDTO("Segundo teste de edição de uma resposta");

        BDDMockito.given(this.userClientRequest.getUserById(3L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(2));

        this.mockMvc.perform(put("/api-forum/v1/forumhub/topics/1/answers")
                        .queryParam("answer_id", "1")
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
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals(3, topic.getAnswers().size()),
                () -> assertEquals("Segundo teste de edição de uma resposta", topic.getAnswers().stream()
                        .filter(answer -> answer.getId().equals(1L)).findFirst().orElseThrow()
                        .getSolution())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(3L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }

    @Order(24)
    @DisplayName("User MOD should be able edit answer of other author if authenticated, " +
            "has authority 'answer:edit' and previous premisses are adequate")
    @Test
    void userMODShouldEditAnswerOfOtherAuthorWithSuccessIfHasSuitableAuthority() throws Exception {
        final AnswerUpdateDTO answerUpdateDTO =
                new AnswerUpdateDTO("Terceiro teste de edição de uma resposta");

        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        this.mockMvc.perform(put("/api-forum/v1/forumhub/topics/3/answers")
                        .queryParam("answer_id", "3")
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
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals(1, topic.getAnswers().size()),
                () -> assertEquals("Terceiro teste de edição de uma resposta", topic.getAnswers().stream()
                        .filter(answer -> answer.getId().equals(3L)).findFirst().orElseThrow()
                        .getSolution())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @Order(25)
    @DisplayName("Should fail with status code 403 if user authenticated hasn't authority 'answer:delete'" +
            " when delete answer")
    @Test
    void shouldFailIfUserHasNotSuitableAuthorityWhenDeleteAnswer() throws Exception {
        this.mockMvc.perform(delete("/api-forum/v1/forumhub/topics/1/answers/delete")
                        .queryParam("answer_id", "1")
                        .with(jwt().jwt(jwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals(3, topic.getAnswers().size()),
                () -> assertTrue(topic.getAnswers().stream().anyMatch(answer -> answer.getId().equals(1L)))

        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }

    @Order(26)
    @DisplayName("Should fail with status code 400 when attempt delete answer if answer_id property " +
            "of query param is sent empty")
    @Test
    void shouldFailIfAnswerIdPropertyOfQueryParamIsEmptyWhenDeleteAnswer() throws Exception {
        this.mockMvc.perform(delete("/api-forum/v1/forumhub/topics/1/answers/delete")
                        .queryParam("answer_id", "")
                        .with(jwt().jwt(jwt)
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals(3, topic.getAnswers().size()),
                () -> assertTrue(topic.getAnswers().stream().anyMatch(answer -> answer.getId().equals(1L)))

        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }

    @Order(27)
    @DisplayName("Should fail with status code 404 when delete answer if the user service " +
            "return 404 not found status code")
    @Test
    void shouldFailToDeleteAnswerIfUserServiceReturn404StatusCode() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willThrow(new RestClientException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        this.mockMvc.perform(delete("/api-forum/v1/forumhub/topics/1/answers/delete")
                        .queryParam("answer_id", "1")
                        .with(jwt().jwt(jwt)
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNotFound());

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals(3, topic.getAnswers().size()),
                () -> assertTrue(topic.getAnswers().stream().anyMatch(answer -> answer.getId().equals(1L)))

        );

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }

    @Order(28)
    @DisplayName("Should fail with status code 422 if provided answer not belonging to the " +
            "provided topic when delete answer")
    @Test
    void shouldFailIfProvidedAnswerNotBelongingToTheProvidedTopicWhenDeleteAnswer() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(delete("/api-forum/v1/forumhub/topics/1/answers/delete")
                        .queryParam("answer_id", "2")
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
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals(3, topic.getAnswers().size()),
                () -> containsInAnyOrder(answersId, topic.getAnswers().stream().map(Answer::getId).toArray())

        );

        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
    }

    @Order(29)
    @DisplayName("Should fail with status code 418 if basic user attempt delete answer of other author")
    @Test
    void shouldFailIfBasicUserAttemptDeleteAnswerOfOtherAuthor() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(delete("/api-forum/v1/forumhub/topics/1/answers/delete")
                        .queryParam("answer_id", "1")
                        .with(jwt().jwt(jwt)
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isIAmATeapot())
                .andExpect(jsonPath("$.detail", is("Privilégio insuficiente")));

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        Assertions.assertAll(
                () -> assertEquals(5, this.answerRepository.findAll().size()),
                () -> assertEquals(3, topic.getAnswers().size()),
                () -> assertTrue(topic.getAnswers().stream().anyMatch(answer -> answer.getId().equals(1L)))

        );

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }

    @Order(30)
    @DisplayName("Answer author should be able delete specified answer if authenticated, " +
            "has authority 'answer:delete' and previous premisses are adequate")
    @Test
    void answerAuthorShouldDeleteSpecifiedAnswerWithSuccessIfHasSuitableAuthority() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        this.mockMvc.perform(delete("/api-forum/v1/forumhub/topics/1/answers/delete")
                        .queryParam("answer_id", "1")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"HttpStatusCode OK\"}"));

        Set<Answer> answers = this.topicRepository.findById(1L).orElseThrow().getAnswers();

        Assertions.assertAll(
                () -> assertEquals(4, this.answerRepository.findAll().size()),
                () -> assertEquals(2, answers.size()),
                () -> assertFalse(answers.stream().anyMatch(answer -> answer.getId().equals(1L)))
        );

        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }

    @Order(31)
    @DisplayName("User ADM should be able delete topic of other author if authenticated, " +
            "has authority 'topic:delete' and previous premisses are adequate")
    @Test
    void userADMShouldDeleteTopicOfOtherAuthorWithSuccessIfHasSuitableAuthority() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(3L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(2));

        this.mockMvc.perform(delete("/api-forum/v1/forumhub/topics/1/answers/delete")
                        .queryParam("answer_id", "4")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "3"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"HttpStatusCode OK\"}"));

        Set<Answer> answers = this.topicRepository.findById(1L).orElseThrow().getAnswers();

        Assertions.assertAll(
                () -> assertEquals(3, this.answerRepository.findAll().size()),
                () -> assertEquals(1, answers.size()),
                () -> assertFalse(answers.stream().anyMatch(answer -> answer.getId().equals(4L)))
        );

        BDDMockito.verify(this.userClientRequest).getUserById(3L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }

    @Order(32)
    @DisplayName("User MOD should be able delete topic of other author if authenticated, " +
            "has authority 'topic:delete' and previous premisses are adequate")
    @Test
    void userMODShouldDeleteTopicOfOtherAuthorWithSuccessIfHasSuitableAuthority() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        this.mockMvc.perform(delete("/api-forum/v1/forumhub/topics/3/answers/delete")
                        .queryParam("answer_id", "3")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_answer:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"HttpStatusCode OK\"}"));

        Set<Answer> answers = this.topicRepository.findById(3L).orElseThrow().getAnswers();

        Assertions.assertAll(
                () -> assertEquals(2, this.answerRepository.findAll().size()),
                () -> assertEquals(0, answers.size())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


}
