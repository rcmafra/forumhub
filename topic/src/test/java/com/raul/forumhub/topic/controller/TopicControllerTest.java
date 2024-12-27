package com.raul.forumhub.topic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raul.forumhub.topic.domain.Status;
import com.raul.forumhub.topic.domain.Topic;
import com.raul.forumhub.topic.dto.request.TopicCreateDTO;
import com.raul.forumhub.topic.dto.request.TopicUpdateDTO;
import com.raul.forumhub.topic.dto.response.GetTopicDTO;
import com.raul.forumhub.topic.exception.handler.GlobalExceptionHandler;
import com.raul.forumhub.topic.security.TopicSecurityConfig;
import com.raul.forumhub.topic.service.TopicService;
import com.raul.forumhub.topic.util.TestsHelper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest
@ActiveProfiles(value = "test")
@ContextConfiguration(classes = {TopicController.class,
        TopicSecurityConfig.class, GlobalExceptionHandler.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TopicControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TopicService topicService;

    @MockBean
    ClientRegistrationRepository clientRegistrationRepository;

    private static final Jwt jwt;

    static {
        jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("user_id", "1")
                .build();
    }


    @Order(1)
    @DisplayName("Should fail with status code 401 when create topic if user unauthenticated")
    @Test
    void shouldFailToCreateTopicIfUnauthenticated() throws Exception {
        final TopicCreateDTO topicCreateDTO = new TopicCreateDTO("Dúvida na utilização do Feign Client",
                "Como utilizar o Feign Client para integração do serviço x?",
                1L);

        this.mockMvc.perform(post("/api-forum/v1/forumhub/topics/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicCreateDTO)))
                .andExpect(status().isUnauthorized());

        BDDMockito.verifyNoInteractions(this.topicService);

    }


    @Order(2)
    @DisplayName("Should create topic with success if user is authenticated")
    @Test
    void shouldCreateTopicWithSuccessIfAuthenticated() throws Exception {
        final TopicCreateDTO topicCreateDTO = new TopicCreateDTO("Dúvida na utilização do Feign Client",
                "Como utilizar o Feign Client para integração do serviço x?",
                1L);

        BDDMockito.doNothing().when(this.topicService).createTopic(topicCreateDTO, 1L);

        this.mockMvc.perform(post("/api-forum/v1/forumhub/topics/create")
                        .with(jwt().jwt(jwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicCreateDTO)))
                .andExpect(status().isCreated());

        BDDMockito.verify(this.topicService).createTopic(topicCreateDTO, 1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);

    }


    @Order(3)
    @DisplayName("Should return all topics unsorted with successful")
    @Test
    void shouldReturnAllTopicsUnsortedWithSuccessful() throws Exception {
        Page<GetTopicDTO> topicPage =
                new PageImpl<>(TestsHelper.TopicHelper.topicListWithAnswers(),
                        Pageable.unpaged(), 3)
                        .map(GetTopicDTO::new);

        BDDMockito.given(this.topicService.topicList(any(Pageable.class)))
                .willReturn(topicPage);

        this.mockMvc.perform(get("/api-forum/v1/forumhub/topics/listAll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..getTopicDTOList.length()", is(3)))
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..page.[?(@.size == 3)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 3)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        BDDMockito.verify(this.topicService).topicList(any(Pageable.class));
        BDDMockito.verifyNoMoreInteractions(this.topicService);


    }

    @Order(4)
    @DisplayName("Should return all topics sorted descendants by created date with successful")
    @Test
    void shouldReturnAllTopicsSortedDescendantByCreateDateWithSuccessful() throws Exception {
        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Topic> sortedTopicByCreatedAt = TestsHelper.TopicHelper.topicListWithAnswers()
                .stream().sorted(Comparator.comparing(Topic::getCreatedAt).reversed())
                .toList();

        Page<GetTopicDTO> topicPage =
                new PageImpl<>(sortedTopicByCreatedAt, pageable, 3)
                        .map(GetTopicDTO::new);

        BDDMockito.given(this.topicService.topicList(pageable))
                .willReturn(topicPage);

        this.mockMvc.perform(get("/api-forum/v1/forumhub/topics/listAll")
                        .queryParam("sort", "createdAt,desc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..getTopicDTOList[0].[?(@.id == 3)]").exists())
                .andExpect(jsonPath("$..getTopicDTOList[1].[?(@.id == 1)]").exists())
                .andExpect(jsonPath("$..getTopicDTOList[2].[?(@.id == 2)]").exists())
                .andExpect(jsonPath("$..getTopicDTOList.length()", is(3)))
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..page.[?(@.size == 10)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 3)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        BDDMockito.verify(this.topicService).topicList(pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicService);


    }

    @Order(5)
    @DisplayName("Should return only two topics sorted in ascendant by status with successful")
    @Test
    void shouldReturnTwoTopicsSortedAscendantByStatusWithSuccessful() throws Exception {
        Pageable pageable = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.ASC, "status"));

        List<Topic> sortedTopicByStatus = TestsHelper.TopicHelper.topicListWithAnswers()
                .stream().filter(topic -> topic.getId().equals(2L) || topic.getId().equals(3L))
                .sorted(Comparator.comparing(Topic::getStatus))
                .toList();

        Page<GetTopicDTO> topicPage =
                new PageImpl<>(sortedTopicByStatus, pageable, 2)
                        .map(GetTopicDTO::new);

        BDDMockito.given(this.topicService.topicList(pageable))
                .willReturn(topicPage);

        this.mockMvc.perform(get("/api-forum/v1/forumhub/topics/listAll")
                        .queryParam("size", "2")
                        .queryParam("sort", "status,asc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..getTopicDTOList[0].[?(@.id == 3)]").exists())
                .andExpect(jsonPath("$..getTopicDTOList[0].[?(@.status == \"SOLVED\")]").exists())
                .andExpect(jsonPath("$..getTopicDTOList[1].[?(@.id == 2)]").exists())
                .andExpect(jsonPath("$..getTopicDTOList[1].[?(@.status == \"UNSOLVED\")]").exists())
                .andExpect(jsonPath("$..getTopicDTOList.length()", is(2)))
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..page.[?(@.size == 2)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 2)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        BDDMockito.verify(this.topicService).topicList(pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicService);

    }


    @Order(6)
    @DisplayName("Should return all topics sorted ascendants by title with successful")
    @Test
    void shouldReturnAllTopicsSortedAscendantByTitleWithSuccessful() throws Exception {
        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.ASC, "title"));

        List<Topic> sortedTopicByTitle = TestsHelper.TopicHelper.topicListWithAnswers()
                .stream().sorted(Comparator.comparing(Topic::getTitle))
                .toList();

        Page<GetTopicDTO> topicPage =
                new PageImpl<>(sortedTopicByTitle, pageable, 3)
                        .map(GetTopicDTO::new);

        BDDMockito.given(this.topicService.topicList(pageable))
                .willReturn(topicPage);

        this.mockMvc.perform(get("/api-forum/v1/forumhub/topics/listAll")
                        .queryParam("sort", "title,asc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..getTopicDTOList[0].[?(@.id == 3)]").exists())
                .andExpect(jsonPath("$..getTopicDTOList[1].[?(@.id == 1)]").exists())
                .andExpect(jsonPath("$..getTopicDTOList[2].[?(@.id == 2)]").exists())
                .andExpect(jsonPath("$..getTopicDTOList.length()", is(3)))
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..page.[?(@.size == 10)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 3)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        BDDMockito.verify(this.topicService).topicList(pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicService);


    }

    @Order(7)
    @DisplayName("Should fail with status code 400 when attempt get topic if topic_id property " +
            "of query param is sent empty")
    @Test
    void shouldFailIfTopicIdPropertyOfQueryParamIsEmptyWhenGetTopic() throws Exception {
        this.mockMvc.perform(get("/api-forum/v1/forumhub/topics")
                        .queryParam("topic_id", "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        BDDMockito.verifyNoInteractions(this.topicService);

    }


    @Order(8)
    @DisplayName("Should return the specified topic with successful if exists")
    @Test
    void shouldReturnTheSpecifiedTopicWithSuccessful() throws Exception {
        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicListWithAnswers().get(0));

        this.mockMvc.perform(get("/api-forum/v1/forumhub/topics")
                        .queryParam("topic_id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@.id == 1)]").exists())
                .andExpect(jsonPath("$.title", is("Dúvida na utilização do Feign Client")));

        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);

    }

    @Order(9)
    @DisplayName("Should fail with status code 403 if user authenticated hasn't authority 'topic:edit'" +
            "when edit topic")
    @Test
    void shouldFailIfUserHasNotSuitableAuthorityWhenEditTopic() throws Exception {
        final TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO(
                "Dúvida quanto a utilização do Elasticsearch",
                "Como posso integrar minha API com o Elasticsearch para monitoração?",
                Status.UNSOLVED, 1L
        );

        this.mockMvc.perform(put("/api-forum/v1/forumhub/topics")
                        .queryParam("topic_id", "1")
                        .with(jwt().jwt(jwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicUpdateDTO)))
                .andExpect(status().isForbidden());

        BDDMockito.verifyNoInteractions(this.topicService);

    }

    @Order(10)
    @DisplayName("Should fail with status code 400 when attempt update topic if topic_id property " +
            "of query param is sent empty")
    @Test
    void shouldFailIfTopicIdPropertyOfQueryParamIsEmptyWhenUpdateTopic() throws Exception {
        TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO(
                "Dúvida quanto a utilização do Elasticsearch",
                "Como posso integrar minha API com o Elasticsearch para monitoração?",
                Status.UNSOLVED, 1L
        );

        this.mockMvc.perform(put("/api-forum/v1/forumhub/topics")
                        .queryParam("topic_id", "")
                        .with(jwt().jwt(jwt)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicUpdateDTO)))
                .andExpect(status().isBadRequest());

        BDDMockito.verifyNoInteractions(this.topicService);


    }

    @Order(11)
    @DisplayName("Should edit topic with success if user authenticated has authority 'topic:edit'")
    @Test
    void shouldEditTopicWithSuccessIfUserHasSuitableAuthority() throws Exception {
        final TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO("Dúvida na utilização do WebClient",
                "Como utilizar o WebClient para integração do serviço x?",
                Status.UNSOLVED, 1L
        );

        Topic topic = TestsHelper.TopicHelper.topicList().get(0);
        topic.setTitle("Dúvida na utilização do WebClient");
        topic.setQuestion("Como utilizar o WebClient para integração do serviço x?");

        BDDMockito.given(this.topicService.updateTopic(1L, 1L, topicUpdateDTO))
                .willReturn(new GetTopicDTO(topic));

        this.mockMvc.perform(put("/api-forum/v1/forumhub/topics")
                        .queryParam("topic_id", "1")
                        .with(jwt().jwt(jwt)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Dúvida na utilização do WebClient")))
                .andExpect(jsonPath("$.question", is("Como utilizar o WebClient para " +
                        "integração do serviço x?")));


        BDDMockito.verify(this.topicService).updateTopic(1L, 1L, topicUpdateDTO);
        BDDMockito.verifyNoMoreInteractions(this.topicService);


    }

    @Order(12)
    @DisplayName("Should fail with status code 403 if user authenticated hasn't authority 'topic:delete'" +
            " when delete topic")
    @Test
    void shouldFailIfUserHasNotSuitableAuthorityWhenDeleteTopic() throws Exception {
        this.mockMvc.perform(delete("/api-forum/v1/forumhub/topics/delete")
                        .queryParam("topic_id", "1")
                        .with(jwt().jwt(jwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

        BDDMockito.verifyNoInteractions(this.topicService);
    }

    @Order(13)
    @DisplayName("Should fail with status code 400 when attempt delete topic if topic_id property " +
            "of query param is sent empty")
    @Test
    void shouldFailIfTopicIdPropertyOfQueryParamIsEmptyWhenDeleteTopic() throws Exception {
        this.mockMvc.perform(delete("/api-forum/v1/forumhub/topics")
                        .queryParam("topic_id", "")
                        .with(jwt().jwt(jwt)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        BDDMockito.verifyNoInteractions(this.topicService);

    }

    @Order(14)
    @DisplayName("Should delete topic with success if user authenticated has authority 'topic:delete'")
    @Test
    void shouldDeleteTopicWithSuccessIfUserHasSuitableAuthority() throws Exception {
        BDDMockito.doNothing().when(this.topicService).deleteTopic(1L, 1L);

        this.mockMvc.perform(delete("/api-forum/v1/forumhub/topics/delete")
                        .queryParam("topic_id", "1")
                        .with(jwt().jwt(jwt)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"HttpStatusCode OK\"}"));

        BDDMockito.verify(this.topicService).deleteTopic(1L, 1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);

    }


}