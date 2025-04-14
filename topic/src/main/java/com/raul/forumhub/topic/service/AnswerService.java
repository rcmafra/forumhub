package com.raul.forumhub.topic.service;

import com.raul.forumhub.topic.client.UserClientRequest;
import com.raul.forumhub.topic.domain.Answer;
import com.raul.forumhub.topic.domain.Author;
import com.raul.forumhub.topic.domain.Status;
import com.raul.forumhub.topic.domain.Topic;
import com.raul.forumhub.topic.dto.request.AnswerRequestDTO;
import com.raul.forumhub.topic.dto.response.AnswerResponseDTO;
import com.raul.forumhub.topic.exception.BusinessException;
import com.raul.forumhub.topic.exception.InstanceNotFoundException;
import com.raul.forumhub.topic.repository.AnswerRepository;
import com.raul.forumhub.topic.util.PermissionUtils;
import com.raul.forumhub.topic.util.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AnswerService {

    private final AnswerRepository answerRepository;

    private final TopicService topicService;

    private final UserClientRequest userClientRequest;

    public AnswerService(AnswerRepository answerRepository, TopicService topicService, UserClientRequest userClientRequest) {
        this.answerRepository = answerRepository;
        this.topicService = topicService;
        this.userClientRequest = userClientRequest;
    }

    public void answerTopic(Long topic_id, Long user_id, AnswerRequestDTO answerRequestDTO) {
        Topic topic = topicService.getTopicById(topic_id);
        Author author = userClientRequest.getUserById(user_id);

        Answer answer = new Answer(answerRequestDTO.solution());
        answer.setTopic(topic);
        answer.setAuthor(author);

        this.saveAnswer(answer);

        log.info("O tópico [ID: {}] recebeu a resposta: {}", topic_id, answer);
    }


    public void markBestAnswer(Long topic_id, Long answer_id, Long user_id) {
        Topic topic = topicService.getTopicById(topic_id);
        Answer answer = this.getAnswerById(answer_id);
        Author author = userClientRequest.getUserById(user_id);

        PermissionUtils.validateTopicOwner(topic, author);
        ValidationUtils.validateMarkBestAnswer(topic, answer);


        topic.setStatus(Status.SOLVED);
        answer.setBestAnswer(true);

        this.topicService.saveTopic(topic);

        log.info("Resposta [ID: {}] marcada como melhor resposta para o tópico [ID: {}]", answer_id, topic_id);

    }

    public void unmarkBestAnswer(Long topic_id, Long answer_id, Long user_id) {
        Topic topic = topicService.getTopicById(topic_id);
        Answer answer = this.getAnswerById(answer_id);
        Author author = userClientRequest.getUserById(user_id);

        PermissionUtils.validateTopicOwner(topic, author);
        ValidationUtils.validateUnmarkBestAnswer(topic, answer);


        topic.setStatus(Status.UNSOLVED);
        answer.setBestAnswer(false);

        this.topicService.saveTopic(topic);

        log.info("Resposta [ID: {}] desmarcada como melhor resposta para o tópico [ID: {}]", answer_id, topic_id);

    }


    public AnswerResponseDTO updateAnswer(Long topic_id, Long answer_id, Long user_id, AnswerRequestDTO answerRequestDTO) {
        this.topicService.getTopicById(topic_id);
        Answer answer = this.getAnswerById(answer_id);
        Author author = this.userClientRequest.getUserById(user_id);

        PermissionUtils.privilegeValidator(answer.getAuthor().getId(), author);

        if (answer.getAuthor().getId() == 0L || answer.getAuthor().getUsername()
                .equalsIgnoreCase("anonymous")) {
            throw new BusinessException("A resposta pertence a um autor inexistente, " +
                                        "ela não pode ser editada!");
        }

        answer.setSolution(answerRequestDTO.solution());
        this.answerRepository.save(answer);

        log.info("Resposta [ID: {}] do tópico [ID: {}] editada com sucesso!", answer_id, topic_id);

        return new AnswerResponseDTO(answer);
    }

    public void deleteAnswer(Long topic_id, Long answer_id, Long user_id) {
        Answer answer = this.getAnswerById(answer_id);
        Author author = this.userClientRequest.getUserById(user_id);

        if (!answer.getTopic().getId().equals(topic_id)) {
            throw new BusinessException(String.format("A resposta [ID: %d] fornecida não pertence ao tópico [ID: %d]", answer_id, topic_id));
        }

        PermissionUtils.privilegeValidator(answer.getAuthor().getId(), author);

        this.answerRepository.delete(answer);

        log.info("Resposta [ID: {}] do tópico [ID: {}] removida com sucesso!", answer_id, topic_id);

    }


    public Answer getAnswerById(Long id) {
        return this.answerRepository.findById(id).orElseThrow(() ->
                new InstanceNotFoundException(String.format("A resposta [ID: %d] informada não existe", id)));
    }

    public void saveAnswer(Answer answer) {
        this.answerRepository.save(answer);
    }
}
