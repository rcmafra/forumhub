package com.raul.forumhub.topic.service;

import com.raul.forumhub.topic.client.UserClientRequest;
import com.raul.forumhub.topic.domain.Answer;
import com.raul.forumhub.topic.domain.Author;
import com.raul.forumhub.topic.domain.Status;
import com.raul.forumhub.topic.domain.Topic;
import com.raul.forumhub.topic.dto.request.AnswerDTO;
import com.raul.forumhub.topic.dto.response.AnswerResponseDTO;
import com.raul.forumhub.topic.exception.AnswerServiceException;
import com.raul.forumhub.topic.exception.InstanceNotFoundException;
import com.raul.forumhub.topic.repository.AnswerRepository;
import com.raul.forumhub.topic.util.PermissionUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    public void answerTopic(Long topic_id, Long user_id, AnswerDTO answerDTO) {
        Topic topic = topicService.getTopicById(topic_id);
        Author author = userClientRequest.getUserById(user_id);

        Answer answer = new Answer(answerDTO.solution());
        answer.setTopic(topic);
        answer.setAuthor(author);

        answerRepository.save(answer);
    }


    public void markBestAnswer(Long topic_id, Long answer_id, Long user_id) {
        Topic topic = topicService.getTopicById(topic_id);
        Author author = userClientRequest.getUserById(user_id);

        PermissionUtils.validateTopicOwner(topic.getAuthor().getId(), author.getId());

        Set<Answer> answersOfTheTopic = topic.getAnswers()
                .stream().filter(Objects::nonNull).collect(Collectors.collectingAndThen(
                        Collectors.toSet(),
                        answers -> {
                            if (answers.isEmpty()) {
                                throw new AnswerServiceException("Ainda não existe respostas para esse tópico");
                            }
                            return answers;
                        }
                ));

        Answer alreadyHasBestAnswers = answersOfTheTopic.stream().filter(Answer::isBestAnswer)
                .findFirst().orElse(null);

        if (alreadyHasBestAnswers != null)
            throw new AnswerServiceException("Este tópico já possui a resposta [ID: "
                    .concat(alreadyHasBestAnswers.getId().toString())
                    .concat("] como melhor resposta"));

        Answer answer = this.getAnswerById(answer_id);

        topic.setStatus(Status.SOLVED);
        answer.setBestAnswer(true);

        this.topicService.saveTopic(topic);
        this.saveAnswer(answer);


    }

    public AnswerResponseDTO updateAnswer(Long topic_id, Long answer_id, Long user_id, AnswerDTO answerDTO) {
        this.topicService.getTopicById(topic_id);
        Answer answer = this.getAnswerById(answer_id);
        Author author = this.userClientRequest.getUserById(user_id);

        PermissionUtils.privilegeValidator(answer.getAuthor().getId(), author);

        if (answer.getAuthor().getUsername().equals("Desconhecido") &&
                answer.getAuthor().getEmail().equals("desconhecido@email.com")) {
            throw new AnswerServiceException("A resposta pertence a um autor inexistente, " +
                    "ele não pode ser editado");
        }

        answer.setSolution(answerDTO.solution());
        this.answerRepository.save(answer);

        return new AnswerResponseDTO(answer);
    }

    public void deleteAnswer(Long topic_id, Long answer_id, Long user_id) {
        Answer answer = this.getAnswerById(answer_id);
        Author author = this.userClientRequest.getUserById(user_id);

        if (!answer.getTopic().getId().equals(topic_id)) {
            throw new AnswerServiceException("A resposta fornecida não pertence a esse tópico");
        }

        PermissionUtils.privilegeValidator(answer.getAuthor().getId(), author);

        this.answerRepository.delete(answer);

    }


    public Answer getAnswerById(Long id) {
        return this.answerRepository.findById(id).orElseThrow(() -> new InstanceNotFoundException("A resposta informada não existe"));
    }

    public void saveAnswer(Answer answer) {
        this.answerRepository.save(answer);
    }
}
