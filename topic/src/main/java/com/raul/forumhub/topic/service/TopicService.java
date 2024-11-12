package com.raul.forumhub.topic.service;

import com.raul.forumhub.topic.client.UserClientRequest;
import com.raul.forumhub.topic.domain.Author;
import com.raul.forumhub.topic.domain.Course;
import com.raul.forumhub.topic.domain.Topic;
import com.raul.forumhub.topic.dto.request.TopicCreateDTO;
import com.raul.forumhub.topic.dto.request.TopicUpdateDTO;
import com.raul.forumhub.topic.dto.response.GetTopicDTO;
import com.raul.forumhub.topic.exception.InstanceNotFoundException;
import com.raul.forumhub.topic.exception.TopicServiceException;
import com.raul.forumhub.topic.repository.TopicRepository;
import com.raul.forumhub.topic.utility.AuthorizationValidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicService {

    private final TopicRepository topicRepository;

    private final UserClientRequest userClientRequest;

    private final CourseService courseService;


    public TopicService(TopicRepository topicRepository, UserClientRequest userClientRequest, CourseService courseService) {
        this.topicRepository = topicRepository;
        this.userClientRequest = userClientRequest;
        this.courseService = courseService;
    }

    public void createTopic(TopicCreateDTO topicCreateDTO, Long user_id) {
        Author author = userClientRequest.getUserById(user_id);
        Course course = courseService.getCourseById(topicCreateDTO.course_id());

        Topic topic = new Topic(topicCreateDTO.title(), topicCreateDTO.question(), author, course);
        this.saveTopic(topic);
    }

    public Page<GetTopicDTO> topicList(Pageable pageable) {
        Page<Topic> topicPage = this.topicRepository.findAll(pageable);
        List<GetTopicDTO> topicDTOList = topicPage.map(GetTopicDTO::new).toList();

        return new PageImpl<>(topicDTOList, pageable, topicPage.getTotalElements());
    }


    public Topic getTopicById(Long topic_id) {
        return topicRepository.findById(topic_id).orElseThrow(() ->
                new InstanceNotFoundException("O tópico informado não existe"));
    }


    public void updateTopic(Long topic_id, Long user_id, TopicUpdateDTO update) {
        Topic topic = this.getTopicById(topic_id);
        Course course = this.courseService.getCourseById(update.course_id());
        Author author = this.userClientRequest.getUserById(user_id);

        AuthorizationValidate.permissionValidator(topic.getAuthor().getId(), author);

        if (topic.getAuthor().getUsername().equals("Desconhecido") &&
                topic.getAuthor().getEmail().equals("desconhecido@email.com")) {
            throw new TopicServiceException("O tópico pertence a um autor inexistente, " +
                    "ele não pode ser editado");
        }

        topic.setTitle(update.topic().getTitle());
        topic.setQuestion(update.topic().getQuestion());
        topic.setStatus(update.topic().getStatus());
        topic.setCourse(course);

        this.saveTopic(topic);

    }


    public void deleteTopic(Long topic_id, Long user_id) {
        Topic topic = this.getTopicById(topic_id);
        Author author = this.userClientRequest.getUserById(user_id);

        AuthorizationValidate.permissionValidator(topic.getAuthor().getId(), author);

        this.topicRepository.delete(topic);
    }

    public void saveTopic(Topic topic) {
        this.topicRepository.save(topic);
    }

    public void validateTopicOwner(Long author_id, Long user_id) {
        if (!author_id.equals(user_id)) {
            throw new TopicServiceException("O tópico fornecido não pertence a esse autor");
        }
    }


}
