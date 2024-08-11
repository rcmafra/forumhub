package com.raul.forumhub.topic.service;

import com.raul.forumhub.topic.client.UserClientRequest;
import com.raul.forumhub.topic.domain.Answer;
import com.raul.forumhub.topic.domain.Status;
import com.raul.forumhub.topic.domain.Topic;
import com.raul.forumhub.topic.domain.Author;
import com.raul.forumhub.topic.dto.request.AnswerTopicDTO;
import com.raul.forumhub.topic.dto.request.AnswerUpdateDTO;
import com.raul.forumhub.topic.dto.response.GetAnswerDTO;
import com.raul.forumhub.topic.repository.AnswerRepository;
import com.raul.forumhub.topic.validator.AuthorizationValidate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

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

    public void answerTopic(Long topic_id, Long user_id, AnswerTopicDTO answerTopicDTO) throws Exception {
        Topic topic = topicService.getTopicById(topic_id);
        Author author = userClientRequest.getUserById(user_id);

        Answer answer = new Answer(answerTopicDTO.solution());
        answer.setTopic(topic);
        answer.setAuthor(author);

        answerRepository.save(answer);
    }


    public void markBestAnswer(Long topic_id, Long answer_id, Long user_id) throws Exception {
        Topic topic = topicService.getTopicById(topic_id);

        List<Answer> answersOfTheTopic = this.answerRepository.getAnswerByTopic(topic)
                .orElse(Collections.emptyList());

        boolean hasBestAnswer = answersOfTheTopic.stream().anyMatch(Answer::isBestAnswer);

        if (!topic.getAuthor().getId().equals(user_id)) {
            throw new Exception("This topic not belonging to this author");
        }
        else if(hasBestAnswer){
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

    public GetAnswerDTO updateAnswer(Long topic_id, Long answer_id, Long user_id, AnswerUpdateDTO answerUpdateDTO) throws Exception {
        this.topicService.getTopicById(topic_id);
        Answer answer = this.getAnswerById(answer_id);
        Author author = this.userClientRequest.getUserById(user_id);

        if(!author.getId().equals(answer.getAuthor().getId())){
            throw new RuntimeException("This answer not belonging to this user");
        }

        answer.setSolution(answerUpdateDTO.solution());
        this.answerRepository.save(answer);

        return new GetAnswerDTO(answer);
    }

    public void deleteAnswer(Long topic_id, Long answer_id, Long user_id) throws Exception {
        Answer answer = this.getAnswerById(answer_id);
        Author author = this.userClientRequest.getUserById(user_id);

        if(!answer.getTopic().getId().equals(topic_id)){
            throw new RuntimeException("This answer not belonging to the topic");
        }

        AuthorizationValidate.AuthValidator(answer.getAuthor().getId(), author.getId(),
                author.getProfile().getProfileName());

        this.answerRepository.delete(answer);

    }


    public Answer getAnswerById(Long id) throws Exception {
        return this.answerRepository.findById(id).orElseThrow(() -> new Exception("Answer not found"));
    }

    public void saveAnswer(Answer answer) {
        this.answerRepository.save(answer);
    }
}
