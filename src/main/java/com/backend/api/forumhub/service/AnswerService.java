package com.backend.api.forumhub.service;

import com.backend.api.forumhub.domain.Answer;
import com.backend.api.forumhub.domain.Topic;
import com.backend.api.forumhub.domain.User;
import com.backend.api.forumhub.domain.Status;
import com.backend.api.forumhub.dto.request.AnswerTopicDTO;
import com.backend.api.forumhub.repository.AnswerRepository;
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

    public void answerTopic(Long topic_id, AnswerTopicDTO answerTopicDTO) throws Exception {
        Topic topic = topicService.getTopic(topic_id);
        User user = userService.getUserById(answerTopicDTO.user_id());
        Answer answer = new Answer(topic, answerTopicDTO.solution(), user);
        answerRepository.save(answer);
    }


    /* todo
    *  Verificar se quem enviou a request para marcar a resposta como melhor (usuário logado) é
    * a mesma usuário que criou o tópico
    * */
    public void markBetterAnswer(Long topic_id, Long answer_id) throws Exception {

        Topic topic = topicService.getTopic(topic_id);
        Answer answer = getAnswer(answer_id);

//        if(!topic.getAuthor().getId().equals(Long.parseLong(sub))){
//            throw new Exception("Topic not belonging to this author");
//        }

        topic.setStatus(Status.SOLVED);
        answer.setBetterAnswer(true);

        this.topicService.saveTopic(topic);
        this.saveAnswer(answer);

    }

    /* todo
    * Verificar se o usuário que está logado e enviou a request de deleção da resposta
    * é o mesmo usuário que criou a resposta ou se é um usuário com perfil de MOD ou ADM.
    *  */
    public void deleteAnswer(Long topic_id, Long answer_id) throws Exception {
        Topic topic = this.topicService.getTopic(topic_id);
        Answer answer = this.getAnswer(answer_id);

    }


    public Answer getAnswer(Long id) throws Exception {
        return this.answerRepository.findById(id).orElseThrow(() -> new Exception("Answer not found"));
    }

    public void saveAnswer(Answer answer) {
        this.answerRepository.save(answer);
    }
}
