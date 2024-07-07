package com.backend.api.forumhub.service;

import com.backend.api.forumhub.domain.*;
import com.backend.api.forumhub.dto.request.AnswerTopicDTO;
import com.backend.api.forumhub.repository.AnswerRepository;
import com.backend.api.forumhub.validator.AuthorizationValidate;
import org.springframework.stereotype.Service;

@Service
public class AnswerService {

    private final AnswerRepository answerRepository;

    private final TopicService topicService;

    private final UserService userService;

    public AnswerService(AnswerRepository answerRepository, TopicService topicService, UserService userService) {
        this.answerRepository = answerRepository;
        this.topicService = topicService;
        this.userService = userService;
    }

    public void answerTopic(Long topic_id, Long user_id, AnswerTopicDTO answerTopicDTO) throws Exception {
        Topic topic = topicService.getTopicById(topic_id);
        User user = userService.getUserById(user_id);

        Answer answer = new Answer(answerTopicDTO.solution());
        answer.setTopic(topic);
        answer.setAuthor(user);

        answerRepository.save(answer);
    }


    public void markBetterAnswer(Long topic_id, Long answer_id, Long user_id) throws Exception {
        Topic topic = topicService.getTopicById(topic_id);
        Answer answer = getAnswerById(answer_id);

        if(!topic.getAuthor().getId().equals(user_id)){
            throw new Exception("This topic not belonging to this author");
        }

        topic.setStatus(Status.SOLVED);
        answer.setBetterAnswer(true);

        this.topicService.saveTopic(topic);
        this.saveAnswer(answer);

    }

    public void deleteAnswer(Long topic_id, Long answer_id, Long user_id) throws Exception {
        this.topicService.getTopicById(topic_id);
        Answer answer = this.getAnswerById(answer_id);
        User user = this.userService.getUserById(user_id);

        AuthorizationValidate.AuthValidator(answer.getAuthor().getId(), user.getId(),
                user.getProfile().getProfileName());

        this.answerRepository.delete(answer);

    }


    public Answer getAnswerById(Long id) throws Exception {
        return this.answerRepository.findById(id).orElseThrow(() -> new Exception("Answer not found"));
    }

    public void saveAnswer(Answer answer) {
        this.answerRepository.save(answer);
    }
}
