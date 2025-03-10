package com.raul.forumhub.topic.service;

import com.raul.forumhub.topic.client.UserClientRequest;
import com.raul.forumhub.topic.domain.Author;
import com.raul.forumhub.topic.domain.Course;
import com.raul.forumhub.topic.domain.Topic;
import com.raul.forumhub.topic.dto.request.TopicCreateRequestDTO;
import com.raul.forumhub.topic.dto.request.TopicUpdateRequestDTO;
import com.raul.forumhub.topic.dto.response.TopicResponseDTO;
import com.raul.forumhub.topic.exception.InstanceNotFoundException;
import com.raul.forumhub.topic.exception.TopicServiceException;
import com.raul.forumhub.topic.repository.TopicRepository;
import com.raul.forumhub.topic.util.PermissionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    public void createTopic(TopicCreateRequestDTO topicCreateRequestDTO, Long user_id) {
        Author author = userClientRequest.getUserById(user_id);
        Course course = courseService.getCourseById(topicCreateRequestDTO.course_id());

        Topic topic = new Topic(topicCreateRequestDTO.title(), topicCreateRequestDTO.question(), author, course);
        this.saveTopic(topic);
    }

    public Page<TopicResponseDTO> topicList(Pageable pageable) {
        return this.topicRepository.findAll(pageable).map(TopicResponseDTO::new);
    }


    public Topic getTopicById(Long topic_id) {
        return topicRepository.findById(topic_id).orElseThrow(() ->
                new InstanceNotFoundException("O tópico informado não existe"));
    }


    public TopicResponseDTO updateTopic(Long topic_id, Long user_id, TopicUpdateRequestDTO update) {
        Topic topic = this.getTopicById(topic_id);
        Course course = this.courseService.getCourseById(update.course_id());
        Author author = this.userClientRequest.getUserById(user_id);

        PermissionUtils.privilegeValidator(topic.getAuthor().getId(), author);

        if (topic.getAuthor().getId() == 0L) {
            throw new TopicServiceException("O tópico pertence a um autor inexistente, " +
                                            "ele não pode ser editado");
        }

        topic.setTitle(update.title());
        topic.setQuestion(update.question());
        topic.setStatus(update.status());
        topic.setCourse(course);

        this.saveTopic(topic);
        return new TopicResponseDTO(topic);

    }


    public void deleteTopic(Long topic_id, Long user_id) {
        Topic topic = this.getTopicById(topic_id);
        Author author = this.userClientRequest.getUserById(user_id);

        PermissionUtils.privilegeValidator(topic.getAuthor().getId(), author);

        this.topicRepository.delete(topic);
    }

    public void saveTopic(Topic topic) {
        this.topicRepository.save(topic);
    }


}
