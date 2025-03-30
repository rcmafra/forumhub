package com.raul.forumhub.topic.service;

import com.raul.forumhub.topic.client.UserClientRequest;
import com.raul.forumhub.topic.domain.Answer;
import com.raul.forumhub.topic.domain.Topic;
import com.raul.forumhub.topic.dto.request.AnswerRequestDTO;
import com.raul.forumhub.topic.exception.*;
import com.raul.forumhub.topic.repository.AnswerRepository;
import com.raul.forumhub.topic.util.TestsHelper;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AnswerServiceTest {

    @Mock
    AnswerRepository answerRepository;

    @Mock
    TopicService topicService;

    @Mock
    UserClientRequest userClientRequest;

    @InjectMocks
    AnswerService answerService;


    @Test
    void shouldFailIfQuestionPropertyIsEmptyWhenAnswerTopic() {
        final AnswerRequestDTO answerRequestDTO = new AnswerRequestDTO("");

        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicListWithAnswers().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        BDDMockito.given(this.answerRepository.save(any(Answer.class)))
                .willThrow(ConstraintViolationException.class);


        Assertions.assertThrows(ConstraintViolationException.class,
                () -> this.answerService.answerTopic(1L, 1L, answerRequestDTO));


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verify(this.answerRepository).save(any(Answer.class));
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);

    }


    @Test
    void shouldFailToAnswerTopicIfSpecifiedTopicNotExists() {
        final AnswerRequestDTO answerRequestDTO = new AnswerRequestDTO("Resposta teste");

        BDDMockito.given(this.topicService.getTopicById(1L))
                .willThrow(new InstanceNotFoundException("O tópico informado não existe"));


        Assertions.assertThrows(InstanceNotFoundException.class,
                () -> this.answerService.answerTopic(1L, 1L, answerRequestDTO),
                "O tópico informado não existe");


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoInteractions(this.userClientRequest);
        BDDMockito.verifyNoInteractions(this.answerRepository);


    }


    @Test
    void shouldFailToAnswerTopicIfUserServiceReturn404StatusCode() {
        final AnswerRequestDTO answerRequestDTO = new AnswerRequestDTO("Resposta teste");

        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicListWithAnswers().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willThrow(new RestClientException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));


        Assertions.assertThrows(RestClientException.class,
                () -> this.answerService.answerTopic(1L, 1L, answerRequestDTO),
                "Usuário não encontrado");


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoInteractions(this.answerRepository);


    }


    @Test
    void shouldAnswerTopicWithSuccessIfEverythingIsOk() {
        final AnswerRequestDTO answerRequestDTO = new AnswerRequestDTO("Resposta teste");

        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicListWithAnswers().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));


        Assertions.assertDoesNotThrow(
                () -> this.answerService.answerTopic(1L, 1L, answerRequestDTO));


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verify(this.answerRepository).save(any(Answer.class));
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);


    }


    @Test
    void shouldNotToMarkBestAnswerIfSpecifiedTopicNotExists() {
        BDDMockito.given(this.topicService.getTopicById(1L)).
                willThrow(new InstanceNotFoundException("O tópico informado não existe"));


        Assertions.assertThrows(InstanceNotFoundException.class,
                () -> this.answerService.markBestAnswer(1L, 1L, 1L),
                "O tópico informado não existe");


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoInteractions(this.userClientRequest);
        BDDMockito.verifyNoInteractions(this.answerRepository);


    }


    @Test
    void shouldNotToMarkBestAnswerIfUserServiceReturn404StatusCode() {
        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicListWithAnswers().get(0));

        BDDMockito.given(this.answerRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(0)));

        BDDMockito.given(this.userClientRequest.getUserById(1L)).
                willThrow(new RestClientException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));


        Assertions.assertThrows(RestClientException.class,
                () -> this.answerService.markBestAnswer(1L, 1L, 1L));


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verify(this.answerRepository).findById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);

    }


    @Test
    void shouldNotToMarkBestAnswerIfAuthenticatedUserIsNotOwnerTopic() {
        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicListWithAnswers().get(0));

        BDDMockito.given(this.answerRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(0)));

        BDDMockito.given(this.userClientRequest.getUserById(2L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        Assertions.assertThrows(PrivilegeValidationException.class,
                () -> this.answerService.markBestAnswer(1L, 1L, 2L));


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verify(this.answerRepository).findById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);


    }


    @Test
    void shouldNotToMarkBestAnswerIfYetNotExistsAnswer() {
        BDDMockito.given(this.topicService.getTopicById(4L))
                .willReturn(TestsHelper.TopicHelper.topicList().get(3));

        BDDMockito.given(this.answerRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(0)));

        BDDMockito.given(this.userClientRequest.getUserById(1L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(0));


        Assertions.assertThrows(ValidationException.class,
                () -> this.answerService.markBestAnswer(4L, 1L, 1L));


        BDDMockito.verify(this.topicService).getTopicById(4L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verify(this.answerRepository).findById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);

    }


    @Test
    void shouldNotToMarkBestAnswerIfAlreadyExistsBestAnswer() {
        BDDMockito.given(this.topicService.getTopicById(2L))
                .willReturn(TestsHelper.TopicHelper.topicListWithAnswers().get(1));

        BDDMockito.given(this.answerRepository.findById(2L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(1)));

        BDDMockito.given(this.userClientRequest.getUserById(2L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(1));


        Assertions.assertThrows(BusinessException.class,
                () -> this.answerService.markBestAnswer(2L, 2L, 2L));


        BDDMockito.verify(this.topicService).getTopicById(2L);
        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verify(this.answerRepository).findById(2L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);


    }


    @Test
    void shouldToMarkBestAnswerWithSuccessIfEverythingIsOk() {
        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicListWithAnswers().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(1L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        BDDMockito.given(this.answerRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(0)));


        Assertions.assertDoesNotThrow(
                () -> this.answerService.markBestAnswer(1L, 1L, 1L));


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verify(this.answerRepository).findById(1L);
        BDDMockito.verify(this.topicService).saveTopic(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);


    }


    @Test
    void shouldFailToUnmarkBestAnswerIfSpecifiedTopicNotExists() {
        BDDMockito.given(this.topicService.getTopicById(6L))
                .willThrow(new InstanceNotFoundException("O tópico informado não existe"));

        Assertions.assertThrows(InstanceNotFoundException.class,
                () -> this.answerService.unmarkBestAnswer(6L, 1L, 1L),
                "O tópico informado não existe");

        BDDMockito.verify(this.topicService).getTopicById(6L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoInteractions(this.userClientRequest);
        BDDMockito.verifyNoInteractions(this.answerRepository);

    }


    @Test
    void shouldFailToUnmarkBestAnswerIfSpecifiedAnswerNotExists() {
        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicList().get(0));

        BDDMockito.given(this.answerRepository.findById(6L))
                .willThrow(new InstanceNotFoundException("A resposta informada não existe"));

        Assertions.assertThrows(InstanceNotFoundException.class,
                () -> this.answerService.unmarkBestAnswer(1L, 6L, 1L),
                "A resposta informada não existe");

        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verify(this.answerRepository).findById(6L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);
        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }


    @Test
    void shouldFailToUnmarkBestAnswerIfUserServiceReturn404StatusCode() {
        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicList().get(0));

        BDDMockito.given(this.answerRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(0)));

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willThrow(new RestClientException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        Assertions.assertThrows(RestClientException.class,
                () -> this.answerService.unmarkBestAnswer(1L, 1L, 1L),
                "Usuário não encontrado");

        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verify(this.answerRepository).findById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }

    @Test
    void shouldNotToUnmarkBestAnswerIfAuthenticatedUserIsNotOwnerTopic() {
        BDDMockito.given(this.topicService.getTopicById(2L))
                .willReturn(TestsHelper.TopicHelper.topicListWithAnswers().get(1));

        BDDMockito.given(this.answerRepository.findById(2L)).
                willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(1)));

        BDDMockito.given(this.userClientRequest.getUserById(1L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        Assertions.assertThrows(PrivilegeValidationException.class,
                () -> this.answerService.unmarkBestAnswer(2L, 2L, 1L));


        BDDMockito.verify(this.topicService).getTopicById(2L);
        BDDMockito.verify(this.answerRepository).findById(2L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);


    }


    @Test
    void shouldNotToUnmarkBestAnswerIfYetNotExistsAnswer() {
        BDDMockito.given(this.topicService.getTopicById(4L))
                .willReturn(TestsHelper.TopicHelper.topicList().get(3));

        BDDMockito.given(this.answerRepository.findById(1L)).
                willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(0)));

        BDDMockito.given(this.userClientRequest.getUserById(1L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(0));


        Assertions.assertThrows(ValidationException.class,
                () -> this.answerService.unmarkBestAnswer(4L, 1L, 1L));


        BDDMockito.verify(this.topicService).getTopicById(4L);
        BDDMockito.verify(this.answerRepository).findById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @Test
    void shouldNotToUnmarkBestAnswerIfAnswerProvidedNotBelongsToTheTopicProvided() {
        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicListWithAnswers().get(0));

        BDDMockito.given(this.answerRepository.findById(2L)).
                willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(1)));

        BDDMockito.given(this.userClientRequest.getUserById(1L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(0));


        Assertions.assertThrows(ValidationException.class,
                () -> this.answerService.unmarkBestAnswer(1L, 2L, 1L));


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verify(this.answerRepository).findById(2L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }

    @Test
    void shouldNotToUnmarkBestAnswerIfAnswerProvidedIsNotAsBestAnswer() {
        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicListWithAnswers().get(0));

        BDDMockito.given(this.answerRepository.findById(1L)).
                willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(0)));

        BDDMockito.given(this.userClientRequest.getUserById(1L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(0));


        Assertions.assertThrows(ValidationException.class,
                () -> this.answerService.unmarkBestAnswer(1L, 1L, 1L));


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verify(this.answerRepository).findById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }

    @Test
    void shouldUnmarkBestAnswerWithSuccessIfEverythingIsOk() {
        BDDMockito.given(this.topicService.getTopicById(2L))
                .willReturn(TestsHelper.TopicHelper.topicListWithAnswers().get(1));

        BDDMockito.given(this.userClientRequest.getUserById(2L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        BDDMockito.given(this.answerRepository.findById(2L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(1)));


        Assertions.assertDoesNotThrow(
                () -> this.answerService.unmarkBestAnswer(2L, 2L, 2L));


        BDDMockito.verify(this.topicService).getTopicById(2L);
        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verify(this.answerRepository).findById(2L);
        BDDMockito.verify(this.topicService).saveTopic(any(Topic.class));
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);


    }


    @Test
    void shouldFailIfSolutionPropertyIsEmptyWhenEditAnswer() {
        final AnswerRequestDTO answerUpdateDTO = new AnswerRequestDTO("");

        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicListWithAnswers().get(0));

        BDDMockito.given(this.answerRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(0)));

        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        BDDMockito.given(this.answerRepository.save(any(Answer.class)))
                .willThrow(ConstraintViolationException.class);


        Assertions.assertThrows(ConstraintViolationException.class,
                () -> this.answerService.updateAnswer(1L, 1L, 2L,
                        answerUpdateDTO));


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verify(this.answerRepository).findById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verify(this.answerRepository).save(any(Answer.class));
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @Test
    void shouldFailToEditAnswerIfTopicNotExists() {
        final AnswerRequestDTO answerUpdateDTO =
                new AnswerRequestDTO("Primeiro teste de edição de uma resposta");

        BDDMockito.given(this.topicService.getTopicById(1L))
                .willThrow(new InstanceNotFoundException("O tópico informado não existe"));


        Assertions.assertThrows(InstanceNotFoundException.class,
                () -> this.answerService.updateAnswer(1L, 1L, 1L,
                        answerUpdateDTO), "O tópico informado não existe");


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoInteractions(this.answerRepository);
        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }


    @Test
    void shouldFailToEditAnswerIfAnswerNotExists() {
        final AnswerRequestDTO answerUpdateDTO =
                new AnswerRequestDTO("Primeiro teste de edição de uma resposta");

        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicList().get(0));

        BDDMockito.given(this.answerRepository.findById(1L))
                .willThrow(new InstanceNotFoundException("A resposta informada não existe"));


        Assertions.assertThrows(InstanceNotFoundException.class,
                () -> this.answerService.updateAnswer(1L, 1L, 1L,
                        answerUpdateDTO), "A resposta informada não existe");


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verify(this.answerRepository).findById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);
        BDDMockito.verifyNoInteractions(this.userClientRequest);

    }


    @Test
    void shouldFailToEditAnswerIfUserServiceReturn404StatusCode() {
        final AnswerRequestDTO answerUpdateDTO =
                new AnswerRequestDTO("Primeiro teste de edição de uma resposta");

        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicList().get(0));

        BDDMockito.given(this.answerRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(0)));

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willThrow(new RestClientException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));


        Assertions.assertThrows(RestClientException.class,
                () -> this.answerService.updateAnswer(1L, 1L, 1L,
                        answerUpdateDTO), "Usuário não encontrado");


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verify(this.answerRepository).findById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @Test
    void shouldFailIfBasicUserAttemptEditAnswerOfOtherAuthor() {
        final AnswerRequestDTO answerUpdateDTO =
                new AnswerRequestDTO("Primeiro teste de edição de uma resposta");

        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicList().get(0));

        BDDMockito.given(this.answerRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(0)));

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));


        Assertions.assertThrows(PrivilegeValidationException.class,
                () -> this.answerService.updateAnswer(1L, 1L, 1L,
                        answerUpdateDTO), "Usuário com privilégios insuficientes para realizar esta operação!");


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verify(this.answerRepository).findById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }


    @DisplayName("Should fail with status code 422 when attempt edit a answer of unknown author")
    @Test
    void shouldFailWhenAttemptEditAnswerOfUnknownAuthor() {
        final AnswerRequestDTO answerUpdateDTO =
                new AnswerRequestDTO("Primeiro teste de edição de uma resposta");


        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicList().get(0));

        BDDMockito.given(this.answerRepository.findById(4L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(3)));

        BDDMockito.given(this.userClientRequest.getUserById(3L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(2));


        Assertions.assertThrows(BusinessException.class,
                () -> this.answerService.updateAnswer(1L, 4L, 3L,
                        answerUpdateDTO),
                "O tópico pertence a um autor inexistente, ele não pode ser editado");


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verify(this.answerRepository).findById(4L);
        BDDMockito.verify(this.userClientRequest).getUserById(3L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }


    @Test
    void answerAuthorShouldEditSpecifiedAnswerWithSuccessIfEverythingIsOk() {
        final AnswerRequestDTO answerUpdateDTO =
                new AnswerRequestDTO("Primeiro teste de edição de uma resposta");

        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicList().get(0));

        BDDMockito.given(this.answerRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(0)));

        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(1));


        Assertions.assertDoesNotThrow(
                () -> this.answerService.updateAnswer(1L, 1L, 2L,
                        answerUpdateDTO));


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verify(this.answerRepository).findById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verify(this.answerRepository).save(any(Answer.class));
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }


    @Test
    void userADMShouldEditAnswerOfOtherAuthorWithSuccessIfEverythingIsOk() {
        final AnswerRequestDTO answerUpdateDTO =
                new AnswerRequestDTO("Segundo teste de edição de uma resposta");


        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicList().get(0));

        BDDMockito.given(this.answerRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(0)));

        BDDMockito.given(this.userClientRequest.getUserById(3L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(2));


        Assertions.assertDoesNotThrow(
                () -> this.answerService.updateAnswer(1L, 1L, 3L,
                        answerUpdateDTO));


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verify(this.answerRepository).findById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(3L);
        BDDMockito.verify(this.answerRepository).save(any(Answer.class));
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }


    @Test
    void userMODShouldEditAnswerOfOtherAuthorWithSuccessIfEverythingIsOk() {
        final AnswerRequestDTO answerUpdateDTO =
                new AnswerRequestDTO("Terceiro teste de edição de uma resposta");

        BDDMockito.given(this.topicService.getTopicById(3L))
                .willReturn(TestsHelper.TopicHelper.topicList().get(2));

        BDDMockito.given(this.answerRepository.findById(3L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(2)));

        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(1));


        Assertions.assertDoesNotThrow(
                () -> this.answerService.updateAnswer(3L, 3L, 2L,
                        answerUpdateDTO));


        BDDMockito.verify(this.topicService).getTopicById(3L);
        BDDMockito.verify(this.answerRepository).findById(3L);
        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verify(this.answerRepository).save(any(Answer.class));
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }


    @Test
    void shouldFailToDeleteAnswerIfUserServiceReturn404StatusCode() {
        BDDMockito.given(this.answerRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(0)));

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willThrow(new RestClientException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));


        Assertions.assertThrows(RestClientException.class,
                () -> this.answerService.deleteAnswer(1L, 1L, 1L),
                "Usuário não encontrado");


        BDDMockito.verify(this.answerRepository).findById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }


    @Test
    void shouldFailIfProvidedAnswerNotBelongingToTheProvidedTopicWhenDeleteAnswer() {
        BDDMockito.given(this.answerRepository.findById(2L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(1)));

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willThrow(new BusinessException("A resposta fornecida não pertence a esse tópico"));


        Assertions.assertThrows(BusinessException.class,
                () -> this.answerService.deleteAnswer(1L, 2L, 1L),
                "A resposta fornecida não pertence a esse tópico");


        BDDMockito.verify(this.answerRepository).findById(2L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }


    @Test
    void shouldFailIfBasicUserAttemptDeleteAnswerOfOtherAuthor() {
        BDDMockito.given(this.answerRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(0)));

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));


        Assertions.assertThrows(PrivilegeValidationException.class,
                () -> this.answerService.deleteAnswer(1L, 1L, 1L),
                "Usuário com privilégios insuficientes para realizar esta operação!");


        BDDMockito.verify(this.answerRepository).findById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }


    @Test
    void answerAuthorShouldDeleteSpecifiedAnswerWithSuccessIfEverythingIsOk() {
        BDDMockito.given(this.answerRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(0)));

        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(1));


        Assertions.assertDoesNotThrow(
                () -> this.answerService.deleteAnswer(1L, 1L, 2L));


        BDDMockito.verify(this.answerRepository).findById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verify(this.answerRepository).delete(any(Answer.class));
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }


    @Test
    void userADMShouldDeleteTopicOfOtherAuthorWithSuccessIfEverythingIsOk() {
        BDDMockito.given(this.answerRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(0)));

        BDDMockito.given(this.userClientRequest.getUserById(3L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(2));


        Assertions.assertDoesNotThrow(
                () -> this.answerService.deleteAnswer(1L, 1L, 3L));


        BDDMockito.verify(this.answerRepository).findById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(3L);
        BDDMockito.verify(this.answerRepository).delete(any(Answer.class));
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }


    @Test
    void userMODShouldDeleteTopicOfOtherAuthorWithSuccessIfEverythingIsOk() {
        BDDMockito.given(this.answerRepository.findById(3L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(2)));

        BDDMockito.given(this.userClientRequest.getUserById(2L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(1));


        Assertions.assertDoesNotThrow(
                () -> this.answerService.deleteAnswer(3L, 3L, 2L));


        BDDMockito.verify(this.answerRepository).findById(3L);
        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verify(this.answerRepository).delete(any(Answer.class));
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }

}