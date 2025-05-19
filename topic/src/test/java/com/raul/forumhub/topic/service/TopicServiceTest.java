package com.raul.forumhub.topic.service;

import com.raul.forumhub.topic.client.UserClientRequest;
import com.raul.forumhub.topic.domain.Author;
import com.raul.forumhub.topic.domain.Status;
import com.raul.forumhub.topic.domain.Topic;
import com.raul.forumhub.topic.dto.request.TopicCreateRequestDTO;
import com.raul.forumhub.topic.dto.request.TopicUpdateRequestDTO;
import com.raul.forumhub.topic.dto.response.TopicResponseDTO;
import com.raul.forumhub.topic.exception.BusinessException;
import com.raul.forumhub.topic.exception.InstanceNotFoundException;
import com.raul.forumhub.topic.exception.PrivilegeValidationException;
import com.raul.forumhub.topic.exception.RestClientException;
import com.raul.forumhub.topic.repository.TopicRepository;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.TypeInformation;
import org.springframework.http.HttpStatus;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TopicServiceTest {

    @Mock
    TopicRepository topicRepository;

    @Mock
    UserClientRequest userClientRequest;

    @Mock
    CourseService courseService;

    @InjectMocks
    TopicService topicService;


    @Test
    void shouldFailIfTitlePropertyIsEmptyWhenCreateTopic() {
        final TopicCreateRequestDTO topicCreateRequestDTO = new TopicCreateRequestDTO("",
                "Como utilizar o Feign Client para integração do serviço x?",
                1L);

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willReturn(TestsHelper.CourseHelper.courseList().get(0));

        BDDMockito.given(this.topicRepository.save(any(Topic.class)))
                .willThrow(ConstraintViolationException.class);


        Assertions.assertThrows(ConstraintViolationException.class,
                () -> this.topicService.createTopic(topicCreateRequestDTO, 1L));


        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.topicRepository).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);

    }


    @Test
    void shouldFailIfQuestionPropertyIsEmptyWhenCreateTopic() {
        final TopicCreateRequestDTO topicCreateRequestDTO = new TopicCreateRequestDTO("Dúvida na utilização do Feign Client",
                "",
                1L);

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willReturn(TestsHelper.CourseHelper.courseList().get(0));

        BDDMockito.given(this.topicRepository.save(any(Topic.class)))
                .willThrow(ConstraintViolationException.class);


        Assertions.assertThrows(ConstraintViolationException.class,
                () -> this.topicService.createTopic(topicCreateRequestDTO, 1L));


        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.topicRepository).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);

    }


    @Test
    void shouldFailToCreateTopicIfTitlePropertyIsGreaterThan150Chars() {
        final TopicCreateRequestDTO topicCreateRequestDTO = new TopicCreateRequestDTO(
                "Qual é a diferença entre o Feign Client, RestTemplate e o WebClient no " +
                "Spring Framework e em que situações é mais adequado utilizá-los durante a " +
                "integração de um serviço?",
                "Diferença entre o Feign Client, RestTemplate e WebClient",
                1L);

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willReturn(TestsHelper.CourseHelper.courseList().get(0));

        BDDMockito.given(this.topicRepository.save(any(Topic.class)))
                .willThrow(new DataIntegrityViolationException("Payload com valor muito grande"));


        Assertions.assertThrows(DataIntegrityViolationException.class,
                () -> this.topicService.createTopic(topicCreateRequestDTO, 1L),
                "Payload com valor muito grande");


        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.topicRepository).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);

    }


    @Test
    void shouldFailToCreateTopicIfCourseNotExists() {
        final TopicCreateRequestDTO topicCreateRequestDTO = new TopicCreateRequestDTO("Dúvida na utilização do Feign Client",
                "Como utilizar o Feign Client para integração do serviço x?",
                1L);

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willThrow(new InstanceNotFoundException("O curso informado não existe"));


        Assertions.assertThrows(InstanceNotFoundException.class,
                () -> this.topicService.createTopic(topicCreateRequestDTO, 1L),
                "O curso informado não existe");


        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoInteractions(this.topicRepository);


    }


    @Test
    void shouldFailToCreateTopicIfUserServiceReturn404StatusCode() {
        final TopicCreateRequestDTO topicCreateRequestDTO = new TopicCreateRequestDTO("Dúvida na utilização do Feign Client",
                "Como utilizar o Feign Client para integração do serviço x?",
                1L);

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willThrow(new RestClientException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));


        Assertions.assertThrows(RestClientException.class,
                () -> this.topicService.createTopic(topicCreateRequestDTO, 1L),
                "Usuário não encontrado");


        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoInteractions(this.courseService);
        BDDMockito.verifyNoInteractions(this.topicRepository);


    }


    @Test
    void shouldCreateTopicWithSuccessIfEverythingIsOK() {
        final TopicCreateRequestDTO topicCreateRequestDTO = new TopicCreateRequestDTO("Dúvida na utilização do Feign Client",
                "Como utilizar o Feign Client para integração do serviço x?",
                1L);

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willReturn(TestsHelper.CourseHelper.courseList().get(0));


        Assertions.assertDoesNotThrow(() -> this.topicService.createTopic(topicCreateRequestDTO, 1L));


        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.topicRepository).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);

    }

    @Test
    void shouldFailWhenRequestAllTopicIfSortPropertyValueNotExists() {
        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "unexpected"));

        BDDMockito.given(this.topicRepository.findAll(pageable))
                .willThrow(new PropertyReferenceException("unexpected",
                        TypeInformation.of(Topic.class), List.of()));

        Assertions.assertThrows(PropertyReferenceException.class,
                () -> this.topicService.topicList(pageable));

        BDDMockito.verify(this.topicRepository).findAll(pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
    }

    @Test
    void shouldReturnAllTopicsUnsortedWithSuccessful() {
        Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());

        List<Topic> topicList = TestsHelper.TopicHelper.topicListWithAnswers();

        Page<TopicResponseDTO> topicPage =
                new PageImpl<>(topicList, pageable, 4)
                        .map(TopicResponseDTO::new);

        BDDMockito.given(this.topicRepository.findAll(pageable))
                .willReturn(new PageImpl<>(topicList, Pageable.unpaged(), 4));


        Assertions.assertDoesNotThrow(() -> this.topicService.topicList(pageable));


        Assertions.assertAll(
                () -> assertEquals(4, topicPage.getContent().size()),
                () -> assertEquals(10, topicPage.getSize()),
                () -> assertEquals(4, topicPage.getTotalElements()),
                () -> assertEquals(1, topicPage.getTotalPages())
        );

        BDDMockito.verify(this.topicRepository).findAll(pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);

    }


    @Test
    void shouldReturnAllTopicsSortedDescendantByCreateDateWithSuccessful() {
        List<Topic> sortedTopicByCreatedAt = TestsHelper.TopicHelper.topicListWithAnswers()
                .stream().sorted(Comparator.comparing(Topic::getCreatedAt).reversed())
                .toList();

        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<TopicResponseDTO> topicPage =
                new PageImpl<>(sortedTopicByCreatedAt, pageable, 4)
                        .map(TopicResponseDTO::new);

        BDDMockito.given(this.topicRepository.findAll(pageable))
                .willReturn(new PageImpl<>(sortedTopicByCreatedAt, pageable, 4));


        Assertions.assertDoesNotThrow(() -> this.topicService.topicList(pageable));


        Assertions.assertAll(
                () -> Assertions.assertEquals(3L, topicPage.getContent().get(0).topic().getId()),
                () -> Assertions.assertEquals(1L, topicPage.getContent().get(1).topic().getId()),
                () -> Assertions.assertEquals(2L, topicPage.getContent().get(2).topic().getId()),
                () -> Assertions.assertEquals(4L, topicPage.getContent().get(3).topic().getId()),
                () -> Assertions.assertEquals(0, topicPage.getNumber()),
                () -> Assertions.assertEquals(4, topicPage.getContent().size()),
                () -> Assertions.assertEquals(10, topicPage.getSize()),
                () -> Assertions.assertEquals(4, topicPage.getTotalElements()),
                () -> Assertions.assertEquals(1, topicPage.getTotalPages())
        );

        BDDMockito.verify(this.topicRepository).findAll(pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);

    }


    @Test
    void shouldReturnTwoTopicsSortedAscendantByStatusWithSuccessful() {
        List<Topic> sortedTopicByStatus = TestsHelper.TopicHelper.topicListWithAnswers()
                .stream().filter(topic -> topic.getId().equals(2L) || topic.getId().equals(3L))
                .sorted(Comparator.comparing(Topic::getStatus))
                .toList();

        Pageable pageable = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.ASC, "status"));

        Page<TopicResponseDTO> topicPage =
                new PageImpl<>(sortedTopicByStatus, pageable, 2)
                        .map(TopicResponseDTO::new);

        BDDMockito.given(this.topicRepository.findAll(pageable))
                .willReturn(new PageImpl<>(sortedTopicByStatus, pageable, 2));


        Assertions.assertDoesNotThrow(() -> this.topicService.topicList(pageable));


        Assertions.assertAll(
                () -> Assertions.assertEquals(3L, topicPage.getContent().get(0).topic().getId()),
                () -> Assertions.assertEquals(Status.SOLVED, topicPage.getContent().get(0).topic().getStatus()),
                () -> Assertions.assertEquals(2L, topicPage.getContent().get(1).topic().getId()),
                () -> Assertions.assertEquals(Status.UNSOLVED, topicPage.getContent().get(1).topic().getStatus()),
                () -> Assertions.assertEquals(0, topicPage.getNumber()),
                () -> Assertions.assertEquals(2, topicPage.getContent().size()),
                () -> Assertions.assertEquals(2, topicPage.getSize()),
                () -> Assertions.assertEquals(2, topicPage.getTotalElements()),
                () -> Assertions.assertEquals(1, topicPage.getTotalPages())
        );

        BDDMockito.verify(this.topicRepository).findAll(pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);

    }


    @Test
    void shouldReturnAllTopicsSortedAscendantByTitleWithSuccessful() {
        List<Topic> sortedTopicByTitle = TestsHelper.TopicHelper.topicListWithAnswers()
                .stream().sorted(Comparator.comparing(Topic::getTitle))
                .toList();

        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.ASC, "title"));

        Page<TopicResponseDTO> topicPage =
                new PageImpl<>(sortedTopicByTitle, pageable, 4)
                        .map(TopicResponseDTO::new);

        BDDMockito.given(this.topicRepository.findAll(pageable))
                .willReturn(new PageImpl<>(sortedTopicByTitle, pageable, 4));


        Assertions.assertDoesNotThrow(() -> this.topicService.topicList(pageable));


        Assertions.assertAll(
                () -> Assertions.assertEquals(3L, topicPage.getContent().get(0).topic().getId()),
                () -> Assertions.assertEquals(1L, topicPage.getContent().get(1).topic().getId()),
                () -> Assertions.assertEquals(2L, topicPage.getContent().get(2).topic().getId()),
                () -> Assertions.assertEquals(4L, topicPage.getContent().get(3).topic().getId()),
                () -> Assertions.assertEquals(0, topicPage.getNumber()),
                () -> Assertions.assertEquals(4, topicPage.getContent().size()),
                () -> Assertions.assertEquals(10, topicPage.getSize()),
                () -> Assertions.assertEquals(4, topicPage.getTotalElements()),
                () -> Assertions.assertEquals(1, topicPage.getTotalPages())
        );

        BDDMockito.verify(this.topicRepository).findAll(pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);

    }

    @Test
    void shouldReturnPageWithoutContentWhenIsNotExistNoneTopicAssociatedToTheCourse() {
        List<Topic> topicsListByCourse = TestsHelper.TopicHelper.topicListWithAnswers()
                .stream().filter(topic -> topic.getCourse().getId().equals(4L))
                .toList();

        Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());

        Page<TopicResponseDTO> topicPage =
                new PageImpl<>(topicsListByCourse, pageable, 0)
                        .map(TopicResponseDTO::new);

        BDDMockito.given(this.topicRepository.findTopicsByCourseId(4L, pageable))
                .willReturn(new PageImpl<>(topicsListByCourse, Pageable.unpaged(), 0));


        Assertions.assertDoesNotThrow(() -> this.topicService.topicsListByCourse(4L, pageable));


        Assertions.assertAll(
                () -> assertEquals(0, topicPage.getContent().size()),
                () -> assertEquals(10, topicPage.getSize()),
                () -> assertEquals(0, topicPage.getTotalElements()),
                () -> assertEquals(0, topicPage.getTotalPages())
        );

        BDDMockito.verify(this.topicRepository).findTopicsByCourseId(4L, pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);


    }

    @Test
    void shouldReturnTopicsByCourseUnsortedWithSuccessful() {
        List<Topic> topicsListByCourse = TestsHelper.TopicHelper.topicListWithAnswers()
                .stream().filter(topic -> topic.getCourse().getId().equals(1L))
                .toList();

        Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());

        Page<TopicResponseDTO> topicPage =
                new PageImpl<>(topicsListByCourse, pageable, 2)
                        .map(TopicResponseDTO::new);

        BDDMockito.given(this.topicRepository.findTopicsByCourseId(1L, pageable))
                .willReturn(new PageImpl<>(topicsListByCourse, pageable, 2));


        Assertions.assertDoesNotThrow(() -> this.topicService.topicsListByCourse(1L, pageable));


        Assertions.assertAll(
                () -> assertEquals(2, topicPage.getContent().size()),
                () -> assertEquals(10, topicPage.getSize()),
                () -> assertEquals(2, topicPage.getTotalElements()),
                () -> assertEquals(1, topicPage.getTotalPages())
        );

        BDDMockito.verify(this.topicRepository).findTopicsByCourseId(1L, pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);

    }


    @Test
    void shouldReturnTopicsByCourseSortedDescendantByCreateDateWithSuccessful() {
        List<Topic> sortedTopicByCreatedAt = TestsHelper.TopicHelper.topicListWithAnswers()
                .stream().filter(topic -> topic.getCourse().getId().equals(1L))
                .sorted(Comparator.comparing(Topic::getCreatedAt).reversed())
                .toList();


        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "createdAt"));


        Page<TopicResponseDTO> topicPage =
                new PageImpl<>(sortedTopicByCreatedAt, pageable, 2)
                        .map(TopicResponseDTO::new);

        BDDMockito.given(this.topicRepository.findTopicsByCourseId(1L, pageable))
                .willReturn(new PageImpl<>(sortedTopicByCreatedAt, pageable, 2));


        Assertions.assertDoesNotThrow(() -> this.topicService.topicsListByCourse(1L, pageable));


        Assertions.assertAll(
                () -> Assertions.assertEquals(1L, topicPage.getContent().get(0).topic().getId()),
                () -> Assertions.assertEquals(4L, topicPage.getContent().get(1).topic().getId()),
                () -> Assertions.assertEquals(0, topicPage.getNumber()),
                () -> Assertions.assertEquals(2, topicPage.getContent().size()),
                () -> Assertions.assertEquals(10, topicPage.getSize()),
                () -> Assertions.assertEquals(2, topicPage.getTotalElements()),
                () -> Assertions.assertEquals(1, topicPage.getTotalPages())
        );

        BDDMockito.verify(this.topicRepository).findTopicsByCourseId(1L, pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);

    }


    @Test
    void shouldReturnOneTopicByCourseSortedAscendantByStatusWithSuccessful() {
        List<Topic> sortedTopicByStatus = TestsHelper.TopicHelper.topicListWithAnswers()
                .stream().filter(topic -> topic.getId().equals(1L))
                .sorted(Comparator.comparing(Topic::getStatus))
                .toList();

        Pageable pageable = PageRequest.of(0, 1,
                Sort.by(Sort.Direction.ASC, "status"));

        Page<TopicResponseDTO> topicPage =
                new PageImpl<>(sortedTopicByStatus, pageable, 1)
                        .map(TopicResponseDTO::new);

        BDDMockito.given(this.topicRepository.findTopicsByCourseId(1L, pageable))
                .willReturn(new PageImpl<>(sortedTopicByStatus, pageable, 1));


        Assertions.assertDoesNotThrow(() -> this.topicService.topicsListByCourse(1L, pageable));


        Assertions.assertAll(
                () -> Assertions.assertEquals(Status.UNSOLVED, topicPage.getContent().get(0).topic().getStatus()),
                () -> Assertions.assertEquals(0, topicPage.getNumber()),
                () -> Assertions.assertEquals(1, topicPage.getContent().size()),
                () -> Assertions.assertEquals(1, topicPage.getSize()),
                () -> Assertions.assertEquals(1, topicPage.getTotalElements()),
                () -> Assertions.assertEquals(1, topicPage.getTotalPages())
        );

        BDDMockito.verify(this.topicRepository).findTopicsByCourseId(1L, pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);

    }


    @Test
    void shouldReturnTopicsByCourseSortedAscendantByTitleWithSuccessful() {
        List<Topic> sortedTopicByTitle = TestsHelper.TopicHelper.topicListWithAnswers()
                .stream().filter(topic -> topic.getCourse().getId().equals(1L))
                .sorted(Comparator.comparing(Topic::getTitle))
                .toList();

        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.ASC, "title"));

        Page<TopicResponseDTO> topicPage =
                new PageImpl<>(sortedTopicByTitle, pageable, 2)
                        .map(TopicResponseDTO::new);

        BDDMockito.given(this.topicRepository.findTopicsByCourseId(1L, pageable))
                .willReturn(new PageImpl<>(sortedTopicByTitle, pageable, 2));


        Assertions.assertDoesNotThrow(() -> this.topicService.topicsListByCourse(1L, pageable));


        Assertions.assertAll(
                () -> Assertions.assertEquals(1L, topicPage.getContent().get(0).topic().getId()),
                () -> Assertions.assertEquals(4L, topicPage.getContent().get(1).topic().getId()),
                () -> Assertions.assertEquals(0, topicPage.getNumber()),
                () -> Assertions.assertEquals(2, topicPage.getContent().size()),
                () -> Assertions.assertEquals(10, topicPage.getSize()),
                () -> Assertions.assertEquals(2, topicPage.getTotalElements()),
                () -> Assertions.assertEquals(1, topicPage.getTotalPages())
        );

        BDDMockito.verify(this.topicRepository).findTopicsByCourseId(1L, pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);

    }


    @Test
    void shouldFailToRequestTheSpecifiedTopicIfNotExists() {
        BDDMockito.given(this.topicRepository.findById(1L))
                .willThrow(new InstanceNotFoundException(String.format("O tópico [ID: %d] informado não existe", 1)));


        Assertions.assertThrows(InstanceNotFoundException.class,
                () -> this.topicService.getTopicById(1L),
                String.format("O tópico [ID: %d] informado não existe", 1));


        BDDMockito.verify(this.topicRepository).findById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);


    }


    @Test
    void shouldReturnTheSpecifiedTopicWithSuccessful() {
        Topic topic = TestsHelper.TopicHelper.topicList().get(0);

        BDDMockito.given(this.topicRepository.findById(1L))
                .willReturn(Optional.of(topic));


        Assertions.assertDoesNotThrow(() -> this.topicService.getTopicById(1L));


        Assertions.assertAll(
                () -> assertEquals(1L, topic.getId()),
                () -> assertEquals("Dúvida na utilização do Feign Client", topic.getTitle())
        );

        BDDMockito.verify(this.topicRepository).findById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);

    }


    @Test
    void shouldFailIfTitlePropertyIsEmptyWhenEditTopic() {
        final TopicUpdateRequestDTO topicUpdateRequestDTO = new TopicUpdateRequestDTO(
                "",
                "Como posso integrar minha API com o Elasticsearch para monitoração?",
                Status.UNSOLVED, 1L
        );

        BDDMockito.given(this.topicRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.TopicHelper.topicList().get(0)));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willReturn(TestsHelper.CourseHelper.courseList().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        BDDMockito.given(this.topicRepository.save(any(Topic.class)))
                .willThrow(ConstraintViolationException.class);


        Assertions.assertThrows(ConstraintViolationException.class,
                () -> this.topicService.updateTopic(1L, 2L, topicUpdateRequestDTO));


        BDDMockito.verify(this.topicRepository).findById(1L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verify(this.topicRepository).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }


    @Test
    void shouldFailIfQuestionPropertyIsEmptyWhenEditTopic() {
        TopicUpdateRequestDTO topicUpdateRequestDTO = new TopicUpdateRequestDTO(
                "Dúvida quanto a utilização do Elasticsearch",
                "",
                Status.UNSOLVED, 1L
        );

        BDDMockito.given(this.topicRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.TopicHelper.topicList().get(0)));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willReturn(TestsHelper.CourseHelper.courseList().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        BDDMockito.given(this.topicRepository.save(any(Topic.class)))
                .willThrow(ConstraintViolationException.class);


        Assertions.assertThrows(ConstraintViolationException.class,
                () -> this.topicService.updateTopic(1L, 2L, topicUpdateRequestDTO));


        BDDMockito.verify(this.topicRepository).findById(1L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verify(this.topicRepository).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }

    @Test
    void shouldFailToEditTopicIfTopicNotExists() {
        final TopicUpdateRequestDTO topicUpdateRequestDTO = new TopicUpdateRequestDTO(
                "Dúvida quanto a utilização do Elasticsearch",
                "Como posso integrar minha API com o Elasticsearch para monitoração?",
                Status.UNSOLVED, 1L
        );

        BDDMockito.given(this.topicRepository.findById(1L))
                .willThrow(new InstanceNotFoundException("O tópico informado não existe"));

        Assertions.assertThrows(InstanceNotFoundException.class,
                () -> this.topicService.updateTopic(1L, 1L, topicUpdateRequestDTO),
                "O tópico informado não existe");

        BDDMockito.verify(this.topicRepository).findById(1L);
        BDDMockito.verifyNoInteractions(this.courseService);
        BDDMockito.verifyNoInteractions(this.userClientRequest);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);

    }

    @Test
    void shouldFailToEditTopicIfCourseNotExists() {
        final TopicUpdateRequestDTO topicUpdateRequestDTO = new TopicUpdateRequestDTO(
                "Dúvida quanto a utilização do Elasticsearch",
                "Como posso integrar minha API com o Elasticsearch para monitoração?",
                Status.UNSOLVED, 1L
        );

        BDDMockito.given(this.topicRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.TopicHelper.topicList().get(0)));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willThrow(new InstanceNotFoundException("O curso informado não existe"));

        Assertions.assertThrows(InstanceNotFoundException.class,
                () -> this.topicService.updateTopic(1L, 1L, topicUpdateRequestDTO),
                "O curso informado não existe");

        BDDMockito.verify(this.topicRepository).findById(1L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.topicRepository, BDDMockito.never()).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }


    @Test
    void shouldFailToEditTopicIfUserServiceReturn404StatusCode() {
        final TopicUpdateRequestDTO topicUpdateRequestDTO = new TopicUpdateRequestDTO(
                "Dúvida quanto a utilização do Elasticsearch",
                "Como posso integrar minha API com o Elasticsearch para monitoração?",
                Status.UNSOLVED, 1L
        );

        BDDMockito.given(this.topicRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.TopicHelper.topicList().get(0)));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willReturn(TestsHelper.CourseHelper.courseList().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willThrow(new RestClientException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        Assertions.assertThrows(RestClientException.class,
                () -> this.topicService.updateTopic(1L, 1L, topicUpdateRequestDTO),
                "Usuário não encontrado");


        BDDMockito.verify(this.topicRepository).findById(1L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verify(this.topicRepository, BDDMockito.never()).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }


    @Test
    void shouldFailIfBasicUserAttemptEditTopicOfOtherAuthor() {
        final TopicUpdateRequestDTO topicUpdateRequestDTO = new TopicUpdateRequestDTO(
                "Dúvida quanto a utilização do Elasticsearch",
                "Como posso integrar minha API com o Elasticsearch para monitoração?",
                Status.UNSOLVED, 1L
        );

        BDDMockito.given(this.topicRepository.findById(2L))
                .willReturn(Optional.of(TestsHelper.TopicHelper.topicList().get(1)));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willReturn(TestsHelper.CourseHelper.courseList().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        Assertions.assertThrows(PrivilegeValidationException.class,
                () -> this.topicService.updateTopic(2L, 1L, topicUpdateRequestDTO),
                "Usuário com privilégios insuficientes para realizar esta operação!");


        BDDMockito.verify(this.topicRepository).findById(2L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verify(this.topicRepository, BDDMockito.never()).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @Test
    void shouldFailWhenAttemptEditTopicOfUnknownAuthor() {
        final TopicUpdateRequestDTO topicUpdateRequestDTO = new TopicUpdateRequestDTO(
                "Dúvida quanto a utilização do Elasticsearch",
                "Como posso integrar minha API com o Elasticsearch para monitoração?",
                Status.SOLVED, 1L
        );

        BDDMockito.given(this.topicRepository.findById(3L))
                .willReturn(Optional.of(TestsHelper.TopicHelper.topicList().get(2)));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willReturn(TestsHelper.CourseHelper.courseList().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(3L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(2));

        Assertions.assertThrows(BusinessException.class,
                () -> this.topicService.updateTopic(3L, 3L, topicUpdateRequestDTO),
                "O tópico pertence a um autor inexistente, ele não pode ser editado!");


        BDDMockito.verify(this.topicRepository).findById(3L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(3L);
        BDDMockito.verify(this.topicRepository, BDDMockito.never()).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }


    @Test
    void shouldEditTopicWithSuccessOfUnknownAuthorWhenAuthorIdIsNotOne() {
        final TopicUpdateRequestDTO topicUpdateRequestDTO = new TopicUpdateRequestDTO(
                "Dúvida quanto a utilização do Elasticsearch",
                "Como posso integrar minha API com o Elasticsearch para monitoração?",
                Status.SOLVED, 1L
        );

        Topic topic = TestsHelper.TopicHelper.topicList().get(2);
        Author anonymous = TestsHelper.AuthorHelper.authorList().get(0);
        anonymous.setId(5L);
        topic.setAuthor(anonymous);

        BDDMockito.given(this.topicRepository.findById(3L))
                .willReturn(Optional.of(topic));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willReturn(TestsHelper.CourseHelper.courseList().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(3L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(2));

        Assertions.assertDoesNotThrow(() ->
                this.topicService.updateTopic(3L, 3L, topicUpdateRequestDTO));


        BDDMockito.verify(this.topicRepository).findById(3L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(3L);
        BDDMockito.verify(this.topicRepository).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }

    @Test
    void shouldEditTopicWithSuccessOfUnknownAuthorWhenUsernameIsNotAnonymous() {
        final TopicUpdateRequestDTO topicUpdateRequestDTO = new TopicUpdateRequestDTO(
                "Dúvida quanto a utilização do Elasticsearch",
                "Como posso integrar minha API com o Elasticsearch para monitoração?",
                Status.SOLVED, 1L
        );

        Topic topic = TestsHelper.TopicHelper.topicList().get(2);
        Author anonymous = TestsHelper.AuthorHelper.authorList().get(0);
        anonymous.setUsername("João");
        topic.setAuthor(anonymous);

        BDDMockito.given(this.topicRepository.findById(3L))
                .willReturn(Optional.of(topic));

        BDDMockito.given(this.topicRepository.findById(3L))
                .willReturn(Optional.of(topic));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willReturn(TestsHelper.CourseHelper.courseList().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(3L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(2));

        Assertions.assertDoesNotThrow(() ->
                this.topicService.updateTopic(3L, 3L, topicUpdateRequestDTO));


        BDDMockito.verify(this.topicRepository).findById(3L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(3L);
        BDDMockito.verify(this.topicRepository).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }

    @Test
    void topicAuthorShouldEditSpecifiedTopicWithSuccess() {
        final TopicUpdateRequestDTO topicUpdateRequestDTO = new TopicUpdateRequestDTO(
                "Dúvida na utilização do WebClient",
                "Como utilizar o WebClient para integração do serviço x?",
                Status.UNSOLVED, 1L
        );

        BDDMockito.given(this.topicRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.TopicHelper.topicList().get(0)));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willReturn(TestsHelper.CourseHelper.courseList().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        Assertions.assertDoesNotThrow(
                () -> this.topicService.updateTopic(1L, 2L, topicUpdateRequestDTO));


        BDDMockito.verify(this.topicRepository).findById(1L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verify(this.topicRepository).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @Test
    void userADMShouldEditTopicOfOtherAuthorWithSuccess() {
        final TopicUpdateRequestDTO topicUpdateRequestDTO = new TopicUpdateRequestDTO(
                "Dúvida na utilização do RestTemplate",
                "Como utilizar o RestTemplate para integração do serviço x?",
                Status.UNSOLVED, 1L
        );

        BDDMockito.given(this.topicRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.TopicHelper.topicList().get(0)));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willReturn(TestsHelper.CourseHelper.courseList().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(4L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(3));

        Assertions.assertDoesNotThrow(
                () -> this.topicService.updateTopic(1L, 4L, topicUpdateRequestDTO));


        BDDMockito.verify(this.topicRepository).findById(1L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(4L);
        BDDMockito.verify(this.topicRepository).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @Test
    void userMODShouldEditTopicOfOtherAuthorWithSuccess() {
        final TopicUpdateRequestDTO topicUpdateRequestDTO = new TopicUpdateRequestDTO(
                "Dúvida na utilização da API de validação do Spring",
                "Quais são as anotações da API de validação do Spring?",
                Status.UNSOLVED, 1L
        );

        BDDMockito.given(this.topicRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.TopicHelper.topicList().get(0)));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willReturn(TestsHelper.CourseHelper.courseList().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(3L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(2));

        Assertions.assertDoesNotThrow(
                () -> this.topicService.updateTopic(1L, 3L, topicUpdateRequestDTO));


        BDDMockito.verify(this.topicRepository).findById(1L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(3L);
        BDDMockito.verify(this.topicRepository).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }

    @Test
    void shouldFailToDeleteTopicIfTopicNotExists() {
        BDDMockito.given(this.topicRepository.findById(1L))
                .willThrow(new InstanceNotFoundException("O tópico informado não existe"));

        Assertions.assertThrows(InstanceNotFoundException.class,
                () -> this.topicService.deleteTopic(1L, 1L),
                "O tópico informado não existe");

        BDDMockito.verify(this.topicRepository).findById(1L);
        BDDMockito.verifyNoInteractions(this.userClientRequest);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);

    }

    @Test
    void shouldFailToDeleteTopicIfUserServiceReturn404StatusCode() {
        BDDMockito.given(this.topicRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.TopicHelper.topicList().get(0)));

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willThrow(new RestClientException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));


        Assertions.assertThrows(RestClientException.class,
                () -> this.topicService.deleteTopic(1L, 1L),
                "Usuário não encontrado");


        BDDMockito.verify(this.topicRepository).findById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verify(this.topicRepository, Mockito.never()).delete(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @Test
    void shouldFailIfBasicUserAttemptDeleteTopicOfOtherAuthor() {
        BDDMockito.given(this.topicRepository.findById(2L))
                .willReturn(Optional.of(TestsHelper.TopicHelper.topicList().get(1)));

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));


        Assertions.assertThrows(PrivilegeValidationException.class,
                () -> this.topicService.deleteTopic(2L, 1L),
                "Usuário com privilégios insuficientes para realizar esta operação!");


        BDDMockito.verify(this.topicRepository).findById(2L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verify(this.topicRepository, Mockito.never()).delete(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @Test
    void topicAuthorShouldDeleteSpecifiedTopicWithSuccess() {
        BDDMockito.given(this.topicRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.TopicHelper.topicList().get(0)));

        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(1));


        Assertions.assertDoesNotThrow(
                () -> this.topicService.deleteTopic(1L, 2L));


        BDDMockito.verify(this.topicRepository).findById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verify(this.topicRepository).delete(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }


    @Test
    void userADMShouldDeleteTopicOfOtherAuthorWithSuccess() {
        BDDMockito.given(this.topicRepository.findById(2L))
                .willReturn(Optional.of(TestsHelper.TopicHelper.topicList().get(1)));

        BDDMockito.given(this.userClientRequest.getUserById(3L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(2));


        Assertions.assertDoesNotThrow(() -> this.topicService.deleteTopic(2L, 3L));


        BDDMockito.verify(this.topicRepository).findById(2L);
        BDDMockito.verify(this.userClientRequest).getUserById(3L);
        BDDMockito.verify(this.topicRepository).delete(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @Test
    void userMODShouldDeleteTopicOfOtherAuthorWithSuccess() {
        BDDMockito.given(this.topicRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.TopicHelper.topicList().get(0)));

        BDDMockito.given(this.userClientRequest.getUserById(3L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(2));


        Assertions.assertDoesNotThrow(() -> this.topicService.deleteTopic(1L, 3L));


        BDDMockito.verify(this.topicRepository).findById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(3L);
        BDDMockito.verify(this.topicRepository).delete(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }

}
