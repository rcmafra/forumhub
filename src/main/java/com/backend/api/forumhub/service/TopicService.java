package com.backend.api.forumhub.service;

import com.backend.api.forumhub.domain.Course;
import com.backend.api.forumhub.domain.Topic;
import com.backend.api.forumhub.domain.User;
import com.backend.api.forumhub.dto.request.CreateTopicDTO;
import com.backend.api.forumhub.dto.request.UpdateTopicDTO;
import com.backend.api.forumhub.dto.response.GetTopicDTO;
import com.backend.api.forumhub.repository.TopicRepository;
import com.backend.api.forumhub.validator.AuthorizationValidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TopicService {

    private final TopicRepository topicRepository;

    private final UserService userService;

    private final CourseService courseService;

    public TopicService(TopicRepository topicRepository, UserService userService, CourseService courseService) {
        this.topicRepository = topicRepository;
        this.userService = userService;
        this.courseService = courseService;
    }

    public void createTopic(CreateTopicDTO createTopicDTO, Long user_id) throws Exception {
        User author = userService.getUserById(user_id);
        Course course = courseService.getCourseById(createTopicDTO.course_id());

        Topic topic = new Topic(createTopicDTO.title(), createTopicDTO.message(), author, course);
        this.saveTopic(topic);
    }

    public Page<GetTopicDTO> topicList(Pageable pageable) {
        return this.topicRepository.findAll(pageable).map(GetTopicDTO::new);
    }


    public Topic getTopicById(Long topic_id) throws Exception {
        return topicRepository.findById(topic_id).orElseThrow(() -> new Exception("Topic not found"));
    }


    public void updateTopic(Long topic_id, Long user_id, UpdateTopicDTO update) throws Exception {
        Topic topic = this.getTopicById(topic_id);
        Course course = this.courseService.getCourseById(update.course_id());
        User user = this.userService.getUserById(user_id);

        AuthorizationValidate.AuthValidator(topic.getAuthor().getId(), user.getId(),
                user.getProfile().getProfileName());

        topic.setTitle(update.topic().getTitle());
        topic.setMessage(update.topic().getMessage());
        topic.setStatus(update.topic().getStatus());
        topic.setCourse(course);

        this.saveTopic(topic);

    }


    public void deleteTopic(Long topic_id, Long user_id) throws Exception {
        Topic topic = this.getTopicById(topic_id);
        User user = this.userService.getUserById(user_id);

        AuthorizationValidate.AuthValidator(topic.getAuthor().getId(), user.getId(),
                user.getProfile().getProfileName());

        this.topicRepository.delete(topic);
    }

    public void saveTopic(Topic topic) {
        this.topicRepository.save(topic);
    }
}
