package com.raul.forumhub.topic.integration.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.raul.forumhub.topic.client.UserClientRequest;
import com.raul.forumhub.topic.domain.Status;
import com.raul.forumhub.topic.domain.Topic;
import com.raul.forumhub.topic.dto.request.TopicCreateDTO;
import com.raul.forumhub.topic.dto.request.TopicUpdateDTO;
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

import static org.hamcrest.MatcherAssert.assertThat;
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
@Order(2)
public class TopicControllerIT {

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

    private static final Jwt JWT;

    private static boolean hasBeenInitialized = false;

    static {
        JWT = Jwt.withTokenValue("token")
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


    @DisplayName("Should fail with status code 404 if resource doesn't exists")
    @Test
    void shouldFailIfResourceDoesNotExistToTheSendRequest() throws Exception {
        final TopicCreateDTO topicCreateDTO = new TopicCreateDTO("Dúvida na utilização do Feign Client",
                "Como utilizar o Feign Client para integração do serviço x?",
                1L);

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/creat")
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicCreateDTO)))
                .andExpect(status().isNotFound());

    }

    @DisplayName("Should fail with status code 400 if method isn't supported")
    @Test
    void shouldFailIfMethodIsNotSupportedToTheSendRequest() throws Exception {
        final TopicCreateDTO topicCreateDTO = new TopicCreateDTO("Dúvida na utilização do Feign Client",
                "Como utilizar o Feign Client para integração do serviço x?",
                1L);

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/create")
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicCreateDTO)))
                .andExpect(status().isBadRequest());

    }


    @DisplayName("Should fail with status code 401 when create topic if user unauthenticated")
    @Test
    void shouldFailToCreateTopicIfUnauthenticated() throws Exception {
        final TopicCreateDTO topicCreateDTO = new TopicCreateDTO("Dúvida na utilização do Feign Client",
                "Como utilizar o Feign Client para integração do serviço x?",
                1L);

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicCreateDTO)))
                .andExpect(status().isUnauthorized());

        Assertions.assertEquals(4, this.topicRepository.findAll().size());

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 400 if title property is sent empty when create topic")
    @Test
    void shouldFailIfTitlePropertyIsEmptyWhenCreateTopic() throws Exception {
        final TopicCreateDTO topicCreateDTO = new TopicCreateDTO("",
                "Como utilizar o Feign Client para integração do serviço x?",
                1L);

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/create")
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("O título não pode ser vazio")));

        Assertions.assertEquals(4, this.topicRepository.findAll().size());

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 400 if question property is sent empty when create topic")
    @Test
    void shouldFailIfQuestionPropertyIsEmptyWhenCreateTopic() throws Exception {
        final TopicCreateDTO topicCreateDTO = new TopicCreateDTO("Dúvida na utilização do Feign Client",
                "",
                1L);

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/create")
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("A pergunta não pode ser vazia")));

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 400 when create topic if the title " +
            "property is greater than 150 chars")
    @Test
    void shouldFailToCreateTopicIfTitlePropertyExceedsLimit() throws Exception {
        final TopicCreateDTO topicCreateDTO = new TopicCreateDTO(
                "Qual é a diferença entre o Feign Client, RestTemplate e o WebClient no " +
                        "Spring Framework e em que situações é mais adequado utilizá-los durante a " +
                        "integração de um serviço?",
                "Diferença entre o Feign Client, RestTemplate e WebClient",
                1L);

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/create")
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("Payload com valor muito grande")));

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 404 when create topic if the course not exists")
    @Test
    void shouldFailToCreateTopicIfCourseNotExists() throws Exception {
        final TopicCreateDTO topicCreateDTO = new TopicCreateDTO("Dúvida na utilização do Feign Client",
                "Como utilizar o Feign Client para integração do serviço x?",
                4L);

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/create")
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicCreateDTO)))
                .andExpect(status().isNotFound());

        Assertions.assertEquals(4, this.topicRepository.findAll().size());

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 404 when create topic if the user service " +
            "return 404 not found status code")
    @Test
    void shouldFailToCreateTopicIfUserServiceReturn404StatusCode() throws Exception {
        final TopicCreateDTO topicCreateDTO = new TopicCreateDTO("Dúvida na utilização do Feign Client",
                "Como utilizar o Feign Client para integração do serviço x?",
                1L);

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willThrow(new RestClientException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/create")
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicCreateDTO)))
                .andExpectAll(status().isNotFound());

        Assertions.assertEquals(4, this.topicRepository.findAll().size());

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @Transactional
    @DisplayName("Should create topic with success if user is authenticated and " +
            "previous premisses are adequate")
    @Test
    void shouldCreateTopicWithSuccessIfAuthenticated() throws Exception {
        final TopicCreateDTO topicCreateDTO = new TopicCreateDTO("Dúvida na utilização do Feign Client",
                "Como utilizar o Feign Client para integração do serviço x?",
                1L);

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/create")
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicCreateDTO)))
                .andExpectAll(status().isCreated());

        Assertions.assertEquals(5, this.topicRepository.findAll().size());

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @DisplayName("Should return all topics unsorted with successful")
    @Test
    void shouldReturnAllTopicsUnsortedWithSuccessful() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/topics/listAll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..getTopicDTOList.length()", is(4)))
                .andExpect(jsonPath("$..page.[?(@.size == 10)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 4)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        Assertions.assertEquals(4, this.topicRepository.findAll().size());


    }


    @DisplayName("Should return all topics sorted descendants by created date with successful")
    @Test
    void shouldReturnAllTopicsSortedDescendantByCreateDateWithSuccessful() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/topics/listAll")
                        .queryParam("sort", "createdAt,desc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..getTopicDTOList[0].[?(@.topic.id == 3)]").exists())
                .andExpect(jsonPath("$..getTopicDTOList[1].[?(@.topic.id == 1)]").exists())
                .andExpect(jsonPath("$..getTopicDTOList[2].[?(@.topic.id == 2)]").exists())
                .andExpect(jsonPath("$..getTopicDTOList[3].[?(@.topic.id == 4)]").exists())
                .andExpect(jsonPath("$..getTopicDTOList.length()", is(4)))
                .andExpect(jsonPath("$..page.[?(@.size == 10)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 4)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        Assertions.assertEquals(4, this.topicRepository.findAll().size());


    }


    @DisplayName("Should return only two topics sorted in ascendant by status with successful")
    @Test
    void shouldReturnTwoTopicsSortedAscendantByStatusWithSuccessful() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/topics/listAll")
                        .queryParam("size", "2")
                        .queryParam("sort", "status,asc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..getTopicDTOList[0].[?(@.topic.status == \"SOLVED\")]").exists())
                .andExpect(jsonPath("$..getTopicDTOList[1].[?(@.topic.status == \"UNSOLVED\")]").exists())
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..getTopicDTOList.length()", is(2)))
                .andExpect(jsonPath("$..page.[?(@.size == 2)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 4)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 2)]").exists());

        Assertions.assertEquals(4, this.topicRepository.findAll().size());

    }


    @DisplayName("Should return all topics sorted ascendants by title with successful")
    @Test
    void shouldReturnAllTopicsSortedAscendantByTitleWithSuccessful() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/topics/listAll")
                        .queryParam("sort", "title,asc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..getTopicDTOList[0].[?(@.topic.id == 3)]").exists())
                .andExpect(jsonPath("$..getTopicDTOList[1].[?(@.topic.id == 1)]").exists())
                .andExpect(jsonPath("$..getTopicDTOList[2].[?(@.topic.id == 2)]").exists())
                .andExpect(jsonPath("$..getTopicDTOList[3].[?(@.topic.id == 4)]").exists())
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..getTopicDTOList.length()", is(4)))
                .andExpect(jsonPath("$..page.[?(@.size == 10)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 4)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        Assertions.assertEquals(4, this.topicRepository.findAll().size());


    }


    @DisplayName("Should fail with status code 400 when attempt get topic if topic_id property " +
            "of query param is sent empty")
    @Test
    void shouldFailIfTopicIdPropertyOfQueryParamIsEmptyWhenGetTopic() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/topics")
                        .queryParam("topic_id", "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        Assertions.assertEquals(4, this.topicRepository.findAll().size());

    }

    @DisplayName("Should fail with status code 400 when request topic with" +
            " param different of type number")
    @Test
    void shouldFailToRequestTopicIfParamDifferentOfTypeNumber() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/topics")
                        .queryParam("topic_id", "unexpected")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        Assertions.assertEquals(4, this.topicRepository.findAll().size());

    }


    @DisplayName("Should fail with status code 404 when request the specified topic if not exists")
    @Test
    void shouldFailToRequestTheSpecifiedTopicIfNotExists() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/topics")
                        .queryParam("topic_id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNotFound());

        Assertions.assertEquals(4, this.topicRepository.findAll().size());

        BDDMockito.verifyNoInteractions(this.userClientRequest);


    }


    @DisplayName("Should return the specified topic with successful if exists")
    @Test
    void shouldReturnTheSpecifiedTopicWithSuccessful() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/topics")
                        .queryParam("topic_id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@.topic.id == 1)]").exists())
                .andExpect(jsonPath("$.topic.title", is("Dúvida na utilização do Feign Client")));

        assertAll(
                () -> assertThat(this.topicRepository.findAll().size(), is(4)),
                () -> assertTrue(this.topicRepository.findById(1L).isPresent())
        );

    }


    @DisplayName("Should fail with status code 403 if user authenticated hasn't authority 'topic:edit'" +
            "when edit topic")
    @Test
    void shouldFailIfUserHasNotSuitableAuthorityWhenEditTopic() throws Exception {
        final TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO(
                "Dúvida quanto a utilização do Elasticsearch",
                "Como posso integrar minha API com o Elasticsearch para monitoração?",
                Status.UNSOLVED, 1L
        );

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/edit")
                        .queryParam("topic_id", "1")
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicUpdateDTO)))
                .andExpect(status().isForbidden());

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        assertAll(
                () -> assertEquals("Dúvida na utilização do Feign Client", topic.getTitle()),
                () -> assertEquals("Como utilizar o Feign Client para integração do serviço x?", topic.getQuestion())
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }

    @DisplayName("Should fail with status code 400 when edit topic with" +
            " param different of type number")
    @Test
    void shouldFailToEditTopicIfParamDifferentOfTypeNumber() throws Exception {
        final TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO(
                "Dúvida na utilização do WebClient",
                "Como utilizar o WebClient para integração do serviço x?",
                Status.UNSOLVED, 1L
        );

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/edit")
                        .queryParam("topic_id", "unexpected")
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicUpdateDTO)))
                .andExpect(status().isBadRequest());

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 400 if title property is sent empty when edit topic")
    @Test
    void shouldFailIfTitlePropertyIsEmptyWhenEditTopic() throws Exception {
        final TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO(
                "",
                "Como posso integrar minha API com o Elasticsearch para monitoração?",
                Status.UNSOLVED, 1L
        );

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/edit")
                        .queryParam("topic_id", "1")
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicUpdateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("O título não pode ser vazio")));

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        assertAll(
                () -> assertEquals("Dúvida na utilização do Feign Client", topic.getTitle()),
                () -> assertEquals("Como utilizar o Feign Client para integração do serviço x?", topic.getQuestion())
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 400 if question property is sent empty when edit topic")
    @Test
    void shouldFailIfQuestionPropertyIsEmptyWhenEditTopic() throws Exception {
        TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO(
                "Dúvida quanto a utilização do Elasticsearch",
                "",
                Status.UNSOLVED, 1L
        );

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/edit")
                        .queryParam("topic_id", "1")
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicUpdateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("A pergunta não pode ser vazia")));

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        assertAll(
                () -> assertEquals("Dúvida na utilização do Feign Client", topic.getTitle()),
                () -> assertEquals("Como utilizar o Feign Client para integração do serviço x?", topic.getQuestion())
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 400 when attempt edit topic if topic_id property " +
            "of query param is sent empty")
    @Test
    void shouldFailIfTopicIdPropertyOfQueryParamIsEmptyWhenEditTopic() throws Exception {
        TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO(
                "Dúvida quanto a utilização do Elasticsearch",
                "Como posso integrar minha API com o Elasticsearch para monitoração?",
                Status.UNSOLVED, 1L
        );

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/edit")
                        .queryParam("topic_id", "")
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicUpdateDTO)))
                .andExpect(status().isBadRequest());

        BDDMockito.verifyNoInteractions(this.userClientRequest);


    }


    @DisplayName("Should fail with status code 404 when edit topic if the course not exists")
    @Test
    void shouldFailToEditTopicIfCourseNotExists() throws Exception {
        final TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO(
                "Dúvida quanto a utilização do Elasticsearch",
                "Como posso integrar minha API com o Elasticsearch para monitoração?",
                Status.UNSOLVED, 4L
        );

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/edit")
                        .queryParam("topic_id", "1")
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicUpdateDTO)))
                .andExpect(status().isNotFound());

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        assertAll(
                () -> assertEquals("Dúvida na utilização do Feign Client", topic.getTitle()),
                () -> assertEquals("Como utilizar o Feign Client para integração do serviço x?", topic.getQuestion())
        );

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 404 when edit topic if the user service return " +
            "404 not found status code")
    @Test
    void shouldFailToEditTopicIfUserServiceReturn404StatusCode() throws Exception {
        final TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO(
                "Dúvida quanto a utilização do Elasticsearch",
                "Como posso integrar minha API com o Elasticsearch para monitoração?",
                Status.UNSOLVED, 1L
        );

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willThrow(new RestClientException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/edit")
                        .queryParam("topic_id", "1")
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicUpdateDTO)))
                .andExpect(status().isNotFound());

        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        assertAll(
                () -> assertEquals("Dúvida na utilização do Feign Client", topic.getTitle()),
                () -> assertEquals("Como utilizar o Feign Client para integração do serviço x?", topic.getQuestion())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 418 if basic user attempt edit topic of other author")
    @Test
    void shouldFailIfBasicUserAttemptEditTopicOfOtherAuthor() throws Exception {
        final TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO(
                "Dúvida quanto a utilização do Elasticsearch",
                "Como posso integrar minha API com o Elasticsearch para monitoração?",
                Status.UNSOLVED, 1L
        );

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/edit")
                        .queryParam("topic_id", "2")
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicUpdateDTO)))
                .andExpect(status().isIAmATeapot())
                .andExpect(jsonPath("$.detail", is("Privilégio insuficiente")));

        Topic topic = this.topicRepository.findById(2L).orElseThrow();

        assertAll(
                () -> assertEquals("Dúvida na utilização do OpenShift", topic.getTitle()),
                () -> assertEquals("Como utilizar o Rosa/OpenShift para implantação do serviço x?", topic.getQuestion())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 422 when attempt edit a topic of unknown author")
    @Test
    void shouldFailWhenAttemptEditTopicOfUnknownAuthor() throws Exception {
        final TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO(
                "Dúvida quanto a utilização do Elasticsearch",
                "Como posso integrar minha API com o Elasticsearch para monitoração?",
                Status.SOLVED, 1L
        );

        BDDMockito.given(this.userClientRequest.getUserById(3L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(2));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/edit")
                        .queryParam("topic_id", "3")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "3"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicUpdateDTO)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", is(
                        "O tópico pertence a um autor inexistente," +
                                " ele não pode ser editado"
                )));

        Topic topic = this.topicRepository.findById(3L).orElseThrow();

        assertAll(
                () -> assertEquals("Dúvida em relação ao teste end-to-end", topic.getTitle()),
                () -> assertEquals("Quais as boas práticas na execução dos testes end-to-end?", topic.getQuestion())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(3L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }

    @Transactional
    @DisplayName("Topic author should be able edit specified topic if authenticated, " +
            "has authority 'topic:edit' and previous premisses are adequate")
    @Test
    void topicAuthorShouldEditSpecifiedTopicWithSuccessIfHasSuitableAuthority() throws Exception {
        final TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO(
                "Dúvida na utilização do WebClient",
                "Como utilizar o WebClient para integração do serviço x?",
                Status.UNSOLVED, 1L
        );

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/edit")
                        .queryParam("topic_id", "1")
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic.title", is("Dúvida na utilização do WebClient")))
                .andExpect(jsonPath("$.topic.question", is("Como utilizar o WebClient para " +
                        "integração do serviço x?")));


        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        assertAll(
                () -> assertEquals("Dúvida na utilização do WebClient", topic.getTitle()),
                () -> assertEquals("Como utilizar o WebClient para integração do serviço x?", topic.getQuestion())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }

    @Transactional
    @DisplayName("User ADM should be able edit topic of other author if authenticated, " +
            "has authority 'topic:edit' and previous premisses are adequate")
    @Test
    void userADMShouldEditTopicOfOtherAuthorWithSuccessIfHasSuitableAuthority() throws Exception {
        final TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO(
                "Dúvida na utilização do RestTemplate",
                "Como utilizar o RestTemplate para integração do serviço x?",
                Status.UNSOLVED, 1L
        );

        BDDMockito.given(this.userClientRequest.getUserById(3L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(2));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/edit")
                        .queryParam("topic_id", "1")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "3"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic.title", is("Dúvida na utilização do RestTemplate")))
                .andExpect(jsonPath("$.topic.question", is("Como utilizar o RestTemplate para " +
                        "integração do serviço x?")));


        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        assertAll(
                () -> assertEquals("Dúvida na utilização do RestTemplate", topic.getTitle()),
                () -> assertEquals("Como utilizar o RestTemplate para integração do serviço x?", topic.getQuestion())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(3L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }

    @Transactional
    @DisplayName("User MOD should be able edit topic of other author if authenticated, " +
            "has authority 'topic:edit' and previous premisses are adequate")
    @Test
    void userMODShouldEditTopicOfOtherAuthorWithSuccessIfHasSuitableAuthority() throws Exception {
        final TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO(
                "Dúvida na utilização da API de validação do Spring",
                "Quais são as anotações da API de validação do Spring?",
                Status.UNSOLVED, 1L
        );

        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/edit")
                        .queryParam("topic_id", "1")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic.title", is("Dúvida na utilização da API de " +
                        "validação do Spring")))
                .andExpect(jsonPath("$.topic.question", is("Quais são as anotações da API de " +
                        "validação do Spring?")));


        Topic topic = this.topicRepository.findById(1L).orElseThrow();

        assertAll(
                () -> assertEquals("Dúvida na utilização da API de validação do Spring", topic.getTitle()),
                () -> assertEquals("Quais são as anotações da API de validação do Spring?", topic.getQuestion())
        );

        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 403 if user authenticated hasn't authority 'topic:delete'" +
            " when delete topic")
    @Test
    void shouldFailIfUserHasNotSuitableAuthorityWhenDeleteTopic() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/topics/delete")
                        .queryParam("topic_id", "1")
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

        Assertions.assertEquals(4, this.topicRepository.findAll().size());

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 400 when attempt delete topic if topic_id property " +
            "of query param is sent empty")
    @Test
    void shouldFailIfTopicIdPropertyOfQueryParamIsEmptyWhenDeleteTopic() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/topics")
                        .queryParam("topic_id", "")
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        Assertions.assertEquals(4, this.topicRepository.findAll().size());

        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }

    @DisplayName("Should fail with status code 400 when delete topic with" +
            " param different of type number")
    @Test
    void shouldFailToDeleteTopicIfParamDifferentOfTypeNumber() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(delete("/forumhub.io/api/v1/topics/delete")
                        .queryParam("topic_id", "unexpected")
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

    }


    @DisplayName("Should fail with status code 404 when delete topic if the user service " +
            "return 404 not found status code")
    @Test
    void shouldFailToDeleteTopicIfUserServiceReturn404StatusCode() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willThrow(new RestClientException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        this.mockMvc.perform(delete("/forumhub.io/api/v1/topics/delete")
                        .queryParam("topic_id", "1")
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNotFound());

        Assertions.assertEquals(4, this.topicRepository.findAll().size());

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 418 if basic user attempt delete topic of other author")
    @Test
    void shouldFailIfBasicUserAttemptDeleteTopicOfOtherAuthor() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(delete("/forumhub.io/api/v1/topics/delete")
                        .queryParam("topic_id", "2")
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isIAmATeapot())
                .andExpect(jsonPath("$.detail", is("Privilégio insuficiente")));

        Assertions.assertEquals(4, this.topicRepository.findAll().size());

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }

    @Transactional
    @DisplayName("Topic author should be able delete specified topic if authenticated, " +
            "has authority 'topic:delete' and previous premisses are adequate")
    @Test
    void topicAuthorShouldDeleteSpecifiedTopicWithSuccessIfHasSuitableAuthority() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        this.mockMvc.perform(delete("/forumhub.io/api/v1/topics/delete")
                        .queryParam("topic_id", "1")
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"HttpStatusCode OK\"}"));

        Assertions.assertEquals(3, this.topicRepository.findAll().size());

        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }

    @Transactional
    @DisplayName("User ADM should be able delete topic of other author if authenticated, " +
            "has authority 'topic:delete' and previous premisses are adequate")
    @Test
    void userADMShouldDeleteTopicOfOtherAuthorWithSuccessIfHasSuitableAuthority() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(3L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(2));

        this.mockMvc.perform(delete("/forumhub.io/api/v1/topics/delete")
                        .queryParam("topic_id", "2")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "3"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"HttpStatusCode OK\"}"));

        Assertions.assertEquals(3, this.topicRepository.findAll().size());

        BDDMockito.verify(this.userClientRequest).getUserById(3L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }

    @Transactional
    @DisplayName("User MOD should be able delete topic of other author if authenticated, " +
            "has authority 'topic:delete' and previous premisses are adequate")
    @Test
    void userMODShouldDeleteTopicOfOtherAuthorWithSuccessIfHasSuitableAuthority() throws Exception {
        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        this.mockMvc.perform(delete("/forumhub.io/api/v1/topics/delete")
                        .queryParam("topic_id", "3")
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "2"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"HttpStatusCode OK\"}"));

        Assertions.assertEquals(3, this.topicRepository.findAll().size());

        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


}
