package com.backend.api.forumhub.service;

import com.backend.api.forumhub.domain.Course;
import com.backend.api.forumhub.domain.Profile;
import com.backend.api.forumhub.domain.Topic;
import com.backend.api.forumhub.domain.User;
import com.backend.api.forumhub.dto.request.CreateTopicDTO;
import com.backend.api.forumhub.dto.request.UpdateTopicDTO;
import com.backend.api.forumhub.dto.response.GetTopicDTO;
import com.backend.api.forumhub.repository.TopicRepository;
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

    public void createTopic(CreateTopicDTO createTopicDTO) throws Exception {
        User author = userService.getUserById(createTopicDTO.user_id());
        Course course = courseService.getCourseById(createTopicDTO.course_id());

        Topic topic = new Topic(createTopicDTO.title(), createTopicDTO.message(), author, course);
        this.saveTopic(topic);
    }

    public Page<GetTopicDTO> topicList(Pageable pageable) {
        return this.topicRepository.findAll(pageable).map(GetTopicDTO::new);
    }

    public Topic getTopic(Long topic_id) throws Exception {
        return topicRepository.findById(topic_id).orElseThrow(() -> new Exception("Topic not found"));
    }

    /*todo
     *Obter o usuário logado atualmente e verificar se ele é o dono do tópico antes de
     * efetuar o update desse tópico
     * */
    public void updateTopic(Long topic_id, UpdateTopicDTO update) throws Exception {
        Topic topic = this.getTopic(topic_id);
        Course course = this.courseService.getCourseById(update.course_id());

        if (!topic.getAuthor().getId().equals(update.author_id())) {
            throw new Exception("Topic not belonging to this author");
        }

        topic.setTitle(update.topic().getTitle());
        topic.setMessage(update.topic().getMessage());
        topic.setStatus(update.topic().getStatus());
        topic.setCourse(course);

        this.saveTopic(topic);

    }

    /*todo
    * Obter o usuário logado que enviou a request e verificar se foi ele quem criou o tópico
    * */
    public void deleteTopic(Long topic_id, Long user_id) throws Exception {
        Topic topic = this.getTopic(topic_id);
        User user = this.userService.getUserById(user_id);

        if(!topic.getAuthor().getId().equals(user_id) || !(user.getProfile().getProfileName().equals(Profile.ProfileName.MOD) ||
                user.getProfile().getProfileName().equals(Profile.ProfileName.ADM))){
            throw new Exception("Insufficient privilege");
        }

        this.topicRepository.delete(topic);
    }

    public void saveTopic(Topic topic) {
        this.topicRepository.save(topic);
    }
}
