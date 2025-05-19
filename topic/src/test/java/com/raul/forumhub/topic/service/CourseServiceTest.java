package com.raul.forumhub.topic.service;

import com.raul.forumhub.topic.domain.Course;
import com.raul.forumhub.topic.dto.request.CourseRequestDTO;
import com.raul.forumhub.topic.exception.InstanceNotFoundException;
import com.raul.forumhub.topic.repository.CourseRepository;
import com.raul.forumhub.topic.util.TestsHelper;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
class CourseServiceTest {

    @Mock
    CourseRepository courseRepository;

    @InjectMocks
    CourseService courseService;


    @Test
    void shouldFailToCreateCourseIfCourseNamePropertyIsEmpty() {
        final CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "", Course.Category.C);

        BDDMockito.given(this.courseRepository.save(any(Course.class)))
                .willThrow(ConstraintViolationException.class);


        Assertions.assertThrows(ConstraintViolationException.class,
                () -> this.courseService.createCourse(courseRequestDTO));


        BDDMockito.verify(this.courseRepository).save(any(Course.class));
        BDDMockito.verifyNoMoreInteractions(this.courseRepository);


    }


    @Test
    void shouldFailToCreateCourseIfHimAlreadyExists() {
        final CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "Criação de uma API Rest", Course.Category.JAVA);

        BDDMockito.given(this.courseRepository.save(any(Course.class)))
                .willThrow(new DataIntegrityViolationException("Payload conflitante"));


        Assertions.assertThrows(DataIntegrityViolationException.class,
                () -> this.courseService.createCourse(courseRequestDTO),
                "Payload conflitante");


        BDDMockito.verify(this.courseRepository).save(any(Course.class));
        BDDMockito.verifyNoMoreInteractions(this.courseRepository);


    }


    @Test
    void shouldCreateCourseWithSuccessIfEverythingIsOk() {
        final CourseRequestDTO courseRequestDTO = new CourseRequestDTO(
                "Conhecendo a arquitetura cliente servidor", Course.Category.JAVA);


        Assertions.assertDoesNotThrow(() -> this.courseService.createCourse(courseRequestDTO));


        BDDMockito.verify(this.courseRepository).save(any(Course.class));
        BDDMockito.verifyNoMoreInteractions(this.courseRepository);


    }


    @Test
    void shouldReturnAllCoursesCreatedWithSuccessful() {
        BDDMockito.given(this.courseRepository.findAll())
                .willReturn(TestsHelper.CourseHelper.courseList());


        Assertions.assertDoesNotThrow(() -> this.courseService.getAllCourse());


        BDDMockito.verify(this.courseRepository).findAll();
        BDDMockito.verifyNoMoreInteractions(this.courseRepository);

    }

    @Test
    void shouldRaiseExceptionWhenRequestCourseIfHimNotExists() {
        BDDMockito.given(this.courseRepository.findById(5L))
                .willThrow(new InstanceNotFoundException
                        (String.format("O curso [ID: %d] informado não existe", 5L)) );

        Assertions.assertThrows(InstanceNotFoundException.class, () -> this.courseService.getCourseById(5L));


        BDDMockito.verify(this.courseRepository).findById(5L);
        BDDMockito.verifyNoMoreInteractions(this.courseRepository);

    }

    @Test
    void shouldFailWhenGetCourseIfSpecifiedCourseNotExists(){
        BDDMockito.given(this.courseRepository.findById(5L))
                .willThrow(new InstanceNotFoundException(String.format("O curso [ID: %d] informado não existe", 5)));

        Assertions.assertThrows(InstanceNotFoundException.class, () -> this.courseService.getCourseById(5L),
                String.format("O curso [ID: %d] informado não existe", 5));

        BDDMockito.verify(this.courseRepository).findById(5L);
        BDDMockito.verifyNoMoreInteractions(this.courseRepository);
    }

    @Test
    void shouldGetCourseWithSuccessIfEvertythingIsOk(){
        BDDMockito.given(this.courseRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.CourseHelper.courseList().get(0)));

        Assertions.assertDoesNotThrow(() -> this.courseService.getCourseById(1L));

        BDDMockito.verify(this.courseRepository).findById(1L);
        BDDMockito.verifyNoMoreInteractions(this.courseRepository);
    }


    @Test
    void shouldFailIfCourseNamePropertyOfDtoObjectIsEmptyWhenEditCourse() {
        final CourseRequestDTO courseUpdateDTO = new CourseRequestDTO("", Course.Category.C);

        BDDMockito.given(this.courseRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.CourseHelper.courseList().get(0)));

        BDDMockito.given(this.courseRepository.save(any(Course.class)))
                .willThrow(ConstraintViolationException.class);


        Assertions.assertThrows(ConstraintViolationException.class,
                () -> this.courseService.updateCourse(1L, courseUpdateDTO));


        BDDMockito.verify(this.courseRepository).findById(1L);
        BDDMockito.verify(this.courseRepository).save(any(Course.class));
        BDDMockito.verifyNoMoreInteractions(this.courseRepository);
    }


    @Test
    void shouldFailToEditCourseIfDesiredCourseNotExists() {
        final CourseRequestDTO courseUpdateDTO =
                new CourseRequestDTO("Como criar uma API Rest escalável", Course.Category.C);


        BDDMockito.given(this.courseRepository.findById(1L))
                .willThrow(new InstanceNotFoundException("O curso informado não existe"));


        Assertions.assertThrows(InstanceNotFoundException.class,
                () -> this.courseService.updateCourse(1L, courseUpdateDTO));


        BDDMockito.verify(this.courseRepository).findById(
                1L);
        BDDMockito.verifyNoMoreInteractions(this.courseRepository);

    }


    @Test
    void shouldEditCourseWithSuccessIfEverythingIsOk() {
        final CourseRequestDTO courseUpdateDTO =
                new CourseRequestDTO("Como criar uma API Rest escalável", Course.Category.C);

        BDDMockito.given(this.courseRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.CourseHelper.courseList().get(0)));


        Assertions.assertDoesNotThrow(() -> this.courseService.updateCourse(1L, courseUpdateDTO));


        BDDMockito.verify(this.courseRepository).findById(1L);
        BDDMockito.verify(this.courseRepository).save(any(Course.class));
        BDDMockito.verifyNoMoreInteractions(this.courseRepository);

    }


    @Test
    void shouldFailToDeleteCourseIfDesiredCourseNotExists() {
        BDDMockito.given(this.courseRepository.findById(1L))
                .willThrow(new InstanceNotFoundException("O curso informado não existe"));


        Assertions.assertThrows(InstanceNotFoundException.class,
                () -> this.courseService.deleteCourse(1L));


        BDDMockito.verify(this.courseRepository).findById(1L);
        BDDMockito.verifyNoMoreInteractions(this.courseRepository);


    }


    @Test
    void shouldDeleteCourseWithSuccessIfEverythingIsOk() {
        BDDMockito.given(this.courseRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.CourseHelper.courseList().get(0)));


        Assertions.assertDoesNotThrow(() -> this.courseService.deleteCourse(1L));


        BDDMockito.verify(this.courseRepository).findById(1L);
        BDDMockito.verify(this.courseRepository).delete(any(Course.class));
        BDDMockito.verifyNoMoreInteractions(this.courseRepository);


    }
}