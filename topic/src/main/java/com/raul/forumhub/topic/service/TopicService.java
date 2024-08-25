package com.raul.forumhub.topic.service;

import com.raul.forumhub.topic.client.UserClientRequest;
import com.raul.forumhub.topic.domain.*;
import com.raul.forumhub.topic.dto.request.TopicCreateDTO;
import com.raul.forumhub.topic.dto.request.TopicUpdateDTO;
import com.raul.forumhub.topic.dto.response.GetTopicDTO;
import com.raul.forumhub.topic.exception.InstanceNotFoundException;
import com.raul.forumhub.topic.exception.TopicServiceException;
import com.raul.forumhub.topic.repository.TopicRepository;
import com.raul.forumhub.topic.validator.AuthorizationValidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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

        Topic topic = new Topic(topicCreateDTO.title(), topicCreateDTO.message(), author, course);
        this.saveTopic(topic);
    }

    public Page<GetTopicDTO> topicList(Pageable pageable) {
        Page<Topic> topicPage = this.topicRepository.findAll(pageable);

        List<GetTopicDTO> topicDTOList = topicPage.stream().map(v1 -> {
            if (Objects.isNull(v1.getAuthor())) {
                this.devocateUnknownAuthorTopic(v1);
            }
            v1.getAnswers().forEach(v2 -> {
                if (Objects.isNull(v2.getAuthor())) {
                    this.devocateUnknownAuthorAnswer(v2);
                }
            });

            return new GetTopicDTO(v1);
        }).toList();

        return new PageImpl<>(topicDTOList, pageable, topicPage.getTotalElements());
    }


    public Topic getTopicById(Long topic_id) {
        Topic topic = topicRepository.findById(topic_id).orElseThrow(() ->
                new InstanceNotFoundException("O tópico informado não existe"));

        if (Objects.isNull(topic.getAuthor())) {
            this.devocateUnknownAuthorTopic(topic);
        }
        topic.getAnswers().forEach(v1 -> {
            if(Objects.isNull(v1.getAuthor())){
                this.devocateUnknownAuthorAnswer(v1);
            }
        });

        return topic;
    }


    public void updateTopic(Long topic_id, Long user_id, TopicUpdateDTO update) {
        Topic topic = this.getTopicById(topic_id);
        Course course = this.courseService.getCourseById(update.course_id());
        Author author = this.userClientRequest.getUserById(user_id);

        AuthorizationValidate.AuthValidator(topic.getAuthor().getId(), author.getId(),
                author.getProfile().getProfileName());

        topic.setTitle(update.topic().getTitle());
        topic.setMessage(update.topic().getMessage());
        topic.setStatus(update.topic().getStatus());
        topic.setCourse(course);

        this.saveTopic(topic);

    }


    public void deleteTopic(Long topic_id, Long user_id) {
        Topic topic = this.getTopicById(topic_id);
        Author author = this.userClientRequest.getUserById(user_id);

        AuthorizationValidate.AuthValidator(topic.getAuthor().getId(), author.getId(),
                author.getProfile().getProfileName());

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

    private void devocateUnknownAuthorTopic(Topic topic) {
        topic.setAuthor(new Author("Desconhecido", "Desconhecido",
                new Profile(Profile.ProfileName.BASIC)));
    }

    private void devocateUnknownAuthorAnswer(Answer answer) {
        answer.setAuthor(new Author("Desconhecido", "Desconhecido",
                new Profile(Profile.ProfileName.BASIC)));
    }
}
