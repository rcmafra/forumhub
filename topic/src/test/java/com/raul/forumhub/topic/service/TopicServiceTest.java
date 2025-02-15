package com.raul.forumhub.topic.service;

import com.raul.forumhub.topic.client.UserClientRequest;
import com.raul.forumhub.topic.domain.Status;
import com.raul.forumhub.topic.domain.Topic;
import com.raul.forumhub.topic.dto.request.TopicCreateDTO;
import com.raul.forumhub.topic.dto.request.TopicUpdateDTO;
import com.raul.forumhub.topic.dto.response.GetTopicDTO;
import com.raul.forumhub.topic.exception.InstanceNotFoundException;
import com.raul.forumhub.topic.exception.RestClientException;
import com.raul.forumhub.topic.exception.TopicServiceException;
import com.raul.forumhub.topic.exception.ValidationException;
import com.raul.forumhub.topic.repository.TopicRepository;
import com.raul.forumhub.topic.util.TestsHelper;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TopicServiceTest {

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
        final TopicCreateDTO topicCreateDTO = new TopicCreateDTO("",
                "Como utilizar o Feign Client para integração do serviço x?",
                1L);

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willReturn(TestsHelper.CourseHelper.courseList().get(0));

        BDDMockito.given(this.topicRepository.save(any(Topic.class)))
                .willThrow(ConstraintViolationException.class);


        Assertions.assertThrows(ConstraintViolationException.class,
                () -> this.topicService.createTopic(topicCreateDTO, 1L));


        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.topicRepository).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);

    }


    @Test
    void shouldFailIfQuestionPropertyIsEmptyWhenCreateTopic() {
        final TopicCreateDTO topicCreateDTO = new TopicCreateDTO("Dúvida na utilização do Feign Client",
                "",
                1L);

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willReturn(TestsHelper.CourseHelper.courseList().get(0));

        BDDMockito.given(this.topicRepository.save(any(Topic.class)))
                .willThrow(ConstraintViolationException.class);


        Assertions.assertThrows(ConstraintViolationException.class,
                () -> this.topicService.createTopic(topicCreateDTO, 1L));


        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.topicRepository).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);

    }


    @Test
    void shouldFailToCreateTopicIfTitlePropertyIsGreaterThan150Chars() {
        final TopicCreateDTO topicCreateDTO = new TopicCreateDTO(
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
                () -> this.topicService.createTopic(topicCreateDTO, 1L),
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
        final TopicCreateDTO topicCreateDTO = new TopicCreateDTO("Dúvida na utilização do Feign Client",
                "Como utilizar o Feign Client para integração do serviço x?",
                1L);

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willThrow(new InstanceNotFoundException("O curso informado não existe"));


        Assertions.assertThrows(InstanceNotFoundException.class,
                () -> this.topicService.createTopic(topicCreateDTO, 1L),
                "O curso informado não existe");


        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoInteractions(this.topicRepository);


    }


    @Test
    void shouldFailToCreateTopicIfUserServiceReturn404StatusCode() {
        final TopicCreateDTO topicCreateDTO = new TopicCreateDTO("Dúvida na utilização do Feign Client",
                "Como utilizar o Feign Client para integração do serviço x?",
                1L);

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willThrow(new RestClientException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));


        Assertions.assertThrows(RestClientException.class,
                () -> this.topicService.createTopic(topicCreateDTO, 1L),
                "Usuário não encontrado");


        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoInteractions(this.courseService);
        BDDMockito.verifyNoInteractions(this.topicRepository);


    }


    @Test
    void shouldCreateTopicWithSuccessIfEverythingIsOK() {
        final TopicCreateDTO topicCreateDTO = new TopicCreateDTO("Dúvida na utilização do Feign Client",
                "Como utilizar o Feign Client para integração do serviço x?",
                1L);

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willReturn(TestsHelper.CourseHelper.courseList().get(0));


        Assertions.assertDoesNotThrow(() -> this.topicService.createTopic(topicCreateDTO, 1L));


        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.topicRepository).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);

    }


    @Test
    void shouldReturnAllTopicsUnsortedWithSuccessful() {
        Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());

        List<Topic> topicList = TestsHelper.TopicHelper.topicListWithAnswers();

        Page<GetTopicDTO> topicPage =
                new PageImpl<>(topicList, pageable, 3)
                        .map(GetTopicDTO::new);

        BDDMockito.given(this.topicRepository.findAll(pageable))
                .willReturn(new PageImpl<>(topicList, Pageable.unpaged(), 3));


        Assertions.assertDoesNotThrow(() -> this.topicService.topicList(pageable));


        Assertions.assertAll(
                () -> assertEquals(3, topicPage.getContent().size()),
                () -> assertEquals(10, topicPage.getSize()),
                () -> assertEquals(3, topicPage.getTotalElements()),
                () -> assertEquals(1, topicPage.getTotalPages())
        );

        BDDMockito.verify(this.topicRepository).findAll(pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);

    }


    @Test
    void shouldReturnAllTopicsSortedDescendantByCreateDateWithSuccessful() {
        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Topic> sortedTopicByCreatedAt = TestsHelper.TopicHelper.topicListWithAnswers()
                .stream().sorted(Comparator.comparing(Topic::getCreatedAt).reversed())
                .toList();

        Page<GetTopicDTO> topicPage =
                new PageImpl<>(sortedTopicByCreatedAt, pageable, 3)
                        .map(GetTopicDTO::new);

        BDDMockito.given(this.topicRepository.findAll(pageable))
                .willReturn(new PageImpl<>(sortedTopicByCreatedAt, pageable, 3));


        Assertions.assertDoesNotThrow(() -> this.topicService.topicList(pageable));


        Assertions.assertAll(
                () -> Assertions.assertEquals(3L, topicPage.getContent().get(0).topic().getId()),
                () -> Assertions.assertEquals(1L, topicPage.getContent().get(1).topic().getId()),
                () -> Assertions.assertEquals(2L, topicPage.getContent().get(2).topic().getId()),
                () -> Assertions.assertEquals(0, topicPage.getNumber()),
                () -> Assertions.assertEquals(3, topicPage.getContent().size()),
                () -> Assertions.assertEquals(10, topicPage.getSize()),
                () -> Assertions.assertEquals(3, topicPage.getTotalElements()),
                () -> Assertions.assertEquals(1, topicPage.getTotalPages())
        );

        BDDMockito.verify(this.topicRepository).findAll(pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);

    }


    @Test
    void shouldReturnTwoTopicsSortedAscendantByStatusWithSuccessful() {
        Pageable pageable = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.ASC, "status"));

        List<Topic> sortedTopicByStatus = TestsHelper.TopicHelper.topicListWithAnswers()
                .stream().filter(topic -> topic.getId().equals(2L) || topic.getId().equals(3L))
                .sorted(Comparator.comparing(Topic::getStatus))
                .toList();

        Page<GetTopicDTO> topicPage =
                new PageImpl<>(sortedTopicByStatus, pageable, 2)
                        .map(GetTopicDTO::new);

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
        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.ASC, "title"));

        List<Topic> sortedTopicByStatus = TestsHelper.TopicHelper.topicListWithAnswers()
                .stream().sorted(Comparator.comparing(Topic::getCreatedAt).reversed())
                .toList();

        Page<GetTopicDTO> topicPage =
                new PageImpl<>(sortedTopicByStatus, pageable, 3)
                        .map(GetTopicDTO::new);

        BDDMockito.given(this.topicRepository.findAll(pageable))
                .willReturn(new PageImpl<>(sortedTopicByStatus, pageable, 3));


        Assertions.assertDoesNotThrow(() -> this.topicService.topicList(pageable));


        Assertions.assertAll(
                () -> Assertions.assertEquals(3L, topicPage.getContent().get(0).topic().getId()),
                () -> Assertions.assertEquals(1L, topicPage.getContent().get(1).topic().getId()),
                () -> Assertions.assertEquals(2L, topicPage.getContent().get(2).topic().getId()),
                () -> Assertions.assertEquals(0, topicPage.getNumber()),
                () -> Assertions.assertEquals(3, topicPage.getContent().size()),
                () -> Assertions.assertEquals(10, topicPage.getSize()),
                () -> Assertions.assertEquals(3, topicPage.getTotalElements()),
                () -> Assertions.assertEquals(1, topicPage.getTotalPages())
        );

        BDDMockito.verify(this.topicRepository).findAll(pageable);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);

    }


    @Test
    void shouldFailToRequestTheSpecifiedTopicIfNotExists() {
        BDDMockito.given(this.topicRepository.findById(1L))
                .willThrow(new InstanceNotFoundException("O tópico informado não existe"));


        Assertions.assertThrows(InstanceNotFoundException.class,
                () -> this.topicService.getTopicById(1L),
                "O tópico informado não existe");


        BDDMockito.verify(this.topicRepository).findById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);


    }


    @DisplayName("Should return the specified topic with successful if exists")
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
        final TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO(
                "",
                "Como posso integrar minha API com o Elasticsearch para monitoração?",
                Status.UNSOLVED, 1L
        );

        BDDMockito.given(this.topicRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.TopicHelper.topicList().get(0)));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willReturn(TestsHelper.CourseHelper.courseList().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        BDDMockito.given(this.topicRepository.save(any(Topic.class)))
                .willThrow(ConstraintViolationException.class);


        Assertions.assertThrows(ConstraintViolationException.class,
                () -> this.topicService.updateTopic(1L, 1L, topicUpdateDTO));


        BDDMockito.verify(this.topicRepository).findById(1L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verify(this.topicRepository).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }


    @Test
    void shouldFailIfQuestionPropertyIsEmptyWhenEditTopic() {
        TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO(
                "Dúvida quanto a utilização do Elasticsearch",
                "",
                Status.UNSOLVED, 1L
        );

        BDDMockito.given(this.topicRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.TopicHelper.topicList().get(0)));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willReturn(TestsHelper.CourseHelper.courseList().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        BDDMockito.given(this.topicRepository.save(any(Topic.class)))
                .willThrow(ConstraintViolationException.class);


        Assertions.assertThrows(ConstraintViolationException.class,
                () -> this.topicService.updateTopic(1L, 1L, topicUpdateDTO));


        BDDMockito.verify(this.topicRepository).findById(1L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verify(this.topicRepository).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }


    @Test
    void shouldFailToEditTopicIfCourseNotExists() {
        final TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO(
                "Dúvida quanto a utilização do Elasticsearch",
                "Como posso integrar minha API com o Elasticsearch para monitoração?",
                Status.UNSOLVED, 1L
        );

        BDDMockito.given(this.topicRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.TopicHelper.topicList().get(0)));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willThrow(new InstanceNotFoundException("O curso informado não existe"));

        Assertions.assertThrows(InstanceNotFoundException.class,
                () -> this.topicService.updateTopic(1L, 1L, topicUpdateDTO),
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
        final TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO(
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
                () -> this.topicService.updateTopic(1L, 1L, topicUpdateDTO),
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
        final TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO(
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

        Assertions.assertThrows(ValidationException.class,
                () -> this.topicService.updateTopic(2L, 1L, topicUpdateDTO),
                "Privilégio insuficiente");


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
        final TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO(
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

        Assertions.assertThrows(TopicServiceException.class,
                () -> this.topicService.updateTopic(3L, 3L, topicUpdateDTO),
                "O tópico pertence a um autor inexistente, ele não pode ser editado");


        BDDMockito.verify(this.topicRepository).findById(3L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(3L);
        BDDMockito.verify(this.topicRepository, BDDMockito.never()).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }


    @Test
    void topicAuthorShouldEditSpecifiedTopicWithSuccess() {
        final TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO(
                "Dúvida na utilização do WebClient",
                "Como utilizar o WebClient para integração do serviço x?",
                Status.UNSOLVED, 1L
        );

        BDDMockito.given(this.topicRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.TopicHelper.topicList().get(0)));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willReturn(TestsHelper.CourseHelper.courseList().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        Assertions.assertDoesNotThrow(
                () -> this.topicService.updateTopic(1L, 1L, topicUpdateDTO));


        BDDMockito.verify(this.topicRepository).findById(1L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verify(this.topicRepository).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @Test
    void userADMShouldEditTopicOfOtherAuthorWithSuccess() {
        final TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO(
                "Dúvida na utilização do RestTemplate",
                "Como utilizar o RestTemplate para integração do serviço x?",
                Status.UNSOLVED, 1L
        );

        BDDMockito.given(this.topicRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.TopicHelper.topicList().get(0)));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willReturn(TestsHelper.CourseHelper.courseList().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(3L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(2));

        Assertions.assertDoesNotThrow(
                () -> this.topicService.updateTopic(1L, 3L, topicUpdateDTO));


        BDDMockito.verify(this.topicRepository).findById(1L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(3L);
        BDDMockito.verify(this.topicRepository).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @Test
    void userMODShouldEditTopicOfOtherAuthorWithSuccess() {
        final TopicUpdateDTO topicUpdateDTO = new TopicUpdateDTO(
                "Dúvida na utilização da API de validação do Spring",
                "Quais são as anotações da API de validação do Spring?",
                Status.UNSOLVED, 1L
        );

        BDDMockito.given(this.topicRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.TopicHelper.topicList().get(0)));

        BDDMockito.given(this.courseService.getCourseById(1L))
                .willReturn(TestsHelper.CourseHelper.courseList().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        Assertions.assertDoesNotThrow(
                () -> this.topicService.updateTopic(1L, 2L, topicUpdateDTO));


        BDDMockito.verify(this.topicRepository).findById(1L);
        BDDMockito.verify(this.courseService).getCourseById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verify(this.topicRepository).save(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
        BDDMockito.verifyNoMoreInteractions(this.courseService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


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


        Assertions.assertThrows(ValidationException.class,
                () -> this.topicService.deleteTopic(2L, 1L),
                "Privilégio insuficiente");


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

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));


        Assertions.assertDoesNotThrow(
                () -> this.topicService.deleteTopic(1L, 1L));


        BDDMockito.verify(this.topicRepository).findById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
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
        BDDMockito.given(this.topicRepository.findById(3L))
                .willReturn(Optional.of(TestsHelper.TopicHelper.topicList().get(2)));

        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(1));


        Assertions.assertDoesNotThrow(() -> this.topicService.deleteTopic(3L, 2L));


        BDDMockito.verify(this.topicRepository).findById(3L);
        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verify(this.topicRepository).delete(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }

}
