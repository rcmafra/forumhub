package com.raul.forumhub.topic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raul.forumhub.topic.domain.Course;
import com.raul.forumhub.topic.dto.request.CourseRequestDTO;
import com.raul.forumhub.topic.dto.request.TopicCreateRequestDTO;
import com.raul.forumhub.topic.dto.response.CourseResponseCollection;
import com.raul.forumhub.topic.dto.response.CourseResponseDTO;
import com.raul.forumhub.topic.exception.handler.GlobalExceptionHandler;
import com.raul.forumhub.topic.security.TopicSecurityConfig;
import com.raul.forumhub.topic.service.CourseService;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest
@ActiveProfiles(value = "test")
@ContextConfiguration(classes = {CourseController.class,
        TopicSecurityConfig.class, GlobalExceptionHandler.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CourseControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CourseService courseService;

    @MockBean
    ClientRegistrationRepository clientRegistrationRepository;


    @DisplayName("Should fail with status code 404 if resource doesn't exists")
    @Test
    void shouldFailIfResourceDoesNotExistToTheSendRequest() throws Exception {
        final TopicCreateRequestDTO topicCreateRequestDTO = new TopicCreateRequestDTO("Dúvida na utilização do Feign Client",
                "Como utilizar o Feign Client para integração do serviço x?",
                1L);

        this.mockMvc.perform(post("/forumhub.io/api/v1/topics/creat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicCreateRequestDTO)))
                .andExpectAll(status().isNotFound());

    }

    @DisplayName("Should fail with status code 400 if method isn't supported")
    @Test
    void shouldFailIfMethodIsNotSupportedToTheSendRequest() throws Exception {
        final TopicCreateRequestDTO topicCreateRequestDTO = new TopicCreateRequestDTO("Dúvida na utilização do Feign Client",
                "Como utilizar o Feign Client para integração do serviço x?",
                1L);

        this.mockMvc.perform(put("/forumhub.io/api/v1/courses/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(topicCreateRequestDTO)))
                .andExpectAll(status().isBadRequest());

    }

    @DisplayName("Should fail with status code 401 when attempt create course if user unauthenticated")
    @Test
    void shouldFailToCreateCourseIfUnauthenticated() throws Exception {
        final CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "Conhecendo a arquitetura de microserviços", Course.Category.C);

        this.mockMvc.perform(post("/forumhub.io/api/v1/courses/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseRequestDTO)))
                .andExpectAll(status().isUnauthorized());

        BDDMockito.verifyNoInteractions(this.courseService);

    }


    @DisplayName("Should fail with status code 403 when attempt create course if user is ADM, but " +
            "hasn't authority course:create")
    @Test
    void shouldFailToCreateCourseIfUserIsADMButHasNotSuitableAuthority() throws Exception {
        final CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "Conhecendo a arquitetura de microserviços", Course.Category.C);

        this.mockMvc.perform(post("/forumhub.io/api/v1/courses/create")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseRequestDTO)))
                .andExpectAll(status().isForbidden());

        BDDMockito.verifyNoInteractions(this.courseService);

    }


    @DisplayName("Should fail with status code 403 when attempt create course if user has authority" +
            " course:create, but isn't ADM")
    @Test
    void shouldFailToCreateCourseIfUserHasSuitableAuthorityButNotIsADM() throws Exception {
        final CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "Conhecendo a arquitetura de microserviços", Course.Category.C);

        this.mockMvc.perform(post("/forumhub.io/api/v1/courses/create")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:create")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseRequestDTO)))
                .andExpectAll(status().isForbidden());

        BDDMockito.verifyNoInteractions(this.courseService);

    }

    @DisplayName("Should fail with status code 400 when create course if category sent is" +
            " different of the enum types available")
    @Test
    void shouldFailToCreateCourseIfEnumTypeSentNonExists() throws Exception {
        String request = """
                    {
                        "name": "Conhecendo a arquitetura cliente servidor",
                        "category": "FORTRAN"
                    }
                """;

        this.mockMvc.perform(post("/forumhub.io/api/v1/courses/create")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:create"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(request))
                .andExpect(status().isBadRequest());

        BDDMockito.verifyNoInteractions(this.courseService);
    }


    @DisplayName("Should create course with success if user ADM authenticated " +
            "has authority course:create")
    @Test
    void shouldCreateCourseWithSuccessIfAuthenticatedAndHasSuitableAuthority() throws Exception {
        final CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "Conhecendo a arquitetura cliente servidor", Course.Category.JAVA);

        BDDMockito.doNothing().when(this.courseService).createCourse(courseRequestDTO);

        this.mockMvc.perform(post("/forumhub.io/api/v1/courses/create")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:create"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().string("{\"message\":\"HttpStatusCode OK\"}"));

        BDDMockito.verify(this.courseService).createCourse(courseRequestDTO);
        BDDMockito.verifyNoMoreInteractions(this.courseService);


    }


    @DisplayName("Should fail with status code 401 when return all courses created if unauthenticated")
    @Test
    void shouldFailToReturnAllCoursesCreatedIfUnauthenticated() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/courses/listAll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnauthorized());

        BDDMockito.verifyNoInteractions(this.courseService);

    }


    @DisplayName("Should return all courses created with successful if authenticated")
    @Test
    void shouldReturnAllCoursesCreatedWithSuccessful() throws Exception {
        BDDMockito.given(this.courseService.getAllCourse())
                .willReturn(Collections.singletonList(
                        new CourseResponseCollection(TestsHelper.CourseHelper.courseList())));

        this.mockMvc.perform(get("/forumhub.io/api/v1/courses/listAll")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..course.length()", is(3)));

        BDDMockito.verify(this.courseService).getAllCourse();
        BDDMockito.verifyNoMoreInteractions(this.courseService);

    }


    @DisplayName("Should fail with status code 401 when edit course if unauthenticated")
    @Test
    void shouldFailToEditCourseIfUnauthenticated() throws Exception {
        final CourseRequestDTO courseUpdateDTO = new CourseRequestDTO("Como criar uma API Rest escalável", Course.Category.C);

        this.mockMvc.perform(put("/forumhub.io/api/v1/courses/edit/{courseName}",
                        "Criação de uma API Rest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseUpdateDTO)))
                .andExpect(status().isUnauthorized());

        BDDMockito.verifyNoInteractions(this.courseService);

    }


    @DisplayName("Should fail with status code 403 when attempt edit course if user is ADM, but " +
            "hasn't authority course:edit")
    @Test
    void shouldFailToEditCourseIfUserIsADMButHasNotSuitableAuthority() throws Exception {
        final CourseRequestDTO courseUpdateDTO = new CourseRequestDTO("Como criar uma API Rest escalável", Course.Category.C);

        this.mockMvc.perform(put("/forumhub.io/api/v1/courses/edit/{courseName}",
                        "Criação de uma API Rest")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseUpdateDTO)))
                .andExpect(status().isForbidden());

        BDDMockito.verifyNoInteractions(this.courseService);

    }


    @DisplayName("Should fail with status code 403 when attempt edit course if user has authority" +
            " course:edit, but isn't ADM")
    @Test
    void shouldFailToEditCourseIfUserHasSuitableAuthorityButNotIsADM() throws Exception {
        final CourseRequestDTO courseUpdateDTO = new CourseRequestDTO("Como criar uma API Rest escalável", Course.Category.C);

        this.mockMvc.perform(put("/forumhub.io/api/v1/courses/edit/{courseName}",
                        "Criação de uma API Rest")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_course:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseUpdateDTO)))
                .andExpect(status().isForbidden());

        BDDMockito.verifyNoInteractions(this.courseService);

    }

    @DisplayName("Should edit course with success if user ADM authenticated has authority 'course:edit'")
    @Test
    void shouldEditCourseWithSuccessIfAuthenticatedAndHasSuitableAuthority() throws Exception {
        final CourseRequestDTO courseUpdateDTO = new CourseRequestDTO("Como criar uma API Rest escalável", Course.Category.C);

        Course course = TestsHelper.CourseHelper.courseList().get(0);
        course.setName("Como criar uma API Rest escalável");

        BDDMockito.given(this.courseService.updateCourse("Criação de uma API Rest", courseUpdateDTO))
                .willReturn(new CourseResponseDTO(course));

        this.mockMvc.perform(put("/forumhub.io/api/v1/courses/edit/{courseName}",
                        "Criação de uma API Rest")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:edit"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.course.name", is("Como criar uma API Rest escalável")));

        BDDMockito.verify(this.courseService).updateCourse("Criação de uma API Rest", courseUpdateDTO);
        BDDMockito.verifyNoMoreInteractions(this.courseService);

    }


    @DisplayName("Should fail with status code 401 when delete course if unauthenticated")
    @Test
    void shouldFailToDeleteCourseIfUnauthenticated() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/courses/delete/{courseName}",
                        "Gerenciamento de contêiners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnauthorized());

        BDDMockito.verifyNoInteractions(this.courseService);

    }


    @DisplayName("Should fail with status code 403 when attempt delete course if user is ADM, but " +
            "hasn't authority course:delete")
    @Test
    void shouldFailToDeleteCourseIfUserIsADMButHasNotSuitableAuthority() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/courses/delete/{courseName}",
                        "Gerenciamento de contêiners")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

        BDDMockito.verifyNoInteractions(this.courseService);

    }


    @DisplayName("Should fail with status code 403 when attempt delete course if user has authority" +
            " course:delete, but isn't ADM")
    @Test
    void shouldFailToDeleteCourseIfUserHasSuitableAuthorityButNotIsADM() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/courses/delete/{courseName}",
                        "Gerenciamento de contêiners")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_course:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

        BDDMockito.verifyNoInteractions(this.courseService);

    }

    @DisplayName("Should delete course with success if user ADM authenticated and " +
            "has authority 'course:delete'")
    @Test
    void shouldDeleteCourseWithSuccessIfAuthenticatedAndHasSuitableAuthority() throws Exception {
        BDDMockito.doNothing().when(this.courseService).deleteCourse("renciamento de contêiners");

        this.mockMvc.perform(delete("/forumhub.io/api/v1/courses/delete/{courseName}",
                        "Gerenciamento de contêiners")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:delete"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"HttpStatusCode OK\"}"));

        BDDMockito.verify(this.courseService).deleteCourse("Gerenciamento de contêiners");
        BDDMockito.verifyNoMoreInteractions(this.courseService);

    }

}
