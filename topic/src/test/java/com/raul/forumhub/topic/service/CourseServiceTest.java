package com.raul.forumhub.topic.service;

import com.raul.forumhub.topic.domain.Course;
import com.raul.forumhub.topic.dto.request.CourseCreateDTO;
import com.raul.forumhub.topic.dto.request.CourseUpdateDTO;
import com.raul.forumhub.topic.exception.InstanceNotFoundException;
import com.raul.forumhub.topic.repository.CourseRepository;
import com.raul.forumhub.topic.util.TestsHelper;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CourseServiceTest {

    @Mock
    CourseRepository courseRepository;

    @InjectMocks
    CourseService courseService;

    @Order(1)
    @Test
    void shouldFailToCreateCourseIfCourseNamePropertyIsEmpty() {
        final CourseCreateDTO courseCreateDTO = new CourseCreateDTO(
                "", Course.Category.C);

        BDDMockito.given(this.courseRepository.save(any(Course.class)))
                .willThrow(ConstraintViolationException.class);


        Assertions.assertThrows(ConstraintViolationException.class,
                () -> this.courseService.createCourse(courseCreateDTO));


        BDDMockito.verify(this.courseRepository).save(any(Course.class));
        BDDMockito.verifyNoMoreInteractions(this.courseRepository);


    }


    @Order(2)
    @Test
    void shouldFailToCreateCourseIfHerAlreadyExists() {
        final CourseCreateDTO courseCreateDTO = new CourseCreateDTO(
                "Criação de uma API Rest", Course.Category.JAVA);

        BDDMockito.given(this.courseRepository.save(any(Course.class)))
                .willThrow(new DataIntegrityViolationException("Payload conflitante"));


        Assertions.assertThrows(DataIntegrityViolationException.class,
                () -> this.courseService.createCourse(courseCreateDTO),
                "Payload conflitante");


        BDDMockito.verify(this.courseRepository).save(any(Course.class));
        BDDMockito.verifyNoMoreInteractions(this.courseRepository);


    }

    @Order(3)
    @Test
    void shouldCreateCourseWithSuccessIfEverythingIsOk() {
        final CourseCreateDTO courseCreateDTO = new CourseCreateDTO(
                "Conhecendo a arquitetura cliente servidor", Course.Category.JAVA);


        Assertions.assertDoesNotThrow(() -> this.courseService.createCourse(courseCreateDTO));


        BDDMockito.verify(this.courseRepository).save(any(Course.class));
        BDDMockito.verifyNoMoreInteractions(this.courseRepository);


    }

    @Order(4)
    @Test
    void shouldReturnAllCoursesCreatedWithSuccessful() {
        BDDMockito.given(this.courseRepository.findAll())
                .willReturn(TestsHelper.CourseHelper.courseList());


        Assertions.assertDoesNotThrow(() -> this.courseService.getAllCourse());


        BDDMockito.verify(this.courseRepository).findAll();
        BDDMockito.verifyNoMoreInteractions(this.courseRepository);

    }

    @Order(5)
    @Test
    void shouldFailIfCourseNamePropertyOfDtoObjectIsEmptyWhenEditCourse() {
        final CourseUpdateDTO courseUpdateDTO = new CourseUpdateDTO("");

        BDDMockito.given(this.courseRepository.findCourseByName("Criação de uma API Rest"))
                .willReturn(Optional.of(TestsHelper.CourseHelper.courseList().get(0)));

        BDDMockito.given(this.courseRepository.save(any(Course.class)))
                .willThrow(ConstraintViolationException.class);


        Assertions.assertThrows(ConstraintViolationException.class,
                () -> this.courseService.updateNameCourse(
                        "Criação de uma API Rest", courseUpdateDTO));


        BDDMockito.verify(this.courseRepository).findCourseByName("Criação de uma API Rest");
        BDDMockito.verify(this.courseRepository).save(any(Course.class));
        BDDMockito.verifyNoMoreInteractions(this.courseRepository);
    }

    @Order(6)
    @Test
    void shouldFailToEditCourseIfDesiredCourseNotExists() {
        final CourseUpdateDTO courseUpdateDTO =
                new CourseUpdateDTO("Como criar uma API Rest escalável");


        BDDMockito.given(this.courseRepository.findCourseByName(
                        "Aprendendo sobre microserviços"))
                .willThrow(new InstanceNotFoundException("O curso informado não existe"));


        Assertions.assertThrows(InstanceNotFoundException.class,
                () -> this.courseService.updateNameCourse(
                        "Aprendendo sobre microserviços", courseUpdateDTO));


        BDDMockito.verify(this.courseRepository).findCourseByName(
                "Aprendendo sobre microserviços");
        BDDMockito.verifyNoMoreInteractions(this.courseRepository);

    }

    @Order(7)
    @Test
    void shouldEditCourseWithSuccessIfEverythingIsOk() {
        final CourseUpdateDTO courseUpdateDTO =
                new CourseUpdateDTO("Como criar uma API Rest escalável");

        BDDMockito.given(this.courseRepository.findCourseByName(
                        "Criação de uma API Rest"))
                .willReturn(Optional.of(TestsHelper.CourseHelper.courseList().get(0)));


        Assertions.assertDoesNotThrow(() -> this.courseService.updateNameCourse(
                "Criação de uma API Rest", courseUpdateDTO));


        BDDMockito.verify(this.courseRepository).findCourseByName("Criação de uma API Rest");
        BDDMockito.verify(this.courseRepository).save(any(Course.class));
        BDDMockito.verifyNoMoreInteractions(this.courseRepository);

    }

    @Order(8)
    @Test
    void shouldFailToDeleteCourseIfDesiredCourseNotExists() {
        BDDMockito.given(this.courseRepository.findCourseByName(
                        "Aprendendo sobre microserviços"))
                .willThrow(new InstanceNotFoundException("O curso informado não existe"));


        Assertions.assertThrows(InstanceNotFoundException.class,
                () -> this.courseService.deleteCourse("Aprendendo sobre microserviços"));


        BDDMockito.verify(this.courseRepository).findCourseByName(
                "Aprendendo sobre microserviços");
        BDDMockito.verifyNoMoreInteractions(this.courseRepository);


    }

    @Order(9)
    @Test
    void shouldDeleteCourseWithSuccessIfEverythingIsOk() {
        BDDMockito.given(this.courseRepository.findCourseByName(
                        "Criação de uma API Rest"))
                .willReturn(Optional.of(TestsHelper.CourseHelper.courseList().get(0)));


        Assertions.assertDoesNotThrow(() -> this.courseService.deleteCourse(
                "Criação de uma API Rest"));


        BDDMockito.verify(this.courseRepository).findCourseByName("Criação de uma API Rest");
        BDDMockito.verify(this.courseRepository).delete(any(Course.class));
        BDDMockito.verifyNoMoreInteractions(this.courseRepository);


    }
}