//package com.backend.challenge.ForumHub.controller;
//
//import com.backend.challenge.ForumHub.domain.Answer;
//import com.backend.challenge.ForumHub.domain.GetCourseDTO;
//import com.backend.challenge.ForumHub.domain.Topic;
//import com.backend.challenge.ForumHub.domain.User;
//import com.backend.challenge.ForumHub.domain.enumerator.Category;
//import com.backend.challenge.ForumHub.domain.enumerator.Status;
//import com.backend.challenge.ForumHub.dto.request.TopicCreateDTO;
//import com.backend.challenge.ForumHub.dto.response.GetTopicDTO;
//import com.backend.challenge.ForumHub.service.TopicService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.nio.charset.StandardCharsets;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@ExtendWith(MockitoExtension.class)
//class TopicControllerTest {
//
//    MockMvc mockMvc;
//
//    @Mock
//    TopicService topicService;
//
//    @InjectMocks
//    TopicController topicController;
//
//    URI uri;
//
//    @BeforeEach
//    void setup() throws URISyntaxException {
//        mockMvc = MockMvcBuilders.standaloneSetup(topicController)
//                .alwaysDo(print()).build();
//        uri = new URI("/forumhub/v1/topics");
//    }
//
//    void shouldCreateATopicWithSuccessfully() throws Exception {
//        TopicCreateDTO createTopicDTO = new TopicCreateDTO("[Dúvida] Melhores práticas " +
//                "para autenticação em APIs REST",
//                "Quais são as melhores práticas para implementar autenticação " +
//                        "em APIs REST? OAuth é sempre a melhor escolha?", 1L, 1L);
//
//        Mockito.doNothing().when(topicService).createTopic(createTopicDTO);
//
//        mockMvc.perform(post(uri + "/createTopic")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .characterEncoding(StandardCharsets.UTF_8)
//                        .content(new ObjectMapper().writeValueAsString(createTopicDTO)))
//                .andExpectAll(status().isCreated(),
//                        content()
//                                .json("{\"message\":\"Topic created with successfully\"}"));
//
//    }
//
//    void shouldReturnAListOfTheTopics(){
//        User author1 = new User("Maria", "maria@email.com", "maria123");
//        User author2 = new User("Jose", "jose@email.com", "jose");
//        User author3 = new User("Raul", "raul@email.com", "raul");
//
//        GetCourseDTO course1 = new GetCourseDTO("Test1", Category.C);
//        GetCourseDTO course2 = new GetCourseDTO("Test2", Category.Cplusplus);
//        GetCourseDTO course3 = new GetCourseDTO("Test3", Category.GOLang);
//
//        Topic topic1 = new Topic("Test1", "Test1", author1, course1);
//        Topic topic2 = new Topic("Test2", "Test3", author2, course2);
//        Topic topic3 = new Topic("Test3", "Test4", author3, course3);
//
//        List<Answer> answerList = new ArrayList<>();
//        answerList.add(new Answer(topic1, "Test1", author1));
//        answerList.add(new Answer(topic2, "Test2", author2));
//        answerList.add(new Answer(topic3, "Test3", author3));
//
//        GetTopicDTO getTopicDTO1 = new GetTopicDTO(1L, "Test1", "Test1", LocalDateTime.now(), Status.UNSOLVED, author1, course1, answerList);
//        GetTopicDTO getTopicDTO2 = new GetTopicDTO(2L, "Test2", "Test2", LocalDateTime.now(), Status.UNSOLVED, author2, course2, answerList);
//        GetTopicDTO getTopicDTO3 = new GetTopicDTO(3L, "Test3", "Test3", LocalDateTime.now(), Status.UNSOLVED, author3, course3, answerList);
//
//        Mockito.when(topicService.topicList())
//
//
//    }
//}