package com.raul.forumhub.topic.integration.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.raul.forumhub.topic.domain.Course;
import com.raul.forumhub.topic.dto.request.CourseRequestDTO;
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
        final CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "Conhecendo a arquitetura cliente/servidor", Course.Category.JAVA);

        this.mockMvc.perform(post("/forumhub.io/api/v1/courses/creat")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:write"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseRequestDTO)))
                .andExpect(status().isNotFound());

    }

    @DisplayName("Should fail with status code 400 if method isn't supported")
    @Test
    void shouldFailIfMethodIsNotSupportedToTheSendRequest() throws Exception {
        final CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "Conhecendo a arquitetura cliente/servidor", Course.Category.JAVA);

        this.mockMvc.perform(put("/forumhub.io/api/v1/courses/create")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:write"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseRequestDTO)))
                .andExpect(status().isBadRequest());
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

        assertEquals(4, this.courseRepository.findAll().size());

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

        assertEquals(4, this.courseRepository.findAll().size());

    }


    @DisplayName("Should fail with status code 403 when attempt create course if user has authority" +
                 " course:create, but isn't ADM")
    @Test
    void shouldFailToCreateCourseIfUserHasSuitableAuthorityButNotIsADM() throws Exception {
        final CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "Conhecendo a arquitetura de microserviços", Course.Category.C);

        this.mockMvc.perform(post("/forumhub.io/api/v1/courses/create")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseRequestDTO)))
                .andExpectAll(status().isForbidden());

        assertEquals(4, this.courseRepository.findAll().size());

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
                                new SimpleGrantedAuthority("SCOPE_course:write"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(request))
                .andExpect(status().isBadRequest());

        assertEquals(4, this.courseRepository.findAll().size());
    }


    @DisplayName("Should fail with status code 400 when create course if the course name property is empty")
    @Test
    void shouldFailToCreateCourseIfCourseNamePropertyIsEmpty() throws Exception {
        final CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "", Course.Category.C);

        this.mockMvc.perform(post("/forumhub.io/api/v1/courses/create")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:write"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseRequestDTO)))
                .andExpectAll(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("O nome do curso não pode ser vazio")));

        assertEquals(4, this.courseRepository.findAll().size());

    }


    @DisplayName("Should fail with status code 409 when create course if her already exists")
    @Test
    void shouldFailToCreateCourseIfHerAlreadyExists() throws Exception {
        final CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "Criação de uma API Rest", Course.Category.JAVA);

        this.mockMvc.perform(post("/forumhub.io/api/v1/courses/create")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:write"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseRequestDTO)))
                .andExpectAll(status().isConflict())
                .andExpect(jsonPath("$.detail", is("Payload conflitante com outro registro")));

        assertEquals(4, this.courseRepository.findAll().size());

    }


    @Transactional
    @DisplayName("Should create course with success if user ADM authenticated, " +
                 "has authority course:create and previous premisses are adequate")
    @Test
    void shouldCreateCourseWithSuccessIfAuthenticatedAndHasSuitableAuthority() throws Exception {
        final CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "Conhecendo a arquitetura cliente servidor", Course.Category.JAVA);

        this.mockMvc.perform(post("/forumhub.io/api/v1/courses/create")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:write"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseRequestDTO)))
                .andExpect(status().isCreated());

        assertEquals(5, this.courseRepository.findAll().size());


    }


    @DisplayName("Should fail with status code 401 when return all courses created if unauthenticated")
    @Test
    void shouldFailToReturnAllCoursesCreatedIfUnauthenticated() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/courses/listAll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnauthorized());

        assertEquals(4, this.courseRepository.findAll().size());

    }


    @DisplayName("Should return all courses created with successful if authenticated")
    @Test
    void shouldReturnAllCoursesCreatedWithSuccessful() throws Exception {
        this.mockMvc.perform(get("/forumhub.io/api/v1/courses/listAll")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..course.length()", is(4)));

        assertEquals(4, this.courseRepository.findAll().size());

    }


    @DisplayName("Should fail with status code 401 when edit course if unauthenticated")
    @Test
    void shouldFailToEditCourseIfUnauthenticated() throws Exception {
        final CourseRequestDTO courseUpdateDTO = new CourseRequestDTO("Como criar uma API Rest escalável", Course.Category.C);

        this.mockMvc.perform(put("/forumhub.io/api/v1/courses/{course_id}/edit", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseUpdateDTO)))
                .andExpect(status().isUnauthorized());

        assertNotEquals("Como criar uma API Rest escalável",
                this.courseRepository.findById(1L).get().getName());

    }


    @DisplayName("Should fail with status code 403 when attempt edit course if user is ADM, but " +
                 "hasn't authority course:edit")
    @Test
    void shouldFailToEditCourseIfUserIsADMButHasNotSuitableAuthority() throws Exception {
        final CourseRequestDTO courseUpdateDTO = new CourseRequestDTO("Como criar uma API Rest escalável", Course.Category.C);

        this.mockMvc.perform(put("/forumhub.io/api/v1/courses/{course_id}/edit", 1)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseUpdateDTO)))
                .andExpect(status().isForbidden());

        assertNotEquals("Como criar uma API Rest escalável",
                this.courseRepository.findById(1L).get().getName());

    }


    @DisplayName("Should fail with status code 403 when attempt edit course if user has authority" +
                 " course:edit, but isn't ADM")
    @Test
    void shouldFailToEditCourseIfUserHasSuitableAuthorityButNotIsADM() throws Exception {
        final CourseRequestDTO courseUpdateDTO = new CourseRequestDTO("Como criar uma API Rest escalável", Course.Category.C);

        this.mockMvc.perform(put("/forumhub.io/api/v1/courses/{course_id}/edit", 1L)
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_course:edit")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseUpdateDTO)))
                .andExpect(status().isForbidden());

        assertNotEquals("Como criar uma API Rest escalável",
                this.courseRepository.findById(1L).get().getName());
    }

    @DisplayName("Should fail with status code 400 when attempt edit course if course name property " +
                 "of DTO object is sent empty")
    @Test
    void shouldFailIfCourseNamePropertyOfDtoObjectIsEmptyWhenEditCourse() throws Exception {
        final CourseRequestDTO courseUpdateDTO = new CourseRequestDTO("", Course.Category.C);

        this.mockMvc.perform(put("/forumhub.io/api/v1/courses/{course_id}/edit", 1L)
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:edit"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseUpdateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("O nome do curso não pode ser vazio")));

        assertNotEquals("", this.courseRepository.findById(1L).get().getName());
    }

    @DisplayName("Should fail to edit course if desired course not exists")
    @Test
    void shouldFailToEditCourseIfDesiredCourseNotExists() throws Exception {
        final CourseRequestDTO courseUpdateDTO = new CourseRequestDTO("Como criar uma API Rest escalável", Course.Category.C);

        this.mockMvc.perform(put("/forumhub.io/api/v1/courses/{course_id}/edit", 5L)
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:edit"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseUpdateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is("O curso [ID: 5] informado não existe")));

        assertFalse(this.courseRepository.findById(5L).isPresent());

    }


    @Transactional
    @DisplayName("Should edit course with success if user ADM authenticated, " +
                 "has authority course:edit and previous premisses are adequate")
    @Test
    void shouldEditCourseWithSuccessIfAuthenticatedAndHasSuitableAuthority() throws Exception {
        final CourseRequestDTO courseUpdateDTO = new CourseRequestDTO("Como criar uma API Rest escalável", Course.Category.C);

        this.mockMvc.perform(put("/forumhub.io/api/v1/courses/{course_id}/edit", 1L)
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:edit"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(new ObjectMapper()
                                .writeValueAsString(courseUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.course.name", is("Como criar uma API Rest escalável")));

        assertEquals("Como criar uma API Rest escalável",
                this.courseRepository.findById(1L).get().getName());
    }


    @DisplayName("Should fail with status code 401 when delete course if unauthenticated")
    @Test
    void shouldFailToDeleteCourseIfUnauthenticated() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/courses/{course_id}/delete", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isUnauthorized());

        assertEquals(4, this.courseRepository.findAll().size());

    }


    @DisplayName("Should fail with status code 403 when attempt delete course if user is ADM, but " +
                 "hasn't authority course:delete")
    @Test
    void shouldFailToDeleteCourseIfUserIsADMButHasNotSuitableAuthority() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/courses/{course_id}/delete", 2L)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

        assertEquals(4, this.courseRepository.findAll().size());

    }


    @DisplayName("Should fail with status code 403 when attempt delete course if user has authority" +
                 " course:delete, but isn't ADM")
    @Test
    void shouldFailToDeleteCourseIfUserHasSuitableAuthorityButNotIsADM() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/courses/{course_id}/delete", 2L)
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_course:delete")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());

        assertEquals(4, this.courseRepository.findAll().size());

    }

    @DisplayName("Should fail to delete course if desired course not exists")
    @Test
    void shouldFailToDeleteCourseIfDesiredCourseNotExists() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/courses/{course_id}/delete", 5L)
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:delete"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is("O curso [ID: 5] informado não existe")));

        Assertions.assertAll(
                () -> assertEquals(4, this.courseRepository.findAll().size()),
                () -> assertFalse(this.courseRepository.findById(5L).isPresent())
        );

    }


    @Transactional
    @DisplayName("Should delete course with success if user ADM authenticated, " +
                 "has authority course:delete and previous premisses are adequate")
    @Test
    void shouldDeleteCourseWithSuccessIfAuthenticatedAndHasSuitableAuthority() throws Exception {
        this.mockMvc.perform(delete("/forumhub.io/api/v1/courses/{course_id}/delete", 2L)
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_course:delete"),
                                new SimpleGrantedAuthority("ROLE_ADM")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"HttpStatusCode OK\"}"));

        assertEquals(3, this.courseRepository.findAll().size());

    }


}
