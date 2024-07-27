package com.backend.api.forumhub.service;

import com.backend.api.forumhub.domain.Answer;
import com.backend.api.forumhub.domain.Status;
import com.backend.api.forumhub.domain.Topic;
import com.backend.api.forumhub.domain.User;
import com.backend.api.forumhub.dto.request.AnswerTopicDTO;
import com.backend.api.forumhub.dto.request.NewAnswer;
import com.backend.api.forumhub.dto.response.AnswerDTO;
import com.backend.api.forumhub.repository.AnswerRepository;
import com.backend.api.forumhub.validator.AuthorizationValidate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

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


    public void markBestAnswer(Long topic_id, Long answer_id, Long user_id) throws Exception {
        Topic topic = topicService.getTopicById(topic_id);

        List<Answer> answersOfTheTopic = this.answerRepository.getAnswerByTopic(topic)
                .orElse(Collections.emptyList());

        boolean hasBetterAnswer = answersOfTheTopic.stream().anyMatch(Answer::isBestAnswer);

        if (!topic.getAuthor().getId().equals(user_id)) {
            throw new Exception("This topic not belonging to this author");
        }
        else if(hasBetterAnswer){
            throw new RuntimeException("Already exists a best answer for this topic");
        }
        else {
            Answer answer = getAnswerById(answer_id);

            topic.setStatus(Status.SOLVED);
            answer.setBestAnswer(true);

            this.topicService.saveTopic(topic);
            this.saveAnswer(answer);
        }

    }

    public AnswerDTO updateAnswer(Long topic_id, Long answer_id, Long user_id, NewAnswer newAnswer) throws Exception {
        this.topicService.getTopicById(topic_id);
        Answer answer = this.getAnswerById(answer_id);
        User user = this.userService.getUserById(user_id);

        if(!user.getId().equals(answer.getAuthor().getId())){
            throw new RuntimeException("This answer not belonging to this user");
        }

        answer.setSolution(newAnswer.solution());
        this.answerRepository.save(answer);

        return new AnswerDTO(answer);
    }

    public void deleteAnswer(Long topic_id, Long answer_id, Long user_id) throws Exception {
        Answer answer = this.getAnswerById(answer_id);
        User user = this.userService.getUserById(user_id);

        if(!answer.getTopic().getId().equals(topic_id)){
            throw new RuntimeException("This answer not belonging to the topic");
        }

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
