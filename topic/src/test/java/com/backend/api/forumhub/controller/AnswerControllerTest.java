//package com.backend.api.forumhub.controller;
//
//import com.backend.api.forumhub.domain.Profile;
//import com.backend.api.forumhub.domain.User;
//import com.backend.api.forumhub.dto.request.AnswerTopicDTO;
//import com.backend.api.forumhub.service.AnswerService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import java.net.URISyntaxException;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@ExtendWith(MockitoExtension.class)
//class AnswerControllerTest {
//
//    MockMvc mockMvc;
//
//    @Mock
//    AnswerService answerService;
//
//    @InjectMocks
//    AnswerController answerController;
//
//    @BeforeEach
//    void setup() throws URISyntaxException {
//        mockMvc = MockMvcBuilders.standaloneSetup(answerController)
//                .alwaysDo(MockMvcResultHandlers.print()).build();
//    }
//
//    @Test
//    void shouldAnswerTheTopicWithSuccessfully() throws Exception {
//        User author = new User("Maria", "maria@email.com", new Profile(Profile.ProfileName.MOD));
//        AnswerTopicDTO answerTopicDTO = new AnswerTopicDTO(1L, "Respondido com sucesso", author);
//
//        Mockito.doNothing().when(answerService).answerTopic(any(), any());
//
//        mockMvc.perform(MockMvcRequestBuilders.
//                        post("/forumhub/v1/topics/{id}/answer", 1)
//                        .contentType(MediaType.APPLICATION_JSON)
//                .content(new ObjectMapper()
//                        .writeValueAsString(answerTopicDTO)))
//                .andExpectAll(status().isOk(),
//                    content().json("{\"message\":\"Topic answered with successfully\"}"))
//                .andReturn();
//
//        Mockito.verify(answerService).answerTopic(any(), any());
//        Mockito.verifyNoMoreInteractions(answerService);
//
//    }
//
//}