package com.raul.forumhub.topic.service;

import com.raul.forumhub.topic.client.UserClientRequest;
import com.raul.forumhub.topic.domain.Answer;
import com.raul.forumhub.topic.domain.Topic;
import com.raul.forumhub.topic.dto.request.AnswerTopicDTO;
import com.raul.forumhub.topic.dto.request.AnswerUpdateDTO;
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


    @Order(1)
    @Test
    void shouldFailIfQuestionPropertyIsEmptyWhenAnswerTopic() {
        final AnswerTopicDTO answerTopicDTO = new AnswerTopicDTO("");

        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicListWithAnswers().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));

        BDDMockito.given(this.answerRepository.save(any(Answer.class)))
                .willThrow(ConstraintViolationException.class);


        Assertions.assertThrows(ConstraintViolationException.class,
                () -> this.answerService.answerTopic(1L, 1L, answerTopicDTO));


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verify(this.answerRepository).save(any(Answer.class));
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);

    }

    @Order(2)
    @Test
    void shouldFailToAnswerTopicIfSpecifiedTopicNotExists() {
        final AnswerTopicDTO answerTopicDTO = new AnswerTopicDTO("Resposta teste");

        BDDMockito.given(this.topicService.getTopicById(1L))
                .willThrow(new InstanceNotFoundException("O tópico informado não existe"));


        Assertions.assertThrows(InstanceNotFoundException.class,
                () -> this.answerService.answerTopic(1L, 1L, answerTopicDTO),
                "O tópico informado não existe");


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoInteractions(this.userClientRequest);
        BDDMockito.verifyNoInteractions(this.answerRepository);


    }


    @Order(3)
    @Test
    void shouldFailToAnswerTopicIfUserServiceReturn404StatusCode() {
        final AnswerTopicDTO answerTopicDTO = new AnswerTopicDTO("Resposta teste");

        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicListWithAnswers().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willThrow(new RestClientException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));


        Assertions.assertThrows(RestClientException.class,
                () -> this.answerService.answerTopic(1L, 1L, answerTopicDTO),
                "Usuário não encontrado");


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoInteractions(this.answerRepository);


    }

    @Order(4)
    @Test
    void shouldAnswerTopicWithSuccessIfEverythingIsOk() {
        final AnswerTopicDTO answerTopicDTO = new AnswerTopicDTO("Resposta teste");

        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicListWithAnswers().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));


        Assertions.assertDoesNotThrow(
                () -> this.answerService.answerTopic(1L, 1L, answerTopicDTO));


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verify(this.answerRepository).save(any(Answer.class));
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);


    }

    @Order(5)
    @Test
    void shouldFailToMarkAnswerBestIfSpecifiedTopicNotExists() {
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

    @Order(6)
    @Test
    void shouldFailToMarkAnswerBestIfUserServiceReturn404StatusCode() {
        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicListWithAnswers().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(1L)).
                willThrow(new RestClientException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));


        Assertions.assertThrows(RestClientException.class,
                () -> this.answerService.markBestAnswer(1L, 1L, 1L));


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoInteractions(this.answerRepository);

    }

    @Order(7)
    @Test
    void shouldFailToMarkAnswerBestIfAuthenticatedUserIsNotOwnerTopic() {
        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicListWithAnswers().get(0));

        BDDMockito.given(this.userClientRequest.getUserById(2L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(1));

        Assertions.assertThrows(TopicServiceException.class,
                () -> this.answerService.markBestAnswer(1L, 1L, 2L),
                "O tópico fornecido não pertence a esse autor");


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoInteractions(this.answerRepository);


    }

    @Order(8)
    @Test
    void shouldFailToMarkAnswerBestIfYetNotExistsAnswer() {
        BDDMockito.given(this.topicService.getTopicById(4L))
                .willReturn(TestsHelper.TopicHelper.topicList().get(3));

        BDDMockito.given(this.userClientRequest.getUserById(1L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(0));


        Assertions.assertThrows(AnswerServiceException.class,
                () -> this.answerService.markBestAnswer(4L, 1L, 1L));


        BDDMockito.verify(this.topicService).getTopicById(4L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoInteractions(this.answerRepository);

    }


    @Order(9)
    @Test
    void shouldFailToMarkAnswerBestIfAlreadyExistsBestAnswer() {
        BDDMockito.given(this.topicService.getTopicById(2L))
                .willReturn(TestsHelper.TopicHelper.topicListWithAnswers().get(1));

        BDDMockito.given(this.userClientRequest.getUserById(2L)).
                willReturn(TestsHelper.AuthorHelper.authorList().get(1));


        Assertions.assertThrows(AnswerServiceException.class,
                () -> this.answerService.markBestAnswer(2L, 2L, 2L),
                "Já existe uma melhor resposta para este tópico");


        BDDMockito.verify(this.topicService).getTopicById(2L);
        BDDMockito.verify(this.userClientRequest).getUserById(2L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoInteractions(this.answerRepository);


    }

    @Order(10)
    @Test
    void shouldMarkAnswerBestWithSuccessIfEverythingIsOk() {
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
        BDDMockito.verify(this.answerRepository).save(any(Answer.class));
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);


    }

    @Order(11)
    @Test
    void shouldFailIfSolutionPropertyIsEmptyWhenEditAnswer() {
        final AnswerUpdateDTO answerUpdateDTO = new AnswerUpdateDTO("");

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

    @Order(12)
    @Test
    void shouldFailToEditAnswerIfTopicNotExists() {
        final AnswerUpdateDTO answerUpdateDTO =
                new AnswerUpdateDTO("Primeiro teste de edição de uma resposta");

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

    @Order(13)
    @Test
    void shouldFailToEditAnswerIfAnswerNotExists() {
        final AnswerUpdateDTO answerUpdateDTO =
                new AnswerUpdateDTO("Primeiro teste de edição de uma resposta");

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


    @Order(14)
    @Test
    void shouldFailToEditAnswerIfUserServiceReturn404StatusCode() {
        final AnswerUpdateDTO answerUpdateDTO =
                new AnswerUpdateDTO("Primeiro teste de edição de uma resposta");

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

    @Order(15)
    @Test
    void shouldFailIfBasicUserAttemptEditAnswerOfOtherAuthor() {
        final AnswerUpdateDTO answerUpdateDTO =
                new AnswerUpdateDTO("Primeiro teste de edição de uma resposta");

        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicList().get(0));

        BDDMockito.given(this.answerRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(0)));

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));


        Assertions.assertThrows(ValidationException.class,
                () -> this.answerService.updateAnswer(1L, 1L, 1L,
                        answerUpdateDTO), "Privilégio insuficiente");


        BDDMockito.verify(this.topicService).getTopicById(1L);
        BDDMockito.verify(this.answerRepository).findById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.topicService);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);

    }

    @Order(16)
    @DisplayName("Should fail with status code 422 when attempt edit a answer of unknown author")
    @Test
    void shouldFailWhenAttemptEditAnswerOfUnknownAuthor() {
        final AnswerUpdateDTO answerUpdateDTO =
                new AnswerUpdateDTO("Primeiro teste de edição de uma resposta");


        BDDMockito.given(this.topicService.getTopicById(1L))
                .willReturn(TestsHelper.TopicHelper.topicList().get(0));

        BDDMockito.given(this.answerRepository.findById(4L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(3)));

        BDDMockito.given(this.userClientRequest.getUserById(3L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(2));


        Assertions.assertThrows(AnswerServiceException.class,
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

    @Order(17)
    @Test
    void answerAuthorShouldEditSpecifiedAnswerWithSuccessIfEverythingIsOk() {
        final AnswerUpdateDTO answerUpdateDTO =
                new AnswerUpdateDTO("Primeiro teste de edição de uma resposta");

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

    @Order(18)
    @Test
    void userADMShouldEditAnswerOfOtherAuthorWithSuccessIfEverythingIsOk() {
        final AnswerUpdateDTO answerUpdateDTO =
                new AnswerUpdateDTO("Segundo teste de edição de uma resposta");


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

    @Order(19)
    @Test
    void userMODShouldEditAnswerOfOtherAuthorWithSuccessIfEverythingIsOk() {
        final AnswerUpdateDTO answerUpdateDTO =
                new AnswerUpdateDTO("Terceiro teste de edição de uma resposta");

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

    @Order(20)
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

    @Order(21)
    @Test
    void shouldFailIfProvidedAnswerNotBelongingToTheProvidedTopicWhenDeleteAnswer() {
        BDDMockito.given(this.answerRepository.findById(2L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(1)));

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willThrow(new AnswerServiceException("A resposta fornecida não pertence a esse tópico"));


        Assertions.assertThrows(AnswerServiceException.class,
                () -> this.answerService.deleteAnswer(1L, 2L, 1L),
                "A resposta fornecida não pertence a esse tópico");


        BDDMockito.verify(this.answerRepository).findById(2L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }

    @Order(22)
    @Test
    void shouldFailIfBasicUserAttemptDeleteAnswerOfOtherAuthor() {
        BDDMockito.given(this.answerRepository.findById(1L))
                .willReturn(Optional.of(TestsHelper.AnswerHelper.answerList().get(0)));

        BDDMockito.given(this.userClientRequest.getUserById(1L))
                .willReturn(TestsHelper.AuthorHelper.authorList().get(0));


        Assertions.assertThrows(ValidationException.class,
                () -> this.answerService.deleteAnswer(1L, 1L, 1L),
                "Privilégio insuficiente");


        BDDMockito.verify(this.answerRepository).findById(1L);
        BDDMockito.verify(this.userClientRequest).getUserById(1L);
        BDDMockito.verifyNoMoreInteractions(this.answerRepository);
        BDDMockito.verifyNoMoreInteractions(this.userClientRequest);


    }

    @Order(23)
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

    @Order(24)
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

    @Order(25)
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