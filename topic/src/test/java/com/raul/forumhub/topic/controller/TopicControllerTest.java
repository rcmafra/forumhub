package com.raul.forumhub.topic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.raul.forumhub.topic.domain.Status;
import com.raul.forumhub.topic.domain.Topic;
import com.raul.forumhub.topic.dto.request.TopicCreateRequestDTO;
import com.raul.forumhub.topic.dto.request.TopicUpdateRequestDTO;
import com.raul.forumhub.topic.dto.response.TopicResponseDTO;
import com.raul.forumhub.topic.exception.handler.GlobalExceptionHandler;
import com.raul.forumhub.topic.security.TopicSecurityConfig;
import com.raul.forumhub.topic.service.TopicService;
import com.raul.forumhub.topic.util.TestsHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
    
    @Autowired
    ObjectMapper objectMapper;

    private static final Jwt JWT;

    static {
        JWT = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("user_id", "1")
                .build();
    }

    @DisplayName("Should fail with status code 404 if resource doesn't exists")
    @Test
    void shouldFailIfResourceDoesNotExistToTheSendRequest() throws Exception {
        final TopicCreateRequestDTO topicCreateRequestDTO = new TopicCreateRequestDTO("Dúvida na utilização do Feign Client",
                "Como utilizar o Feign Client para integração do serviço x?",
                1L);

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/creat")
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(this.objectMapper
                                .writeValueAsString(topicCreateRequestDTO)))
                .andExpect(status().isNotFound());

    }

    @DisplayName("Should fail with status code 400 if method isn't supported")
    @Test
    void shouldFailIfMethodIsNotSupportedToTheSendRequest() throws Exception {
        final TopicCreateRequestDTO topicCreateRequestDTO = new TopicCreateRequestDTO("Dúvida na utilização do Feign Client",
                "Como utilizar o Feign Client para integração do serviço x?",
                1L);

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/create")
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(this.objectMapper
                                .writeValueAsString(topicCreateRequestDTO)))
                .andExpect(status().isBadRequest());

    }


    @DisplayName("Should fail with status code 401 when create topic if user unauthenticated")
    @Test
    void shouldFailToCreateTopicIfUnauthenticated() throws Exception {
        final TopicCreateRequestDTO topicCreateRequestDTO = new TopicCreateRequestDTO("Dúvida na utilização do Feign Client",
                "Como utilizar o Feign Client para integração do serviço x?",
                1L);

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(this.objectMapper
                                .writeValueAsString(topicCreateRequestDTO)))
                .andExpect(status().isUnauthorized());

        BDDMockito.verifyNoInteractions(this.topicService);

    }

    @DisplayName("Should fail with status code 400 if title property is sent empty when create topic")
    @Test
    void shouldFailIfTitlePropertyIsEmptyWhenCreateTopic() throws Exception {
        final TopicCreateRequestDTO topicCreateRequestDTO = new TopicCreateRequestDTO("",
                "Como utilizar o Feign Client para integração do serviço x?",
                1L);

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/create")
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicCreateRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("O título não pode ser vazio")));

        BDDMockito.verifyNoInteractions(this.topicService);

    }


    @DisplayName("Should fail with status code 400 if question property is sent empty when create topic")
    @Test
    void shouldFailIfQuestionPropertyIsEmptyWhenCreateTopic() throws Exception {
        final TopicCreateRequestDTO topicCreateRequestDTO = new TopicCreateRequestDTO("Dúvida na utilização do Feign Client",
                "",
                1L);

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/create")
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicCreateRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("A pergunta não pode ser vazia")));

        BDDMockito.verifyNoInteractions(this.topicService);

    }

    @DisplayName("Should create topic with success if user is authenticated")
    @Test
    void shouldCreateTopicWithSuccessIfAuthenticated() throws Exception {
        final TopicCreateRequestDTO topicCreateRequestDTO = new TopicCreateRequestDTO("Dúvida na utilização do Feign Client",
                "Como utilizar o Feign Client para integração do serviço x?",
                1L);

        final TopicResponseDTO topicResponseDTO = new TopicResponseDTO(TestsHelper.TopicHelper.topicList().get(0));

        BDDMockito.given(this.topicService.createTopic(topicCreateRequestDTO, 1L))
                .willReturn(topicResponseDTO);

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/create")
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(this.objectMapper
                                .writeValueAsString(topicCreateRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().json(this.objectMapper
                        .registerModule(new JavaTimeModule())
                        .writeValueAsString(topicResponseDTO)));

        BDDMockito.verify(this.topicService).createTopic(topicCreateRequestDTO, 1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);

    }


    @DisplayName("Should return all topics unsorted with successful")
    @Test
    void shouldReturnAllTopicsUnsortedWithSuccessful() throws Exception {
        Page<TopicResponseDTO> topicPage =
                new PageImpl<>(TestsHelper.TopicHelper.topicListWithAnswers(),
                        Pageable.unpaged(), 4)
                        .map(TopicResponseDTO::new);

        BDDMockito.given(this.topicService.topicList(any(Pageable.class)))
                .willReturn(topicPage);

        this.mockMvc.perform(get("/forumhub.io/api/v1/topics/listAll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..topicResponseDTOList.length()", is(4)))
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..page.[?(@.size == 4)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 4)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        BDDMockito.verify(this.topicService).topicList(any(Pageable.class));
        BDDMockito.verifyNoMoreInteractions(this.topicService);


    }


    @DisplayName("Should return all topics sorted descendants by created date with successful")
    @Test
    void shouldReturnAllTopicsSortedDescendantByCreateDateWithSuccessful() throws Exception {
        List<Topic> sortedTopicByCreatedAt = TestsHelper.TopicHelper.topicListWithAnswers()
                .stream().sorted(Comparator.comparing(Topic::getCreatedAt).reversed())
                .toList();

        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<TopicResponseDTO> topicPage =
                new PageImpl<>(sortedTopicByCreatedAt, pageable, 4)
                        .map(TopicResponseDTO::new);

        BDDMockito.given(this.topicService.topicList(pageable))
                .willReturn(topicPage);

        this.mockMvc.perform(get("/forumhub.io/api/v1/topics/listAll")
                        .queryParam("sort", "createdAt,desc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..topicResponseDTOList[0].[?(@.topic.id == 3)]").exists())
                .andExpect(jsonPath("$..topicResponseDTOList[1].[?(@.topic.id == 1)]").exists())
                .andExpect(jsonPath("$..topicResponseDTOList[2].[?(@.topic.id == 2)]").exists())
                .andExpect(jsonPath("$..topicResponseDTOList[3].[?(@.topic.id == 4)]").exists())
                .andExpect(jsonPath("$..topicResponseDTOList.length()", is(4)))
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..page.[?(@.size == 10)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 4)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        BDDMockito.verify(this.topicService).topicList(pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicService);


    }


    @DisplayName("Should return only two topics sorted in ascendant by status with successful")
    @Test
    void shouldReturnTwoTopicsSortedAscendantByStatusWithSuccessful() throws Exception {
        List<Topic> sortedTopicByStatus = TestsHelper.TopicHelper.topicListWithAnswers()
                .stream().filter(topic -> topic.getId().equals(2L) || topic.getId().equals(3L))
                .sorted(Comparator.comparing(Topic::getStatus))
                .toList();

        Pageable pageable = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.ASC, "status"));

        Page<TopicResponseDTO> topicPage =
                new PageImpl<>(sortedTopicByStatus, pageable, 2)
                        .map(TopicResponseDTO::new);

        BDDMockito.given(this.topicService.topicList(pageable))
                .willReturn(topicPage);

        this.mockMvc.perform(get("/forumhub.io/api/v1/topics/listAll")
                        .queryParam("size", "2")
                        .queryParam("sort", "status,asc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..topicResponseDTOList[0].[?(@.topic.id == 3)]").exists())
                .andExpect(jsonPath("$..topicResponseDTOList[0].[?(@.topic.status == \"SOLVED\")]").exists())
                .andExpect(jsonPath("$..topicResponseDTOList[1].[?(@.topic.id == 2)]").exists())
                .andExpect(jsonPath("$..topicResponseDTOList[1].[?(@.topic.status == \"UNSOLVED\")]").exists())
                .andExpect(jsonPath("$..topicResponseDTOList.length()", is(2)))
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..page.[?(@.size == 2)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 2)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        BDDMockito.verify(this.topicService).topicList(pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicService);

    }


    @DisplayName("Should return all topics sorted ascendants by title with successful")
    @Test
    void shouldReturnAllTopicsSortedAscendantByTitleWithSuccessful() throws Exception {
        List<Topic> sortedTopicByTitle = TestsHelper.TopicHelper.topicListWithAnswers()
                .stream().sorted(Comparator.comparing(Topic::getTitle))
                .toList();

        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.ASC, "title"));

        Page<TopicResponseDTO> topicPage =
                new PageImpl<>(sortedTopicByTitle, pageable, 4)
                        .map(TopicResponseDTO::new);

        BDDMockito.given(this.topicService.topicList(pageable))
                .willReturn(topicPage);

        this.mockMvc.perform(get("/forumhub.io/api/v1/topics/listAll")
                        .queryParam("sort", "title,asc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..topicResponseDTOList[0].[?(@.topic.id == 3)]").exists())
                .andExpect(jsonPath("$..topicResponseDTOList[1].[?(@.topic.id == 1)]").exists())
                .andExpect(jsonPath("$..topicResponseDTOList[2].[?(@.topic.id == 2)]").exists())
                .andExpect(jsonPath("$..topicResponseDTOList[3].[?(@.topic.id == 4)]").exists())
                .andExpect(jsonPath("$..topicResponseDTOList.length()", is(4)))
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..page.[?(@.size == 10)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 4)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        BDDMockito.verify(this.topicService).topicList(pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicService);


    }

    @DisplayName("Should return page without content when to request topics by course id and" +
                 " isn't exist none topics associate with course")
    @Test
    void shouldReturnPageWithoutContentWhenIsNotExistNoneTopicAssociatedToTheCourse() throws Exception {
        List<Topic> topicsListByCourse = TestsHelper.TopicHelper.topicListWithAnswers()
                .stream().filter(topic -> topic.getCourse().getId().equals(4L))
                .toList();

        Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());

        Page<TopicResponseDTO> topicPage =
                new PageImpl<>(topicsListByCourse, pageable, 0)
                        .map(TopicResponseDTO::new);

        BDDMockito.given(this.topicService.topicsListByCourse(4L, pageable))
                .willReturn(topicPage);

        this.mockMvc.perform(get("/forumhub.io/api/v1/topics/searchTopicsByCourse")
                        .queryParam("course_id", "4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..topicResponseDTOList.length()", is(0)))
                .andExpect(jsonPath("$..page.[?(@.size == 10)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 0)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 0)]").exists());

        BDDMockito.verify(this.topicService).topicsListByCourse(4L, pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicService);


    }

    @DisplayName("Should return topics by course id unsorted with successful")
    @Test
    void shouldReturnTopicsByCourseUnsortedWithSuccessful() throws Exception {
        List<Topic> topicsListByCourse = TestsHelper.TopicHelper.topicListWithAnswers()
                .stream().filter(topic -> topic.getCourse().getId().equals(1L))
                .toList();

        Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());

        Page<TopicResponseDTO> topicPage =
                new PageImpl<>(topicsListByCourse, pageable, 2)
                        .map(TopicResponseDTO::new);

        BDDMockito.given(this.topicService.topicsListByCourse(1L, pageable))
                .willReturn(topicPage);

        this.mockMvc.perform(get("/forumhub.io/api/v1/topics/searchTopicsByCourse")
                        .queryParam("course_id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..topicResponseDTOList.length()", is(2)))
                .andExpect(jsonPath("$..page.[?(@.size == 10)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 2)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        BDDMockito.verify(this.topicService).topicsListByCourse(1L, pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicService);


    }


    @DisplayName("Should return topics by course id sorted descendants by created date with successful")
    @Test
    void shouldReturnTopicsByCourseSortedDescendantByCreateDateWithSuccessful() throws Exception {
        List<Topic> sortedTopicByCreatedAt = TestsHelper.TopicHelper.topicListWithAnswers()
                .stream().filter(topic -> topic.getCourse().getId().equals(1L))
                .sorted(Comparator.comparing(Topic::getCreatedAt).reversed())
                .toList();

        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<TopicResponseDTO> topicPage =
                new PageImpl<>(sortedTopicByCreatedAt, pageable, 2)
                        .map(TopicResponseDTO::new);

        BDDMockito.given(this.topicService.topicsListByCourse(1L, pageable))
                .willReturn(topicPage);

        this.mockMvc.perform(get("/forumhub.io/api/v1/topics/searchTopicsByCourse")
                        .queryParam("course_id", "1")
                        .queryParam("sort", "createdAt,desc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..topicResponseDTOList[0].[?(@.topic.id == 1)]").exists())
                .andExpect(jsonPath("$..topicResponseDTOList[1].[?(@.topic.id == 4)]").exists())
                .andExpect(jsonPath("$..topicResponseDTOList.length()", is(2)))
                .andExpect(jsonPath("$..page.[?(@.size == 10)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 2)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        BDDMockito.verify(this.topicService).topicsListByCourse(1L, pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicService);


    }


    @DisplayName("Should return only one topic by course id sorted in ascendant by status with successful")
    @Test
    void shouldReturnOneTopicByCourseSortedAscendantByStatusWithSuccessful() throws Exception {
        List<Topic> sortedTopicByStatus = TestsHelper.TopicHelper.topicListWithAnswers()
                .stream().filter(topic -> topic.getId().equals(1L))
                .sorted(Comparator.comparing(Topic::getStatus))
                .toList();

        Pageable pageable = PageRequest.of(0, 1,
                Sort.by(Sort.Direction.ASC, "status"));

        Page<TopicResponseDTO> topicPage =
                new PageImpl<>(sortedTopicByStatus, pageable, 1)
                        .map(TopicResponseDTO::new);

        BDDMockito.given(this.topicService.topicsListByCourse(1L, pageable))
                .willReturn(topicPage);

        this.mockMvc.perform(get("/forumhub.io/api/v1/topics/searchTopicsByCourse")
                        .queryParam("course_id", "1")
                        .queryParam("size", "1")
                        .queryParam("sort", "status,asc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..topicResponseDTOList[0].[?(@.topic.status == \"UNSOLVED\")]").exists())
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..topicResponseDTOList.length()", is(1)))
                .andExpect(jsonPath("$..page.[?(@.size == 1)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 1)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        BDDMockito.verify(this.topicService).topicsListByCourse(1L, pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicService);

    }


    @DisplayName("Should return topics by course id sorted ascendants by title with successful")
    @Test
    void shouldReturnTopicsByCourseSortedAscendantByTitleWithSuccessful() throws Exception {
        List<Topic> sortedTopicByTitle = TestsHelper.TopicHelper.topicListWithAnswers()
                .stream().filter(topic -> topic.getCourse().getId().equals(1L))
                .sorted(Comparator.comparing(Topic::getTitle))
                .toList();

        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.ASC, "title"));

        Page<TopicResponseDTO> topicPage =
                new PageImpl<>(sortedTopicByTitle, pageable, 2)
                        .map(TopicResponseDTO::new);

        BDDMockito.given(this.topicService.topicsListByCourse(1L, pageable))
                .willReturn(topicPage);

        this.mockMvc.perform(get("/forumhub.io/api/v1/topics/searchTopicsByCourse")
                        .queryParam("course_id", "1")
                        .queryParam("sort", "title,asc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..topicResponseDTOList[0].[?(@.topic.id == 1)]").exists())
                .andExpect(jsonPath("$..topicResponseDTOList[1].[?(@.topic.id == 4)]").exists())
                .andExpect(jsonPath("$..page.[?(@.number == 0)]").exists())
                .andExpect(jsonPath("$..topicResponseDTOList.length()", is(2)))
                .andExpect(jsonPath("$..page.[?(@.size == 10)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalElements == 2)]").exists())
                .andExpect(jsonPath("$..page.[?(@.totalPages == 1)]").exists());

        BDDMockito.verify(this.topicService).topicsListByCourse(1L, pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicService);


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

        BDDMockito.verifyNoInteractions(this.topicService);

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

        BDDMockito.verifyNoInteractions(this.topicService);

    }


    @DisplayName("Should return the specified topic with successful if exists")
    @Test
    void shouldReturnTheSpecifiedTopicWithSuccessful() throws Exception {
        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicListWithAnswers().get(0));

        this.mockMvc.perform(get("/forumhub.io/api/v1/topics")
                        .queryParam("topic_id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@.topic.id == 1)]").exists())
                .andExpect(jsonPath("$.topic.title", is("Dúvida na utilização do Feign Client")));

        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);

    }


    @DisplayName("Should fail with status code 403 if user authenticated hasn't authority 'topic:edit'" +
                 "when edit topic")
    @Test
    void shouldFailIfUserHasNotSuitableAuthorityWhenEditTopic() throws Exception {
        final TopicUpdateRequestDTO topicUpdateRequestDTO = new TopicUpdateRequestDTO(
                "Dúvida quanto a utilização do Elasticsearch",
                "Como posso integrar minha API com o Elasticsearch para monitoração?",
                Status.UNSOLVED, 1L
        );

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/edit", 1)
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(this.objectMapper
                                .writeValueAsString(topicUpdateRequestDTO)))
                .andExpect(status().isForbidden());

        BDDMockito.verifyNoInteractions(this.topicService);

    }

    @DisplayName("Should fail with status code 400 when edit topic with" +
                 " param different of type number")
    @Test
    void shouldFailToEditTopicIfParamDifferentOfTypeNumber() throws Exception {
        final TopicUpdateRequestDTO topicUpdateRequestDTO = new TopicUpdateRequestDTO(
                "Dúvida na utilização do WebClient",
                "Como utilizar o WebClient para integração do serviço x?",
                Status.UNSOLVED, 1L
        );

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/edit", "unexpected")
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(this.objectMapper
                                .writeValueAsString(topicUpdateRequestDTO)))
                .andExpect(status().isBadRequest());

        BDDMockito.verifyNoInteractions(this.topicService);

    }

    @DisplayName("Should fail with status code 400 if title property is sent empty when edit topic")
    @Test
    void shouldFailIfTitlePropertyIsEmptyWhenEditTopic() throws Exception {
        final TopicUpdateRequestDTO topicUpdateRequestDTO = new TopicUpdateRequestDTO(
                "",
                "Como posso integrar minha API com o Elasticsearch para monitoração?",
                Status.UNSOLVED, 1L
        );

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/edit", 1)
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicUpdateRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("O título não pode ser vazio")));

        BDDMockito.verifyNoInteractions(this.topicService);

    }


    @DisplayName("Should fail with status code 400 if question property is sent empty when edit topic")
    @Test
    void shouldFailIfQuestionPropertyIsEmptyWhenEditTopic() throws Exception {
        TopicUpdateRequestDTO topicUpdateRequestDTO = new TopicUpdateRequestDTO(
                "Dúvida quanto a utilização do Elasticsearch",
                "",
                Status.UNSOLVED, 1L
        );

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/edit", 1)
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicUpdateRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("A pergunta não pode ser vazia")));

        BDDMockito.verifyNoInteractions(this.topicService);

    }


    @DisplayName("Should fail with status code 400 when attempt update topic if topic_id property " +
                 "of query param is sent empty")
    @Test
    void shouldFailIfTopicIdPropertyOfQueryParamIsEmptyWhenUpdateTopic() throws Exception {
        TopicUpdateRequestDTO topicUpdateRequestDTO = new TopicUpdateRequestDTO(
                "Dúvida quanto a utilização do Elasticsearch",
                "Como posso integrar minha API com o Elasticsearch para monitoração?",
                Status.UNSOLVED, 1L
        );

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics")
                        .queryParam("topic_id", "")
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(this.objectMapper
                                .writeValueAsString(topicUpdateRequestDTO)))
                .andExpect(status().isBadRequest());

        BDDMockito.verifyNoInteractions(this.topicService);


    }

    @DisplayName("Should edit topic of the other author with success if user authenticated has authority 'topic:edit'" +
                 "and is ADM")
    @Test
    void shouldEditTopicOfTheOtherAuthorWithSuccessIfUserHasSuitableAuthorityAndIsADM() throws Exception {
        final TopicUpdateRequestDTO topicUpdateRequestDTO = new TopicUpdateRequestDTO("Dúvida na utilização do WebClient",
                "Como utilizar o WebClient para integração do serviço x?",
                Status.UNSOLVED, 1L
        );

        Topic topic = TestsHelper.TopicHelper.topicList().get(0);
        topic.setTitle("Dúvida na utilização do WebClient");
        topic.setQuestion("Como utilizar o WebClient para integração do serviço x?");

        BDDMockito.given(this.topicService.updateTopic(1L, 4L, topicUpdateRequestDTO))
                .willReturn(new TopicResponseDTO(topic));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/edit", 1)
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "4"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(this.objectMapper
                                .writeValueAsString(topicUpdateRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic.title", is("Dúvida na utilização do WebClient")))
                .andExpect(jsonPath("$.topic.question", is("Como utilizar o WebClient para " +
                                                           "integração do serviço x?")));


        BDDMockito.verify(this.topicService).updateTopic(1L, 4L, topicUpdateRequestDTO);
        BDDMockito.verifyNoMoreInteractions(this.topicService);


    }

    @DisplayName("Should edit topic of the other author with success if user authenticated has authority 'topic:edit'" +
                 "and is MOD")
    @Test
    void shouldEditTopicOfTheOtherAuthorWithSuccessIfUserHasSuitableAuthorityAndIsMOD() throws Exception {
        final TopicUpdateRequestDTO topicUpdateRequestDTO = new TopicUpdateRequestDTO("Dúvida na utilização do WebClient",
                "Como utilizar o WebClient para integração do serviço x?",
                Status.UNSOLVED, 1L
        );

        Topic topic = TestsHelper.TopicHelper.topicList().get(0);
        topic.setTitle("Dúvida na utilização do WebClient");
        topic.setQuestion("Como utilizar o WebClient para integração do serviço x?");

        BDDMockito.given(this.topicService.updateTopic(1L, 3L, topicUpdateRequestDTO))
                .willReturn(new TopicResponseDTO(topic));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/edit", 1)
                        .with(jwt().jwt(jwt -> jwt.claim("user_id", "3"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(this.objectMapper
                                .writeValueAsString(topicUpdateRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic.title", is("Dúvida na utilização do WebClient")))
                .andExpect(jsonPath("$.topic.question", is("Como utilizar o WebClient para " +
                                                           "integração do serviço x?")));


        BDDMockito.verify(this.topicService).updateTopic(1L, 3L, topicUpdateRequestDTO);
        BDDMockito.verifyNoMoreInteractions(this.topicService);


    }


    @DisplayName("Topic owner should edit your topic with success if is authenticated has authority 'topic:edit'")
    @Test
    void shouldEditTopicWithSuccessIfUserHasSuitableAuthority() throws Exception {
        final TopicUpdateRequestDTO topicUpdateRequestDTO = new TopicUpdateRequestDTO("Dúvida na utilização do WebClient",
                "Como utilizar o WebClient para integração do serviço x?",
                Status.UNSOLVED, 1L
        );

        Topic topic = TestsHelper.TopicHelper.topicList().get(0);
        topic.setTitle("Dúvida na utilização do WebClient");
        topic.setQuestion("Como utilizar o WebClient para integração do serviço x?");

        BDDMockito.given(this.topicService.updateTopic(1L, 1L, topicUpdateRequestDTO))
                .willReturn(new TopicResponseDTO(topic));

        this.mockMvc.perform(put("/forumhub.io/api/v1/topics/{topic_id}/edit", 1)
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(this.objectMapper
                                .writeValueAsString(topicUpdateRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic.title", is("Dúvida na utilização do WebClient")))
                .andExpect(jsonPath("$.topic.question", is("Como utilizar o WebClient para " +
                                                           "integração do serviço x?")));


        BDDMockito.verify(this.topicService).updateTopic(1L, 1L, topicUpdateRequestDTO);
        BDDMockito.verifyNoMoreInteractions(this.topicService);


    }


    @DisplayName("Should fail with status code 403 if user authenticated hasn't authority 'topic:delete'" +
                 " when delete topic")
    @Test
    void shouldFailIfUserHasNotSuitableAuthorityWhenDeleteTopic() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/topics/{topic_id}/delete", 1)
                        .with(jwt().jwt(JWT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

        BDDMockito.verifyNoInteractions(this.topicService);
    }

    @DisplayName("Should fail with status code 400 when delete topic with" +
                 " param different of type number")
    @Test
    void shouldFailToDeleteTopicIfParamDifferentOfTypeNumber() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/topics/{topic_id}/delete", "unexpected")
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());

        BDDMockito.verifyNoInteractions(this.topicService);

    }


    @DisplayName("Should delete topic with success if user authenticated has authority 'topic:delete'")
    @Test
    void shouldDeleteTopicWithSuccessIfUserHasSuitableAuthority() throws Exception {
        BDDMockito.doNothing().when(this.topicService).deleteTopic(1L, 1L);

        this.mockMvc.perform(delete("/forumhub.io/api/v1/topics/{topic_id}/delete", 1)
                        .with(jwt().jwt(JWT)
                                .authorities(new SimpleGrantedAuthority("SCOPE_topic:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"HttpStatusCode OK\"}"));

        BDDMockito.verify(this.topicService).deleteTopic(1L, 1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);

    }


}