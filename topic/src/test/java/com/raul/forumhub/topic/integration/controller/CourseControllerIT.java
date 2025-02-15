package com.raul.forumhub.topic.integration.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.raul.forumhub.topic.domain.Course;
import com.raul.forumhub.topic.dto.request.CourseDTO;
import com.raul.forumhub.topic.repository.CourseRepository;
import com.raul.forumhub.topic.util.TestsHelper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

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
@Order(3)
public class CourseControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CourseRepository courseRepository;

    @MockBean
    ClientRegistrationRepository clientRegistrationRepository;

    private static boolean hasBeenInitialized = false;


    @BeforeEach
    void setup() {
        if (!hasBeenInitialized) {
            this.courseRepository.saveAll(TestsHelper.CourseHelper.courseList());
            hasBeenInitialized = true;
        }
    }


    @DisplayName("Should fail with status code 404 if resource doesn't exists")
    @Test
    void shouldFailIfResourceDoesNotExistToTheSendRequest() throws Exception {
        final CourseDTO courseDTO = new CourseDTO(
                "Conhecendo a arquitetura cliente/servidor", Course.Category.JAVA);

        this.mockMvc.perform(post("/forumhub.io/api/v1/courses/creat")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:create"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseDTO)))
                .andExpect(status().isNotFound());

    }

    @DisplayName("Should fail with status code 400 if method isn't supported")
    @Test
    void shouldFailIfMethodIsNotSupportedToTheSendRequest() throws Exception {
        final CourseDTO courseDTO = new CourseDTO(
                "Conhecendo a arquitetura cliente/servidor", Course.Category.JAVA);

        this.mockMvc.perform(put("/forumhub.io/api/v1/courses/create")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:create"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseDTO)))
                .andExpect(status().isBadRequest());
    }


    @DisplayName("Should fail with status code 401 when attempt create course if user unauthenticated")
    @Test
    void shouldFailToCreateCourseIfUnauthenticated() throws Exception {
        final CourseDTO courseDTO = new CourseDTO(
                "Conhecendo a arquitetura de microserviços", Course.Category.C);

        this.mockMvc.perform(post("/forumhub.io/api/v1/courses/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseDTO)))
                .andExpectAll(status().isUnauthorized());

        assertEquals(3, this.courseRepository.findAll().size());

    }


    @DisplayName("Should fail with status code 403 when attempt create course if user is ADM, but " +
            "hasn't authority course:create")
    @Test
    void shouldFailToCreateCourseIfUserIsADMButHasNotSuitableAuthority() throws Exception {
        final CourseDTO courseDTO = new CourseDTO(
                "Conhecendo a arquitetura de microserviços", Course.Category.C);

        this.mockMvc.perform(post("/forumhub.io/api/v1/courses/create")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseDTO)))
                .andExpectAll(status().isForbidden());

        assertEquals(3, this.courseRepository.findAll().size());

    }


    @DisplayName("Should fail with status code 403 when attempt create course if user has authority" +
            " course:create, but isn't ADM")
    @Test
    void shouldFailToCreateCourseIfUserHasSuitableAuthorityButNotIsADM() throws Exception {
        final CourseDTO courseDTO = new CourseDTO(
                "Conhecendo a arquitetura de microserviços", Course.Category.C);

        this.mockMvc.perform(post("/forumhub.io/api/v1/courses/create")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:create")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseDTO)))
                .andExpectAll(status().isForbidden());

        assertEquals(3, this.courseRepository.findAll().size());

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

        assertEquals(3, this.courseRepository.findAll().size());
    }


    @DisplayName("Should fail with status code 400 when create course if the course name property is empty")
    @Test
    void shouldFailToCreateCourseIfCourseNamePropertyIsEmpty() throws Exception {
        final CourseDTO courseDTO = new CourseDTO(
                "", Course.Category.C);

        this.mockMvc.perform(post("/forumhub.io/api/v1/courses/create")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:create"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseDTO)))
                .andExpectAll(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("O nome do curso não pode ser vazio")));

        assertEquals(3, this.courseRepository.findAll().size());

    }


    @DisplayName("Should fail with status code 409 when create course if her already exists")
    @Test
    void shouldFailToCreateCourseIfHerAlreadyExists() throws Exception {
        final CourseDTO courseDTO = new CourseDTO(
                "Criação de uma API Rest", Course.Category.JAVA);

        this.mockMvc.perform(post("/forumhub.io/api/v1/courses/create")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:create"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseDTO)))
                .andExpectAll(status().isConflict())
                .andExpect(jsonPath("$.detail", is("Payload conflitante")));

        assertEquals(3, this.courseRepository.findAll().size());

    }


    @Transactional
    @DisplayName("Should create course with success if user ADM authenticated, " +
            "has authority course:create and previous premisses are adequate")
    @Test
    void shouldCreateCourseWithSuccessIfAuthenticatedAndHasSuitableAuthority() throws Exception {
        final CourseDTO courseDTO = new CourseDTO(
                "Conhecendo a arquitetura cliente servidor", Course.Category.JAVA);

        this.mockMvc.perform(post("/forumhub.io/api/v1/courses/create")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:create"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().string("{\"message\":\"HttpStatusCode OK\"}"));

        assertEquals(4, this.courseRepository.findAll().size());


    }


    @DisplayName("Should fail with status code 401 when return all courses created if unauthenticated")
    @Test
    void shouldFailToReturnAllCoursesCreatedIfUnauthenticated() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/courses/listAll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnauthorized());

        assertEquals(3, this.courseRepository.findAll().size());

    }


    @DisplayName("Should return all courses created with successful if authenticated")
    @Test
    void shouldReturnAllCoursesCreatedWithSuccessful() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/courses/listAll")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..course.length()", is(3)));

        assertEquals(3, this.courseRepository.findAll().size());

    }


    @DisplayName("Should fail with status code 401 when edit course if unauthenticated")
    @Test
    void shouldFailToEditCourseIfUnauthenticated() throws Exception {
        final CourseDTO courseUpdateDTO = new CourseDTO("Como criar uma API Rest escalável", Course.Category.C);

        this.mockMvc.perform(put("/forumhub.io/api/v1/courses/edit")
                        .queryParam("courseName", "Criação de uma API Rest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseUpdateDTO)))
                .andExpect(status().isUnauthorized());

        assertFalse(this.courseRepository.findCourseByName(
                "Como criar uma API Rest escalável").isPresent());

    }


    @DisplayName("Should fail with status code 403 when attempt edit course if user is ADM, but " +
            "hasn't authority course:edit")
    @Test
    void shouldFailToEditCourseIfUserIsADMButHasNotSuitableAuthority() throws Exception {
        final CourseDTO courseUpdateDTO = new CourseDTO("Como criar uma API Rest escalável", Course.Category.C);

        this.mockMvc.perform(put("/forumhub.io/api/v1/courses/edit")
                        .queryParam("courseName", "Criação de uma API Rest")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseUpdateDTO)))
                .andExpect(status().isForbidden());

        assertFalse(this.courseRepository.findCourseByName(
                "Como criar uma API Rest escalável").isPresent());

    }


    @DisplayName("Should fail with status code 403 when attempt edit course if user has authority" +
            " course:edit, but isn't ADM")
    @Test
    void shouldFailToEditCourseIfUserHasSuitableAuthorityButNotIsADM() throws Exception {
        final CourseDTO courseUpdateDTO = new CourseDTO("Como criar uma API Rest escalável", Course.Category.C);

        this.mockMvc.perform(put("/forumhub.io/api/v1/courses/edit")
                        .queryParam("courseName", "Criação de uma API Rest")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_course:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseUpdateDTO)))
                .andExpect(status().isForbidden());

        assertFalse(this.courseRepository.findCourseByName(
                "Como criar uma API Rest escalável").isPresent());
    }

    @DisplayName("Should fail with status code 400 when attempt edit course if course name property " +
            "of DTO object is sent empty")
    @Test
    void shouldFailIfCourseNamePropertyOfDtoObjectIsEmptyWhenEditCourse() throws Exception {
        final CourseDTO courseUpdateDTO = new CourseDTO("", Course.Category.C);

        this.mockMvc.perform(put("/forumhub.io/api/v1/courses/edit")
                        .queryParam("courseName", "Criação de uma API Rest")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:edit"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseUpdateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("O nome do curso não pode ser vazio")));

        assertFalse(this.courseRepository.findCourseByName("").isPresent());
    }


    @DisplayName("Should fail with status code 400 when attempt edit course if course name property " +
            "of query param is sent empty")
    @Test
    void shouldFailIfCourseNamePropertyOfQueryParamIsEmptyWhenEditCourse() throws Exception {
        final CourseDTO courseUpdateDTO = new CourseDTO("Como criar uma API Rest escalável", Course.Category.C);

        this.mockMvc.perform(put("/forumhub.io/api/v1/courses/edit")
                        .queryParam("courseName", "")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:edit"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseUpdateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("O nome do curso não pode ser vazio")));

        assertFalse(this.courseRepository.findCourseByName(
                "Como criar uma API Rest escalável").isPresent());

    }

    @DisplayName("Should fail to edit course if desired course not exists")
    @Test
    void shouldFailToEditCourseIfDesiredCourseNotExists() throws Exception {
        final CourseDTO courseUpdateDTO = new CourseDTO("Como criar uma API Rest escalável", Course.Category.C);

        this.mockMvc.perform(put("/forumhub.io/api/v1/courses/edit")
                        .queryParam("courseName", "Lidando com load balancer na AWS")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:edit"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseUpdateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is("O curso informado não existe")));

        assertFalse(this.courseRepository.findCourseByName(
                "Como criar uma API Rest escalável").isPresent());

    }


    @Transactional
    @DisplayName("Should edit course with success if user ADM authenticated, " +
            "has authority course:edit and previous premisses are adequate")
    @Test
    void shouldEditCourseWithSuccessIfAuthenticatedAndHasSuitableAuthority() throws Exception {
        final CourseDTO courseUpdateDTO = new CourseDTO("Como criar uma API Rest escalável", Course.Category.C);

        this.mockMvc.perform(put("/forumhub.io/api/v1/courses/edit")
                        .queryParam("courseName", "Criação de uma API Rest")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:edit"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.course.name", is("Como criar uma API Rest escalável")));

        Assertions.assertAll(
                () -> assertTrue(this.courseRepository.findCourseByName(
                        "Como criar uma API Rest escalável").isPresent()),
                () -> assertFalse(this.courseRepository.findCourseByName(
                        "Criação de uma API Rest").isPresent())
        );
    }


    @DisplayName("Should fail with status code 401 when delete course if unauthenticated")
    @Test
    void shouldFailToDeleteCourseIfUnauthenticated() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/courses/delete")
                        .queryParam("courseName", "Gerenciamento de contêiners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnauthorized());

        assertEquals(3, this.courseRepository.findAll().size());

    }


    @DisplayName("Should fail with status code 403 when attempt delete course if user is ADM, but " +
            "hasn't authority course:delete")
    @Test
    void shouldFailToDeleteCourseIfUserIsADMButHasNotSuitableAuthority() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/courses/delete")
                        .queryParam("courseName", "Gerenciamento de contêiners")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

        assertEquals(3, this.courseRepository.findAll().size());

    }


    @DisplayName("Should fail with status code 403 when attempt delete course if user has authority" +
            " course:delete, but isn't ADM")
    @Test
    void shouldFailToDeleteCourseIfUserHasSuitableAuthorityButNotIsADM() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/courses/delete")
                        .queryParam("courseName", "Gerenciamento de contêiners")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_course:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

        assertEquals(3, this.courseRepository.findAll().size());

    }


    @DisplayName("Should fail with status code 400 when attempt delete course if course name property " +
            "of query param is sent empty")
    @Test
    void shouldFailIfCourseNamePropertyOfQueryParamIsEmptyWhenDeleteCourse() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/courses/delete")
                        .queryParam("courseName", "")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:delete"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("O nome do curso não pode ser vazio")));

        assertEquals(3, this.courseRepository.findAll().size());


    }


    @DisplayName("Should fail to delete course if desired course not exists")
    @Test
    void shouldFailToDeleteCourseIfDesiredCourseNotExists() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/courses/delete")
                        .queryParam("courseName", "Lidando com load balancer na AWS")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:delete"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is("O curso informado não existe")));

        Assertions.assertAll(
                () -> assertEquals(3, this.courseRepository.findAll().size()),
                () -> assertFalse(this.courseRepository.findCourseByName(
                        "Lidando com load balancer na AWS").isPresent())
        );

    }


    @Transactional
    @DisplayName("Should delete course with success if user ADM authenticated, " +
            "has authority course:delete and previous premisses are adequate")
    @Test
    void shouldDeleteCourseWithSuccessIfAuthenticatedAndHasSuitableAuthority() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/courses/delete")
                        .queryParam("courseName", "Gerenciamento de contêiners")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:delete"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"HttpStatusCode OK\"}"));

        assertEquals(2, this.courseRepository.findAll().size());

    }


}
